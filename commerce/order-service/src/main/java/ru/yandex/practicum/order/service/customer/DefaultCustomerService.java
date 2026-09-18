package ru.yandex.practicum.order.service.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.entity.Customer;
import ru.yandex.practicum.order.repository.CustomerRepository;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCustomerService implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer save(Customer customer) {
        return findByEmail(customer.getEmail())
                .orElseGet(() -> {
                    Customer savedCustomer = customerRepository.save(customer);
                    log.info("New customer: {}", savedCustomer);
                    return savedCustomer;
                });
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
}
