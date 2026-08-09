package ru.yandex.practicum.telemetry.collector.util;

import lombok.experimental.UtilityClass;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@UtilityClass
public class ScenarioMapper {

    public static ScenarioConditionAvro toScenarioConditionAvro(ScenarioConditionProto scenarioCondition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(scenarioCondition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(scenarioCondition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name()));

        switch (scenarioCondition.getValueCase()) {
            case VALUE_NOT_SET -> builder.setValue(null);
            case INT_VALUE -> builder.setValue(scenarioCondition.getIntValue());
            case BOOL_VALUE -> builder.setValue(scenarioCondition.getBoolValue());
        }

        return builder.build();
    }

    public static DeviceActionAvro toDeviceActionAvro(DeviceActionProto deviceAction) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceAction.getSensorId())
                .setType(ActionTypeAvro.valueOf(deviceAction.getType().name()))
                .setValue(deviceAction.getValue())
                .build();
    }

    public static List<ScenarioConditionAvro> toScenarioConditionAvro(List<ScenarioConditionProto> scenarioConditions) {
        return scenarioConditions.stream()
                .map(ScenarioMapper::toScenarioConditionAvro)
                .toList();
    }

    public static List<DeviceActionAvro> toDeviceActionAvro(List<DeviceActionProto> deviceActions) {
        return deviceActions.stream()
                .map(ScenarioMapper::toDeviceActionAvro)
                .toList();
    }
}
