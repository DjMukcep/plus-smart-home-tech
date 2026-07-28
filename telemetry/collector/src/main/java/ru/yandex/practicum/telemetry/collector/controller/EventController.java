package controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import model.hub_event.HubEvent;
import model.sensor_event.SensorEvent;
import org.springframework.web.bind.annotation.*;
import service.EventService;

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
