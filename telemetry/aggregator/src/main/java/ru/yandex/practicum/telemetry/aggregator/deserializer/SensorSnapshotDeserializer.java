package ru.yandex.practicum.telemetry.aggregator.deserializer;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SensorSnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro>{
    public SensorSnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}
