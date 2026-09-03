package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.entity.Customer;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = saveCustomer(request);
        Order order = OrderMapper.toOrder(request, customer);

        order = orderRepository.save(order);

        List<OrderItem> items = OrderMapper.toOrderItems(request, order);
        order.setItems(items);
        log.info("Create order: {}", order);

        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findWithCustomerAndItemsById(id).orElseThrow(
                () -> new NotFoundException(String.format("Order with id %s not found", id))
        );
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findAllByCustomerEmail(email);
    }

    private Customer saveCustomer(CreateOrderRequest request) {
        Customer customer = OrderMapper.toCustomer(request);

        return customerService.save(customer);
    }
}
