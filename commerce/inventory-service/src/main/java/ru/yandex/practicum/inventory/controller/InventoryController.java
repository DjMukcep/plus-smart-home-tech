package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/inventory")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto addInventoryRecord(@RequestBody @Valid ReserveRequest request) {
        return InventoryMapper.toDto(inventoryService.addInventoryRecord(request));
    }

    @PostMapping(path = "/reserve")
    public ReserveResponse reserveInventoryRecord(@RequestBody @Valid ReserveRequest request) {
        return inventoryService.reserveInventoryRecord(request);
    }

    @PutMapping
    public InventoryDto updateInventoryRecord(@RequestBody @Valid UpdateInventoryRequest request) {
        return InventoryMapper.toDto(inventoryService.updateInventoryRecord(request));
    }

    @GetMapping
    public List<InventoryDto> getInventoryRecords() {
        return InventoryMapper.toDto(inventoryService.getInventoryRecords());
    }

    @GetMapping(path = "/{productId}")
    public InventoryDto getInventoryRecordById(@PathVariable @Positive Long productId) {
        return InventoryMapper.toDto(inventoryService.getInventoryRecordByProductId(productId));
    }
}
