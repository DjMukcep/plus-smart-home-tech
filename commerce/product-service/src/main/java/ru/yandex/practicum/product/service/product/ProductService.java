package ru.yandex.practicum.product.service.product;

import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Product;

import java.util.List;

public interface ProductService {

    Product createProduct(CreateProductRequest request);
    Product updateProduct(UpdateProductRequest request, Long id);
    List<Product> getProducts();
    Product getProduct(Long id);
    List<Product> getProductsByCategoryId(Long id);
    List<Product> searchByName(String name);
}
