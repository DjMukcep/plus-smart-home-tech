package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEventType;

public interface HubEventHandler {

    HubEventType getHubEventType();

    void handle(HubEvent hubEvent, String topic);

    default HubEventAvro createHubEventAvro(HubEvent event, SpecificRecordBase payload) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}
