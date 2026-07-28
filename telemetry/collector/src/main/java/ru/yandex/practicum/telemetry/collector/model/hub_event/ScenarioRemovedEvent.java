package ru.yandex.practicum.telemetry.collector.model.hub_event;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {

    @Size(min = 3)
    private String name;

    @Override
    public HubEventType geType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}
