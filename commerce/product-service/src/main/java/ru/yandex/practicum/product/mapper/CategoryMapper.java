package ru.yandex.practicum.product.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;

import java.util.List;

@UtilityClass
public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName(), category.getDescription());
    }

    public static List<CategoryDto> toDtos(List<Category> categories) {
        return categories.stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    public static Category toEntity(CreateCategoryRequest request) {
        return Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }
}
