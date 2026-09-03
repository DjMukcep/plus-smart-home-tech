package ru.yandex.practicum.product.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;

import java.util.List;

@UtilityClass
public class ProductMapper {

    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                CategoryMapper.toDto(product.getCategory()),
                product.getImageUrl(),
                product.isActive()
        );
    }

    public static List<ProductDto> toDtos(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    public static Product toEntity(CreateProductRequest request, Category category) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(category)
                .imageUrl(request.imageUrl())
                .build();
    }
}
