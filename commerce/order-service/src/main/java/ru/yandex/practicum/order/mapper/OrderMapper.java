package ru.yandex.practicum.order.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.entity.Customer;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@UtilityClass
public class OrderMapper {

    public static Order toOrder(Customer customer) {
        return Order.builder()
                .customer(customer)
                .status(OrderStatus.CREATED)
                .statusDetails("string")
                .build();
    }

    public static List<OrderItem> toOrderItems(CreateOrderRequest request, Order order) {
        return request.items().stream()
                .map(itemRequest -> OrderItem.builder()
                        .order(order)
                        .productId(itemRequest.productId())
                        .productName(itemRequest.productName())
                        .quantity(itemRequest.quantity())
                        .price(itemRequest.price())
                        .build())
                .toList();
    }

    public static Customer toCustomer(CreateOrderRequest request) {
        return Customer.builder()
                .name(request.customerName())
                .email(request.customerEmail())
                .build();
    }

    public static OrderDto toOrderDto(Order order) {
        BigDecimal totalPrice = order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(orderItem -> new OrderItemDto(
                        orderItem.getId(),
                        orderItem.getProductId(),
                        orderItem.getProductName(),
                        orderItem.getQuantity(),
                        orderItem.getPrice()
                ))
                .toList();

        return new OrderDto(
                order.getId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getStatus().name(),
                totalPrice,
                order.getStatusDetails(),
                order.getCreatedAt(),
                itemDtos
        );
    }

    public static List<OrderDto> toOrderDtos(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toOrderDto)
                .toList();
    }
}
