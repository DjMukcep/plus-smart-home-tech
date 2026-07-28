package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEventType;

public interface SensorEventHandler {

    SensorEventType getSensorEventType();

    void handle(SensorEvent sensorEvent, String topic);

    default SensorEventAvro createSensorEvent(SensorEvent event, SpecificRecordBase payload) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}
