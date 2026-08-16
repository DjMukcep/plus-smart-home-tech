package ru.yandex.practicum.telemetry.analyzer.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConditionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ConditionOperation operation;

    private Integer value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condition)) return false;
        return id != null && id.equals(((Condition) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
