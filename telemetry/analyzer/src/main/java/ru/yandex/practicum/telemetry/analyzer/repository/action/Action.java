package ru.yandex.practicum.telemetry.analyzer.repository.action;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "actions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType type;

    private Integer value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        return id != null && id.equals(((Action) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
