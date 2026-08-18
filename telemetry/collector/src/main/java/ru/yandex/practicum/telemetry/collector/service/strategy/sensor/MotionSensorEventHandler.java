package ru.yandex.practicum.telemetry.collector.service.strategy.sensor;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "MOTION_SENSOR_EVENT")
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(SensorEventProto sensorEvent, String topic) {
        MotionSensorProto event = sensorEvent.getMotionSensorEvent();

        MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.getMotion())
                .setVoltage(event.getVoltage())
                .build();

        SensorEventAvro message = createSensorEvent(sensorEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(), message);
    }
}
