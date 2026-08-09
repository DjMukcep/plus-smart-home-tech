package ru.yandex.practicum.telemetry.collector.service.strategy.hub;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.service.strategy.KafkaEventSender;

@Component(value = "DEVICE_REMOVED")
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {

    private final KafkaEventSender eventSender;

    @Override
    public void handle(HubEventProto hubEvent, String topic) {
        DeviceRemovedEventProto event = hubEvent.getDeviceRemoved();

        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();

        HubEventAvro message = createHubEventAvro(hubEvent,payload);
        eventSender.sendMessage(topic, message.getHubId(), message.getTimestamp(),message);
    }
}
