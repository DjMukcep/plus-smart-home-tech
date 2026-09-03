package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto addOrder(@RequestBody @Valid CreateOrderRequest request) {
        return OrderMapper.toOrderDto(orderService.createOrder(request));
    }

    @GetMapping(path = "/{id}")
    public OrderDto getOrderById(@PathVariable @Positive Long id) {
        return OrderMapper.toOrderDto(orderService.getOrderById(id));
    }

    @GetMapping
    public List<OrderDto> getOrders() {
        return OrderMapper.toOrderDtos(orderService.getAllOrders());
    }

    @GetMapping(path = "/by-email")
    public List<OrderDto> getOrdersByEmail(@RequestParam("email") String email) {
        return OrderMapper.toOrderDtos(orderService.getOrdersByEmail(email));
    }
}
