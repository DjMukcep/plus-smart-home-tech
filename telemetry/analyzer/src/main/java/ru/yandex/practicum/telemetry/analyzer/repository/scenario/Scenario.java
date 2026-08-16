package ru.yandex.practicum.telemetry.analyzer.repository.scenario;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.telemetry.analyzer.repository.action.Action;
import ru.yandex.practicum.telemetry.analyzer.repository.condition.Condition;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString
@Table(name = "scenarios",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hub_id","name"})
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "hub_id")
    private String hubId;

    @Column(nullable = false)
    private String name;

    // Связь с условиями: Датчик -> Условие
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(table = "scenario_conditions", name = "sensor_id")
    @JoinTable(name = "scenario_conditions",
    joinColumns = @JoinColumn(name = "scenario_id"),
    inverseJoinColumns = @JoinColumn(name = "condition_id"))
    @Builder.Default
    private Map<String, Condition> conditions = new HashMap<>();

    // Связь с действиями: Датчик -> Действие
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(table = "scenario_actions", name = "sensor_id")
    @JoinTable(name = "scenario_actions",
    joinColumns = @JoinColumn(name = "scenario_id"),
    inverseJoinColumns = @JoinColumn(name = "action_id"))
    @Builder.Default
    private Map<String, Action> actions = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scenario)) return false;
        return id != null && id.equals(((Scenario) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
