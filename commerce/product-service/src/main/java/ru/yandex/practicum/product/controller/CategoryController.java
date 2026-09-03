package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.service.category.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CreateCategoryRequest request) {
        return CategoryMapper.toDto(categoryService.createCategory(request));
    }

    @GetMapping
    public List<CategoryDto> getCategories() {
        return CategoryMapper.toDtos(categoryService.getCategories());
    }

    @GetMapping(path = "/{id}")
    public CategoryDto getCategory(@PathVariable @Positive Long id) {
        return CategoryMapper.toDto(categoryService.getCategory(id));
    }
}
