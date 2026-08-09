package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.telemetry.collector.util.ScenarioMapper;
import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "SCENARIO_ADDED")
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(HubEventProto hubEvent, String topic) {
        ScenarioAddedEventProto event = hubEvent.getScenarioAdded();

        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(ScenarioMapper.toScenarioConditionAvro(event.getConditionList()))
                .setActions(ScenarioMapper.toDeviceActionAvro(event.getActionList()))
                .build();

        HubEventAvro message = createHubEventAvro(hubEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(),message);
    }
}
