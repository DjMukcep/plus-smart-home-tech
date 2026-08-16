package ru.yandex.practicum.telemetry.analyzer.repository.condition;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "conditions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
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
