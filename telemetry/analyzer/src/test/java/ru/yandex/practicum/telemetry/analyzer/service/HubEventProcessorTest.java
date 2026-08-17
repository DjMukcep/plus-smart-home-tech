package ru.yandex.practicum.telemetry.analyzer.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.AnalyzerApp;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.repository.action.Action;
import ru.yandex.practicum.telemetry.analyzer.repository.action.ActionType;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.Condition;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.repository.scenario.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.scenario.ScenarioRepository;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AnalyzerApp.class)
@Transactional
class HubEventProcessorTest {

    @MockBean
    private Consumer<String, HubEventAvro> consumer;

    @MockBean
    private KafkaTopicsProperties topics;

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private HubEventProcessor hubEventProcessor;


    @Test
    void shouldUpdateExistingScenario() {
        String hubId = "hub-1";

        Scenario oldScenario = Scenario.builder()
                .hubId(hubId)
                .name("scenario-1")
                .conditions(new HashMap<>(Map.of(
                        "sensor-1",
                        Condition.builder()
                                .type(ConditionType.TEMPERATURE)
                                .operation(ConditionOperation.GREATER_THAN)
                                .value(20)
                                .build()
                )))
                .actions(new HashMap<>(Map.of(
                        "sensor-2",
                        Action.builder()
                                .type(ActionType.ACTIVATE)
                                .build()
                )))
                .build();

        oldScenario = scenarioRepository.saveAndFlush(oldScenario);

        Long oldScenarioId = oldScenario.getId();

        // новое событие с тем же hubId + name,
        // но с другими condition/action
        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
                .setName("scenario-1")
                .setConditions(List.of(
                        ScenarioConditionAvro.newBuilder()
                                .setSensorId("sensor-3")
                                .setType(ConditionTypeAvro.LUMINOSITY)
                                .setOperation(ConditionOperationAvro.LOWER_THAN)
                                .setValue(10)
                                .build()
                ))
                .setActions(List.of(
                        DeviceActionAvro.newBuilder()
                                .setSensorId("sensor-4")
                                .setType(ActionTypeAvro.DEACTIVATE)
                                .build()
                ))
                .build();

        hubEventProcessor.processScenarioAddedEvent(hubId, event);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        // второго сценария не появилось
        assertThat(scenarios).hasSize(1);

        Scenario scenario = scenarios.getFirst();

        // используется тот же Scenario
        assertThat(scenario.getId()).isEqualTo(oldScenarioId);
        assertThat(scenario.getHubId()).isEqualTo(hubId);
        assertThat(scenario.getName()).isEqualTo("scenario-1");

        // старое условие заменилось новым
        assertThat(scenario.getConditions()).containsKey("sensor-3");
        assertThat(scenario.getConditions()).doesNotContainKey("sensor-1");

        Condition condition = scenario.getConditions().get("sensor-3");

        assertThat(condition.getType()).isEqualTo(ConditionType.LUMINOSITY);
        assertThat(condition.getOperation()).isEqualTo(ConditionOperation.LOWER_THAN);
        assertThat(condition.getValue()).isEqualTo(10);

        // старое действие заменилось новым
        assertThat(scenario.getActions()).containsKey("sensor-4");
        assertThat(scenario.getActions()).doesNotContainKey("sensor-2");

        Action action = scenario.getActions().get("sensor-4");

        assertThat(action.getType()).isEqualTo(ActionType.DEACTIVATE);
    }


    @Test
    void shouldCreateNewScenarioWhenItDoesNotExist() {
        String hubId = "hub-1";

        ScenarioAddedEventAvro event = ScenarioAddedEventAvro.newBuilder()
                .setName("new-scenario")
                .setConditions(List.of(
                        ScenarioConditionAvro.newBuilder()
                                .setSensorId("sensor-1")
                                .setType(ConditionTypeAvro.TEMPERATURE)
                                .setOperation(ConditionOperationAvro.GREATER_THAN)
                                .setValue(25)
                                .build()
                ))
                .setActions(List.of(
                        DeviceActionAvro.newBuilder()
                                .setSensorId("sensor-2")
                                .setType(ActionTypeAvro.ACTIVATE)
                                .build()
                ))
                .build();

        hubEventProcessor.processScenarioAddedEvent(hubId, event);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        assertThat(scenarios).hasSize(1);

        Scenario scenario = scenarios.getFirst();

        assertThat(scenario.getId()).isNotNull();
        assertThat(scenario.getHubId()).isEqualTo(hubId);
        assertThat(scenario.getName()).isEqualTo("new-scenario");
        assertThat(scenario.getConditions()).containsKey("sensor-1");
        assertThat(scenario.getActions()).containsKey("sensor-2");
    }

}