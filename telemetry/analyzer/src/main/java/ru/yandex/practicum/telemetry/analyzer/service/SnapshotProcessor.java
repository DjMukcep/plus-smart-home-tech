package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.repository.action.Action;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.Condition;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.repository.scenario.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.scenario.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.service.AnalyzerClient;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final Consumer<String, SensorsSnapshotAvro> consumer;
    private final KafkaTopicsProperties topics;
    private final ScenarioRepository scenarioRepository;
    private final AnalyzerClient analyzerClient;
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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private void subscribe() {
        consumer.subscribe(Collections.singletonList(topics.snapshots()));
        log.info("Успешная подписка на топик: {}", topics.snapshots());
    }

    private void consumeEvents() {
        while (true) {
            ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

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

    private void processRecords(ConsumerRecords<String, SensorsSnapshotAvro> records) {
        for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
            if (record == null) {
                continue;
            }

            processRecord(record.value());
        }
    }

    private void processRecord(SensorsSnapshotAvro eventAvro) {
        if (eventAvro == null) {
            return;
        }

        String hubId = eventAvro.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            return;
        }

        for (Scenario scenario : scenarios) {
            Map<String, Condition> conditions = scenario.getConditions();

            if (conditions == null || conditions.isEmpty()) {
                continue;
            }

            boolean allConditionsMatched = true;

            for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }

                String sensorId = entry.getKey();
                Condition condition = entry.getValue();

                SensorStateAvro sensorStateAvro = eventAvro.getSensorsState().get(sensorId);

                if (sensorStateAvro == null || sensorStateAvro.getData() == null) {
                    continue;
                }

                boolean matched = processSensor(sensorStateAvro.getData(), condition);

                if (!matched) {
                    allConditionsMatched = false;
                    break;
                }
            }

            if (allConditionsMatched) {
                Map<String, Action> actions = scenario.getActions();
                if (actions == null || actions.isEmpty()) {
                    continue;
                }
                for (Map.Entry<String, Action> entry : actions.entrySet()) {
                    String sensorId = entry.getKey();
                    Action action = entry.getValue();
                    String scenarioName = scenario.getName();
                    analyzerClient.sendActionToHub(hubId, scenarioName, sensorId, action);
                }
            }
        }

    }

    private boolean processSensor(Object data, Condition condition) {
        return switch (data) {
            case ClimateSensorAvro climate -> processClimateSensor(climate, condition);
            case TemperatureSensorAvro temperature -> processTemperatureSensor(temperature, condition);
            case LightSensorAvro light -> processLightSensor(light, condition);
            case MotionSensorAvro motion -> processMotionSensor(motion, condition);
            case SwitchSensorAvro switchSensor -> processSwitchSensor(switchSensor, condition);
            case null, default -> false;
        };
    }

    private boolean processClimateSensor(ClimateSensorAvro eventAvro, Condition condition) {
        int co2Level = eventAvro.getCo2Level();
        int humidity = eventAvro.getHumidity();
        int temperatureC = eventAvro.getTemperatureC();
        ConditionType conditionType = condition.getType();
        ConditionOperation operation = condition.getOperation();
        Integer conditionValue = condition.getValue();

        return switch (conditionType) {
            case HUMIDITY -> processCondition(conditionType, operation, conditionValue, humidity);
            case TEMPERATURE -> processCondition(conditionType, operation, conditionValue, temperatureC);
            case CO2LEVEL -> processCondition(conditionType, operation, conditionValue, co2Level);
            case MOTION, SWITCH, LUMINOSITY -> false;
        };
    }

    private boolean processLightSensor(LightSensorAvro eventAvro, Condition condition) {
        int luminosity = eventAvro.getLuminosity();
        ConditionType conditionType = condition.getType();
        ConditionOperation operation = condition.getOperation();
        Integer conditionValue = condition.getValue();

        return conditionType == LUMINOSITY
                && processCondition(conditionType, operation, conditionValue, luminosity);
    }

    private boolean processTemperatureSensor(TemperatureSensorAvro eventAvro, Condition condition) {
        int temperatureC = eventAvro.getTemperatureC();
        ConditionType conditionType = condition.getType();
        ConditionOperation operation = condition.getOperation();
        Integer conditionValue = condition.getValue();

        return conditionType == TEMPERATURE
                && processCondition(conditionType, operation, conditionValue, temperatureC);
    }

    private boolean processMotionSensor(MotionSensorAvro eventAvro, Condition condition) {
        boolean motion = eventAvro.getMotion();
        ConditionType conditionType = condition.getType();
        ConditionOperation operation = condition.getOperation();
        Integer conditionValue = condition.getValue();

        return conditionType == MOTION
                && processCondition(conditionType, operation, conditionValue, motion);
    }

    private boolean processSwitchSensor(SwitchSensorAvro eventAvro, Condition condition) {
        boolean state = eventAvro.getState();
        ConditionType conditionType = condition.getType();
        ConditionOperation operation = condition.getOperation();
        Integer conditionValue = condition.getValue();

        return conditionType == SWITCH
                && processCondition(conditionType, operation, conditionValue, state);
    }

    private boolean processCondition(
            ConditionType type, ConditionOperation operation, Integer targetValue, Object sensorValue) {
        return switch (type) {
            case MOTION,
                 SWITCH -> checkBooleanCondition(operation, sensorValue, targetValue);
            case TEMPERATURE,
                 LUMINOSITY,
                 HUMIDITY,
                 CO2LEVEL -> checkIntegerCondition(operation, sensorValue, targetValue);
        };
    }

    private boolean checkIntegerCondition(ConditionOperation operation, Object sensorValue, Integer targetValue) {
        Integer sensorValueInt = sensorValue instanceof Number n ? n.intValue() : null;

        if (sensorValueInt == null || targetValue == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> sensorValueInt.equals(targetValue);
            case GREATER_THAN -> sensorValueInt > targetValue;
            case LOWER_THAN -> sensorValueInt < targetValue;
        };
    }

    private boolean checkBooleanCondition(ConditionOperation operation, Object sensorValue, Integer targetValue) {
        Boolean sensorValueBoolean = sensorValue instanceof Boolean ? (Boolean) sensorValue : null;
        Boolean targetValueBoolean  = targetValue == null ? null : targetValue == 1;

        if (sensorValueBoolean == null || targetValueBoolean == null) {
            return false;
        }

        return operation == ConditionOperation.EQUALS && sensorValueBoolean.equals(targetValueBoolean);
    }
}
