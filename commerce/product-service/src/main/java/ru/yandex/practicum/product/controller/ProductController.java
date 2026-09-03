package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.service.product.ProductService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto addProduct(@RequestBody @Valid CreateProductRequest request) {
        return ProductMapper.toDto(productService.createProduct(request));
    }

    @PatchMapping(path = "/{id}")
    public ProductDto updateProduct(@RequestBody @Valid UpdateProductRequest request, @PathVariable Long id) {
        return ProductMapper.toDto(productService.updateProduct(request,id));
    }

    @GetMapping
    public List<ProductDto> getProducts() {
        return ProductMapper.toDtos(productService.getProducts());
    }

    @GetMapping(path = "/{id}")
    public ProductDto getProduct(@PathVariable @Positive Long id) {
        return ProductMapper.toDto(productService.getProduct(id));
    }

    @GetMapping(path = "/category/{categoryId}")
    public List<ProductDto> getProductsByCategoryId(@PathVariable @Positive Long categoryId) {
        return ProductMapper.toDtos(productService.getProductsByCategoryId(categoryId));
    }

    @GetMapping(path = "/search")
    public List<ProductDto> searchByName(@RequestParam("query") @NotBlank String query) {
        return ProductMapper.toDtos(productService.searchByName(query));
    }
}
