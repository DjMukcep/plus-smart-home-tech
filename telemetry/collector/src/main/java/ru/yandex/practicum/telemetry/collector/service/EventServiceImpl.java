package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.hub_event.*;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.*;
import ru.yandex.practicum.telemetry.collector.service.strategy.Hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.strategy.sensor.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class EventServiceImpl implements EventService {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventServiceImpl(List<SensorEventHandler> sensorHandlers,
                            List<HubEventHandler> hubHandlers) {
        this.sensorEventHandlers = initEventHandlers(
                sensorHandlers,SensorEventHandler::getSensorEventType);
        this.hubEventHandlers = initEventHandlers(
                hubHandlers,HubEventHandler::getHubEventType);
    }

    @Override
    public void publishSensorEvent(SensorEvent event) {
        String topic = "telemetry.sensors.v1";
        sensorEventHandlers.get(event.getType()).handle(event, topic);
    }

    @Override
    public void publishHubEvent(HubEvent event) {
        String topic = "telemetry.hubs.v1";
        hubEventHandlers.get(event.geType()).handle(event, topic);
    }

    private <K, H> Map<K, H> initEventHandlers(List<H> handlers, Function<H, K> keyExtractor) {
        return handlers.stream()
                .collect(Collectors.toMap(
                        keyExtractor,
                        Function.identity()
                ));
    }
}
