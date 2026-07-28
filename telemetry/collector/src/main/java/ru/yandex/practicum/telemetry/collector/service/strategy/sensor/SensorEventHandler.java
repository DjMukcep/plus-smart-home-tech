package ru.yandex.practicum.telemetry.collector.service.strategy;

import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEventType;

public interface SensorEventHandler {
    SensorEventType getSensorType();
    void  handle(SensorEvent sensorEvent);
    
}
