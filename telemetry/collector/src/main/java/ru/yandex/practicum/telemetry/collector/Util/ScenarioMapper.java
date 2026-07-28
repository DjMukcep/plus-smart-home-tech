package Util;

import lombok.experimental.UtilityClass;
import model.hub_event.DeviceAction;
import model.hub_event.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@UtilityClass
public class ScenarioMapper {

    public static ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition scenarioCondition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(scenarioCondition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(scenarioCondition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name()))
                .setValue(scenarioCondition.getValue())
                .build();
    }

    public static DeviceActionAvro toDeviceActionAvro(DeviceAction deviceAction) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceAction.getSensorId())
                .setType(ActionTypeAvro.valueOf(deviceAction.getType().name()))
                .setValue(deviceAction.getValue())
                .build();
    }

    public static List<ScenarioConditionAvro> toScenarioConditionAvro(List<ScenarioCondition> scenarioConditions) {
        return scenarioConditions.stream()
                .map(ScenarioMapper::toScenarioConditionAvro)
                .toList();
    }

    public static List<DeviceActionAvro> toDeviceActionAvro(List<DeviceAction> deviceActions) {
        return deviceActions.stream()
                .map(ScenarioMapper::toDeviceActionAvro)
                .toList();
    }
}
