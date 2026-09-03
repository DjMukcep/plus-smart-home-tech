package ru.yandex.practicum.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.order.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
