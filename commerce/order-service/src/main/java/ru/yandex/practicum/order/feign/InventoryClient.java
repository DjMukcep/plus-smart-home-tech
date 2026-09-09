package ru.yandex.practicum.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.order.dto.proxy.inventory.ReserveRequest;
import ru.yandex.practicum.order.service.fallback.InventoryClientFallbackFactory;

@FeignClient(name = "inventory-service", fallbackFactory = InventoryClientFallbackFactory.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/reserve")
    void reserveStock(@RequestBody ReserveRequest request);

    @PostMapping("/api/inventory/release")
    void releaseStock(@RequestBody ReserveRequest request);
}
