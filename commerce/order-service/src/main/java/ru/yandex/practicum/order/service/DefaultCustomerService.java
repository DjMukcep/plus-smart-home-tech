package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.entity.Customer;
import ru.yandex.practicum.order.repository.CustomerRepository;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultCustomerService implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer save(Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        log.info("New customer: {}", savedCustomer);

        return savedCustomer;
    }
}
