package ru.yandex.practicum.telemetry.collector.service.strategy.Hub;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.telemetry.collector.Util.ScenarioMapper;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.hub_event.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

@Component
public class ScenarioAddedEventHandler extends Event implements HubEventHandler {

    public ScenarioAddedEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEvent hubEvent, String topic) {
        ScenarioAddedEvent event = (ScenarioAddedEvent) hubEvent;
        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(ScenarioMapper.toScenarioConditionAvro(event.getConditions()))
                .setActions(ScenarioMapper.toDeviceActionAvro(event.getActions()))
                .build();
        sendMessage(topic, createHubEventAvro(event,payload));
    }
}
