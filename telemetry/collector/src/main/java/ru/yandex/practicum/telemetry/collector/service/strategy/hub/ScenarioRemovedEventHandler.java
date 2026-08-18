package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "SCENARIO_REMOVED")
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(HubEventProto hubEvent, String topic) {
        ScenarioRemovedEventProto event = hubEvent.getScenarioRemoved();

        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();

        HubEventAvro message = createHubEventAvro(hubEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(),message);
    }
}
