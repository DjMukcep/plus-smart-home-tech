package ru.yandex.practicum.order.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.order.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"customer", "items"})
    Optional<Order> findWithCustomerAndItemsById(Long customerId);

    @NonNull
    @EntityGraph(attributePaths = {"customer", "items"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"customer", "items"})
    List<Order> findAllByCustomerEmail(String email);
}
