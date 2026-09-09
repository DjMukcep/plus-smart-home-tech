package ru.yandex.practicum.order.service.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.proxy.inventory.ReserveRequest;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.feign.InventoryClient;

@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    private final static Logger log = LoggerFactory.getLogger(InventoryClientFallbackFactory.class);

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {

            @Override
            public void reserveStock(ReserveRequest request) {
                log.warn(
                        "inventory-service недоступен при резервировании товара id={}",
                        request.productId(),
                        cause
                );

                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }

            @Override
            public void releaseStock(ReserveRequest request) {
                log.warn(
                        "inventory-service недоступен при снятии резерва товара id={}",
                        request.productId(),
                        cause
                );

                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }
        };
    }


}
