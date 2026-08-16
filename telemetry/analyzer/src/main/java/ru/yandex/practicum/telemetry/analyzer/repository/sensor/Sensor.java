package ru.yandex.practicum.telemetry.analyzer.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
