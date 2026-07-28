package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.service.strategy.Event;

@Component
public class SwitchSensorEventHandler extends Event implements SensorEventHandler {

    public SwitchSensorEventHandler(Producer<String, SpecificRecordBase> producer) {
        super(producer);
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent, String topic) {
        SwitchSensorEvent event = (SwitchSensorEvent) sensorEvent;
        SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                .setState(event.getState())
                .build();
        sendMessage(topic,createSensorEvent(event,payload));
    }
}
