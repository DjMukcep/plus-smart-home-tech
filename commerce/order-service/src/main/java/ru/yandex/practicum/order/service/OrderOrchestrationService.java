package ru.yandex.practicum.order.service;

import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.entity.Order;

public interface OrderOrchestrationService {

    Order createOrder(CreateOrderRequest request);
}
