package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "LIGHT_SENSOR_EVENT")
@RequiredArgsConstructor
public class LightSensorEventHandler implements SensorEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(SensorEventProto sensorEvent, String topic) {
        LightSensorProto event = sensorEvent.getLightSensorEvent();

        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();

        SensorEventAvro message = createSensorEvent(sensorEvent, payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(), message);
    }
}
