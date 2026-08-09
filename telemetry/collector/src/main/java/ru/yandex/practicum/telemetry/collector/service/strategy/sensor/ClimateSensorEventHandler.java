package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "CLIMATE_SENSOR_EVENT")
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(SensorEventProto sensorEvent, String topic) {
        ClimateSensorProto event = sensorEvent.getClimateSensorEvent();

        ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Level())
                .build();

        SensorEventAvro message = createSensorEvent(sensorEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(),  message.getTimestamp(), message);
    }
}
