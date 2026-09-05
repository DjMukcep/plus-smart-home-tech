package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.DuplicateException;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;


@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultInventoryService implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public Inventory addInventoryRecord(ReserveRequest request) {
        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateException(String.format("Product with id %s already exists", request.productId()));
        }
        Inventory record = InventoryMapper.toEntity(request);
        record = inventoryRepository.save(record);
        log.info("Add inventory record: {}", record);

        return record;
    }

    @Override
    @Transactional
    public Inventory updateInventoryRecord(UpdateInventoryRequest request) {
        Inventory record = getInventoryRecordByProductId(request.productId());

        if (request.quantity() < record.getReservedQuantity()) {
            throw new InsufficientStockException("New quantity cannot be less than the reserved quantity");
        }

        record.setQuantity(request.quantity());
        log.info("Update inventory record: {}", record);

        return record;
    }

    @Override
    @Transactional
    public ReserveResponse reserveInventoryRecord(ReserveRequest request) {
        Inventory record = getInventoryRecordByProductId(request.productId());

        int requestQuantity = request.quantity();
        int recQuantity = record.getQuantity();
        int recReservedQuantity = record.getReservedQuantity();
        int recAvailableQuantity = recQuantity - recReservedQuantity;

        if(recAvailableQuantity >= requestQuantity) {
            record.setReservedQuantity(recReservedQuantity + requestQuantity);
            recAvailableQuantity = recQuantity - record.getReservedQuantity();
            log.info("Reserve inventory record: {}", record);

            return new ReserveResponse(
                    true,recAvailableQuantity,"Товар успешно зарезервирован");
        }

        throw new InsufficientStockException("Not enough stock");
    }

    @Override
    public Inventory getInventoryRecordByProductId(Long id) {
        return inventoryRepository.findByProductId(id)
                .orElseThrow(() -> new NotFoundException("Inventory record not found!"));
    }

    @Override
    public List<Inventory> getInventoryRecords() {
        return inventoryRepository.findAll();
    }
}
