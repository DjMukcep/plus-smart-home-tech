package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub_event.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEventType;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

import java.time.Instant;

@Component
public class DeviceAddedEventHandler extends Event implements HubEventHandler {

    public DeviceAddedEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEvent hubEvent, String topic) {
        DeviceAddedEvent event = (DeviceAddedEvent) hubEvent;

        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                .build();

        HubEventAvro message = createHubEventAvro(event,payload);
        sendMessage(topic, message.getHubId(), message.getTimestamp(),message);
    }
}
