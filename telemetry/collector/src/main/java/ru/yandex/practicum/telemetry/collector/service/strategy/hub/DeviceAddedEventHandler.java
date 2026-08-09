package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "DEVICE_ADDED")
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(HubEventProto hubEvent, String topic) {
        DeviceAddedEventProto event = hubEvent.getDeviceAdded();

        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(DeviceTypeAvro.valueOf(event.getType().name()))
                .build();

        HubEventAvro message = createHubEventAvro(hubEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(),message);
    }
}
