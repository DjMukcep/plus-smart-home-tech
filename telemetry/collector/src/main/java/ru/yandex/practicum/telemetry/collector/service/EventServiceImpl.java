package service;

import Util.ScenarioMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import model.hub_event.*;
import model.sensor_event.*;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.kafka.telemetry.event.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final Producer<String, SpecificRecordBase> producer;

    @Override
    public void publishSensorEvent(SensorEvent event) {
        String topic = "telemetry.sensors.v1";
        switch (event.getType()) {
            case LIGHT_SENSOR_EVENT ->  {
                LightSensorEvent sensorEvent = (LightSensorEvent) event;
                processLightSensorEvent(sensorEvent,topic);
            }
            case MOTION_SENSOR_EVENT ->  {
                MotionSensorEvent motionEvent = (MotionSensorEvent) event;
                processMotionSensorEvent(motionEvent,topic);
            }
            case CLIMATE_SENSOR_EVENT ->  {
                ClimateSensorEvent climateEvent = (ClimateSensorEvent) event;
                processClimateSensorEvent(climateEvent,topic);
            }
            case TEMPERATURE_SENSOR_EVENT ->  {
                TemperatureSensorEvent temperatureEvent = (TemperatureSensorEvent) event;
                processTemperatureSensorEvent(temperatureEvent, topic);
            }
            case SWITCH_SENSOR_EVENT ->   {
                SwitchSensorEvent switchEvent = (SwitchSensorEvent) event;
                processSwitchSensorEvent(switchEvent, topic);
            }
        }
    }

    @Override
    public void publishHubEvent(HubEvent event) {
        String topic = "telemetry.hubs.v1";
        switch (event.geType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) event;
                processDeviceAddedEvent(deviceAddedEvent,topic);
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) event;
                processDeviceRemovedEvent(deviceRemovedEvent,topic);
            }
            case SCENARIO_ADDED ->  {
                ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) event;
                processScenarioAddedEvent(scenarioAddedEvent,topic);
            }
            case SCENARIO_REMOVED ->  {
                ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) event;
                processScenarioRemovedEvent(scenarioRemovedEvent,topic);
            }
        }
    }

    private void processLightSensorEvent(LightSensorEvent event, String topic) {
        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
        sendMessage(topic, createSensorEvent(event,payload));
    }

    private void processMotionSensorEvent(MotionSensorEvent event, String topic) {
        MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.getMotion())
                .setVoltage(event.getVoltage())
                .build();
        sendMessage(topic,createSensorEvent(event,payload));
    }

    private void processClimateSensorEvent(ClimateSensorEvent event, String topic) {
        ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Value())
                .build();
        sendMessage(topic,createSensorEvent(event,payload));
    }

    private void processTemperatureSensorEvent(TemperatureSensorEvent event, String topic) {
        TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
        sendMessage(topic,createSensorEvent(event,payload));
    }

    private void processSwitchSensorEvent(SwitchSensorEvent event, String topic) {
        SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                .setState(event.getState())
                .build();
        sendMessage(topic,createSensorEvent(event,payload));
    }

    private SensorEventAvro createSensorEvent(SensorEvent event, SpecificRecordBase payload) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private void processDeviceAddedEvent(DeviceAddedEvent event, String topic) {
        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                .build();
        sendMessage(topic, createHubEvent(event,payload));

    }

    private void processDeviceRemovedEvent(DeviceRemovedEvent event, String topic) {
        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
        sendMessage(topic, createHubEvent(event,payload));
    }

    private void processScenarioAddedEvent(ScenarioAddedEvent event, String topic) {
        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(ScenarioMapper.toScenarioConditionAvro(event.getConditions()))
                .setActions(ScenarioMapper.toDeviceActionAvro(event.getActions()))
                .build();
        sendMessage(topic, createHubEvent(event,payload));
    }

    private void processScenarioRemovedEvent(ScenarioRemovedEvent event, String topic) {
        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
        sendMessage(topic, createHubEvent(event,payload));
    }

    private HubEventAvro createHubEvent(HubEvent event, SpecificRecordBase payload) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private void sendMessage(String topic, SpecificRecordBase message) {
        producer.send(new ProducerRecord<>(topic, message), this::onMessageSent);
    }

    private void onMessageSent(RecordMetadata metadata, Exception ex) {
        if (ex != null) {
            log.error("Ошибка отправки сообщения в топик {}", metadata != null ? metadata.topic() : "unknown", ex);
        } else  {
            log.info("Сообщение отправлено. topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());
        }
    }
}
