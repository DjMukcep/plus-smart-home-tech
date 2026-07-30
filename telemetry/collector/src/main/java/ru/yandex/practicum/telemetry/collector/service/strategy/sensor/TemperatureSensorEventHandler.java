package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.TemperatureSensorEvent;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

@Component
public class TemperatureSensorEventHandler extends Event implements SensorEventHandler {

    public TemperatureSensorEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent, String topic) {
        TemperatureSensorEvent event = (TemperatureSensorEvent) sensorEvent;

        TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();

        SensorEventAvro message = createSensorEvent(event,payload);
        sendMessage(topic, message.getHubId(), message.getTimestamp(), message);
    }
}
