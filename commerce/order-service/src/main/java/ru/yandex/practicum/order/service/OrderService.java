package ru.yandex.practicum.order.service;

import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    Order saveOrder(CreateOrderRequest request, OrderStatus status);

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    List<Order> getOrdersByEmail(String email);
}
