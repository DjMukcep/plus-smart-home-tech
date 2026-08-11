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

    public void start() {
        addShutdownHook();
        subscribe();

        try {
            consumeEvents();
        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения работы (wakeup).");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            closeResources();
        }
    }

    private void consumeEvents() {
        while (true) {
            ConsumerRecords<String, SensorEventAvro> records =
                    consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

            if (!records.isEmpty()) {
                processRecords(records);
                consumer.commitSync();
            }
        }
    }

    private void processRecords(
            ConsumerRecords<String, SensorEventAvro> records
    ) {
        for (ConsumerRecord<String, SensorEventAvro> record : records) {
            processRecord(record);
        }
    }

    private void processRecord(ConsumerRecord<String, SensorEventAvro> record) {
        Optional<SensorsSnapshotAvro> snapshot = updateState(record.value());

        snapshot.ifPresent(
                sensorsSnapshotAvro -> sendSnapshot(
                        record.value().getHubId(),
                        sensorsSnapshotAvro
                )
        );
    }

    private void sendSnapshot(
            String hubId,
            SensorsSnapshotAvro snapshot
    ) {
        producer.send(new ProducerRecord<>(
                topics.snapshots(),
                hubId,
                snapshot
        ));
    }

    private void subscribe() {
        consumer.subscribe(Collections.singletonList(topics.sensors()));
        log.info("Успешная подписка на топик: {}", topics.sensors());
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private void closeResources() {
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

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        if (event == null) {
            return Optional.empty();
        }

        SensorsSnapshotAvro snapshot = getOrCreateSnapshot(event);
        Map<String, SensorStateAvro> sensorStates = getSensorStates(snapshot);

        if (shouldSkipUpdate(sensorStates, event)) {
            return Optional.empty();
        }

        updateSnapshot(snapshot, sensorStates, event);

        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro getOrCreateSnapshot(SensorEventAvro event) {
        return snapshots.computeIfAbsent(
                event.getHubId(),
                key -> SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setSensorsState(new HashMap<>())
                        .build()
        );
    }

    private Map<String, SensorStateAvro> getSensorStates(SensorsSnapshotAvro snapshot) {
        Map<String, SensorStateAvro> currentStates = snapshot.getSensorsState();

        return currentStates != null
                ? new HashMap<>(currentStates)
                : new HashMap<>();
    }

    private boolean shouldSkipUpdate(
            Map<String, SensorStateAvro> sensorStates,
            SensorEventAvro event
    ) {
        SensorStateAvro oldState = sensorStates.get(event.getId());

        if (oldState == null) {
            return false;
        }

        boolean isOldTimestampNewer = oldState.getTimestamp().isAfter(event.getTimestamp());
        boolean isDataIdentical = Objects.equals(oldState.getData(), event.getPayload());

        return isOldTimestampNewer || isDataIdentical;
    }

    private void updateSnapshot(
            SensorsSnapshotAvro snapshot,
            Map<String, SensorStateAvro> sensorStates,
            SensorEventAvro event
    ) {
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorStates.put(event.getId(), newState);

        snapshot.setSensorsState(sensorStates);
        snapshot.setTimestamp(event.getTimestamp());
    }
}
