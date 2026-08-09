package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

public interface SensorEventHandler {

    void handle(SensorEventProto sensorEvent, String topic);

    default <T extends SensorEventProto> SensorEventAvro createSensorEvent(T event, SpecificRecordBase payload) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos())
                )
                .setPayload(payload)
                .build();
    }
}
