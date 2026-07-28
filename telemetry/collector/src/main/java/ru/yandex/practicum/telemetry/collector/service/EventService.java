package service;

import model.hub_event.HubEvent;
import model.sensor_event.SensorEvent;

public interface EventService {
    void publishSensorEvent(SensorEvent event);
    void publishHubEvent(HubEvent event);
}
