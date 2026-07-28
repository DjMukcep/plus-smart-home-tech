package ru.yandex.practicum.telemetry.collector.service.strategy.Hub;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub_event.ScenarioRemovedEvent;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

@Component
public class ScenarioRemovedEventHandler extends Event implements HubEventHandler {

    public ScenarioRemovedEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEvent hubEvent, String topic) {
        ScenarioRemovedEvent event = (ScenarioRemovedEvent) hubEvent;
        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
        sendMessage(topic, createHubEventAvro(event,payload));
    }
}
