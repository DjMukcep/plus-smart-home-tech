package ru.yandex.practicum.telemetry.collector.service.strategy.Hub;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub_event.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEventType;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

@Component
public class DeviceRemovedEventHandler extends Event implements HubEventHandler {

    public DeviceRemovedEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEvent hubEvent, String topic) {
        DeviceRemovedEvent event = (DeviceRemovedEvent) hubEvent;
        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
        sendMessage(topic, createHubEventAvro(event,payload));
    }
}
