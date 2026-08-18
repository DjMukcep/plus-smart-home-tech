package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "TEMPERATURE_SENSOR_EVENT")
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(SensorEventProto sensorEvent, String topic) {
        TemperatureSensorProto event = sensorEvent.getTemperatureSensorEvent();

        TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();

        SensorEventAvro message = createSensorEvent(sensorEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(), message);
    }
}
