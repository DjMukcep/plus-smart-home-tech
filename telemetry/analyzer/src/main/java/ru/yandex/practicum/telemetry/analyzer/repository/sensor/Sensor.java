package ru.yandex.practicum.telemetry.analyzer.repository.sensor;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "sensors")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    @Id
    private String id;

    @Column(nullable = false, name = "hub_id")
    private String hubId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sensor)) return false;
        return id != null && id.equals(((Sensor) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
