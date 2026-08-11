package ru.yandex.practicum.telemetry.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.avro.specific.SpecificRecordBase;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, SensorEventAvro> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaTopicsProperties topics;
    private final Map<String, SensorsSnapshotAvro> snapshots;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    // ... объявление полей и конструктора ...

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(Collections.singletonList(topics.sensors()));
            log.info("Успешная подписка на топик: {}", topics.sensors());
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    Optional<SensorsSnapshotAvro> sensorsSnapshotOpt = updateState(record.value());

                    sensorsSnapshotOpt.ifPresent(
                            sensorsSnapshotAvro -> producer.send(new ProducerRecord<>(
                                    topics.snapshots(),
                                    record.value().getHubId(),
                                    sensorsSnapshotAvro
                            ))
                    );
                }
                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения работы (wakeup).");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    /*
        Проверяем, есть ли снапшот для event.getHubId()
        Если снапшот есть, то достаём его
        Если нет, то создаём новый

        Проверяем, есть ли в снапшоте данные для event.getId()
            Если данные есть, то достаём их в переменную oldState
                  Проверка, если oldState.getTimestamp() произошёл позже, чем
                  event.getTimestamp() или oldState.getData() равен
                  event.getPayload(), то ничего обнавлять не нужно, выходим из метода
                  вернув Optional.empty()

        // если дошли до сюда, значит, пришли новые данные и
        // снапшот нужно обновить.
        Создаём экземпляр SensorStateAvro на основе данных события.
        Добавляем полученный экземпляр в снапшот.
        Обновляем таймстемп снапшота таймстемпом из события.
        Возвращаем снапшот - Optional.of(snapshot)
    */
    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        if (event == null) {
            return Optional.empty();
        }

        String hubId = event.getHubId();
        String eventId = event.getId();
        Instant eventTimestamp = event.getTimestamp();

        SensorsSnapshotAvro sensorsSnapshotAvro = snapshots.computeIfAbsent(hubId, key ->
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(eventTimestamp)
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        Map<String, SensorStateAvro> currentStates = sensorsSnapshotAvro.getSensorsState();
        Map<String, SensorStateAvro> sensorsStateAvro = currentStates != null ?
                new HashMap<>(currentStates) : new HashMap<>();

        if (sensorsStateAvro.containsKey(eventId)) {
            SensorStateAvro oldState = sensorsStateAvro.get(eventId);
            boolean isOldTimestampNewer = oldState.getTimestamp().isAfter(eventTimestamp);
            boolean isDataIdentical = Objects.equals(oldState.getData(), event.getPayload());

            if (isOldTimestampNewer || isDataIdentical) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTimestamp)
                .setData(event.getPayload())
                .build();

        sensorsStateAvro.put(event.getId(), newState);
        sensorsSnapshotAvro.setSensorsState(sensorsStateAvro);
        sensorsSnapshotAvro.setTimestamp(eventTimestamp);

        return Optional.of(sensorsSnapshotAvro);
    }
}
