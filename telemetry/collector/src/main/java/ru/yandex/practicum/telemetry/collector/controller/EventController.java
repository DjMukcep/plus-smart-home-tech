package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.telemetry.collector.model.hub_event.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor_event.SensorEvent;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.telemetry.collector.service.EventService;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/sensors")
    public  void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        eventService.publishSensorEvent(event);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        eventService.publishHubEvent(event);
    }
}
