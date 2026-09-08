package ru.yandex.practicum.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.order.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
}
