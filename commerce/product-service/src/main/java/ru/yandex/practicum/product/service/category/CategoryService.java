package ru.yandex.practicum.product.service.category;

import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(CreateCategoryRequest request);

    List<Category> getCategories();

    Category getCategory(Long categoryId);
}
