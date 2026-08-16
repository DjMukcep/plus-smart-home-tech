package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.repository.action.Action;
import ru.yandex.practicum.telemetry.analyzer.repository.action.ActionType;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.Condition;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.repository.scenario.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.scenario.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.sensor.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.sensor.SensorRepository;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "ru.yandex.practicum.telemetry.analyzer.repository")
public class HubEventProcessor implements Runnable {

    private final Consumer<String, HubEventAvro> consumer;
    private final KafkaTopicsProperties topics;
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    @Override
    public void run() {
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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private void subscribe() {
        consumer.subscribe(Collections.singletonList(topics.hubs()));
        log.info("Успешная подписка на топик: {}", topics.hubs());
    }

    private void consumeEvents() {
        while (true) {
            ConsumerRecords<String, HubEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

            if (!records.isEmpty()) {
                processRecords(records);
                consumer.commitSync();
            }
        }
    }

    private void closeResources() {
        try {
            consumer.commitSync();
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();
        }
    }

    private void processRecords(
            ConsumerRecords<String, HubEventAvro> records
    ) {
        for (ConsumerRecord<String, HubEventAvro> record : records) {
            if (record == null) {
                continue;
            }

            processRecord(record.value());
        }
    }

    private void processRecord(HubEventAvro eventAvro) {
        if (eventAvro == null) {
            return;
        }

        String hubId = eventAvro.getHubId();
        Object payload = eventAvro.getPayload();

        processEventAvro(payload, hubId);

    }

    private void processEventAvro(Object payload, String hubId) {
        switch (payload) {
            case DeviceAddedEventAvro deviceAddedEvent ->
                    processDeviceAddedEvent(hubId, deviceAddedEvent);
            case DeviceRemovedEventAvro deviceRemovedEvent ->
                    processDeviceRemovedEvent(hubId, deviceRemovedEvent);
            case ScenarioAddedEventAvro scenarioAddedEvent ->
                    processScenarioAddedEvent(hubId, scenarioAddedEvent);
            case ScenarioRemovedEventAvro scenarioRemovedEvent ->
                    processScenarioRemovedEvent(hubId, scenarioRemovedEvent);
            case null -> log.warn("Получен пустой payload для хаба {}", hubId);
            default -> log.error("Неизвестный тип события: {}", payload.getClass().getName());
        }
    }

    private void processDeviceAddedEvent(String hubId,DeviceAddedEventAvro event) {
        Sensor sensor = getSensor(hubId, event);
        sensorRepository.save(sensor);
    }

    private Sensor getSensor(String hubId, DeviceAddedEventAvro event) {
        return Sensor.builder()
                .id(event.getId())
                .hubId(hubId)
                .build();
    }

    private void processDeviceRemovedEvent(String hubId, DeviceRemovedEventAvro event) {
        Optional<Sensor> sensor = sensorRepository.findByIdAndHubId(event.getId(), hubId);
        sensor.ifPresent(sensorRepository::delete);
    }

    private void processScenarioAddedEvent(String hubId, ScenarioAddedEventAvro event) {
        Scenario scenario = getScenario(hubId,event);
        scenarioRepository.save(scenario);
    }

    private Scenario getScenario(String hubId, ScenarioAddedEventAvro event) {
        return Scenario.builder()
                .hubId(hubId)
                .name(event.getName())
                .actions(getActions(event))
                .conditions(getConditions(event))
                .build();
    }

    private Map<String, Action> getActions(ScenarioAddedEventAvro event) {
        return event.getActions().stream()
                .collect(Collectors.toMap(DeviceActionAvro::getSensorId, this::getAction));
    }

    private Action getAction(DeviceActionAvro deviceActionAvro) {
        return Action.builder()
                .type(getActionType(deviceActionAvro))
                .value(deviceActionAvro.getValue())
                .build();
    }

    private ActionType getActionType(DeviceActionAvro deviceActionAvro) {
        return ActionType.valueOf(deviceActionAvro.getType().name());
    }

    private Map<String, Condition> getConditions(ScenarioAddedEventAvro event) {
        return event.getConditions().stream()
                .collect(Collectors.toMap(
                        ScenarioConditionAvro::getSensorId, this::getCondition)
                );
    }

    private Condition getCondition(ScenarioConditionAvro event) {
        Integer value = switch (event.getValue()) {
            case Integer val -> val;
            case Boolean val -> val ? 1 : 0;
            case null, default -> null;
        };

        return Condition.builder()
                .type(getConditionType(event))
                .operation(getConditionOperation(event))
                .value(value)
                .build();
    }

    private ConditionOperation getConditionOperation(ScenarioConditionAvro condition) {
        return ConditionOperation.valueOf(condition.getOperation().name());
    }

    private ConditionType getConditionType(ScenarioConditionAvro condition) {
        return ConditionType.valueOf(condition.getType().name());
    }

    private void processScenarioRemovedEvent(String hubId, ScenarioRemovedEventAvro event) {
        Optional<Scenario> scenario = scenarioRepository.findByHubIdAndName(hubId, event.getName());
        scenario.ifPresent(scenarioRepository::delete);
    }
}
