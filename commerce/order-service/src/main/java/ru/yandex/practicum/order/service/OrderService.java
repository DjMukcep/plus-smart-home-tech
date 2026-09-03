package ru.yandex.practicum.order.service;

import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.entity.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(CreateOrderRequest request);

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    List<Order> getOrdersByEmail(String email);
}
