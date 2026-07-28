package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEventType;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

@Component
public class LightSensorEventHandler extends Event implements SensorEventHandler {

    public LightSensorEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent, String topic) {
        LightSensorEvent event = (LightSensorEvent) sensorEvent;
        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
        sendMessage(topic, createSensorEvent(event, payload));
    }
}
