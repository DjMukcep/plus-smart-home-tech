package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import org.apache.avro.specific.SpecificRecordBase;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

public interface HubEventHandler {

    void handle(HubEventProto hubEvent, String topic);

    default <T extends HubEventProto> HubEventAvro createHubEventAvro(T event, SpecificRecordBase payload) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                ))
                .setPayload(payload)
                .build();
    }
}
