package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;
import ru.yandex.practicum.telemetry.collector.model.hub_event.*;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.*;
import ru.yandex.practicum.telemetry.collector.service.strategy.hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.strategy.sensor.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class EventServiceImpl implements EventService {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;
    private final KafkaTopicsProperties topics;

    public EventServiceImpl(List<SensorEventHandler> sensorHandlers,
                            List<HubEventHandler> hubHandlers,
                            KafkaTopicsProperties topics) {
        this.sensorEventHandlers = initEventHandlers(
                sensorHandlers,SensorEventHandler::getSensorEventType);
        this.hubEventHandlers = initEventHandlers(
                hubHandlers,HubEventHandler::getHubEventType);
        this.topics = topics;
    }

    @Override
    public void publishSensorEvent(SensorEvent event) {
        sensorEventHandlers.get(event.getType()).handle(event, topics.sensors());
    }

    @Override
    public void publishHubEvent(HubEvent event) {
        hubEventHandlers.get(event.getType()).handle(event, topics.hubs());
    }

    private <K, H> Map<K, H> initEventHandlers(List<H> handlers, Function<H, K> keyExtractor) {
        return handlers.stream()
                .collect(Collectors.toMap(
                        keyExtractor,
                        Function.identity()
                ));
    }
}
