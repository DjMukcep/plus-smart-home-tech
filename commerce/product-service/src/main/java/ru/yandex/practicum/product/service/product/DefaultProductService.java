package ru.yandex.practicum.product.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.repository.ProductRepository;
import ru.yandex.practicum.product.service.category.CategoryService;


import java.util.List;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        Category category = categoryService.getCategory(request.categoryId());
        Product product = ProductMapper.toEntity(request,category);
        product = productRepository.save(product);
        log.info("Created product {}", product);

        return product;
    }

    @Override
    @Transactional
    public Product updateProduct(UpdateProductRequest request, Long id) {
        Product product = getProduct(id);
        updateProductFromUpdateRequest(request, product);
        log.info("Updated product {}", product);

        return product;
    }

    @Override
    public List<Product> getProducts() {
        return productRepository.findAllByIsActive(true);
    }

    @Override
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id " + id));
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.getProductsByCategoryId(categoryId);
    }

    @Override
    public List<Product> searchByName(String name) {
        return productRepository.findAllByNameContainsIgnoreCase(name);
    }

    private void updateProductFromUpdateRequest(UpdateProductRequest request, Product product) {
        ofNullable(request.name()).ifPresent(product::setName);
        ofNullable(request.description()).ifPresent(product::setDescription);
        ofNullable(request.price()).ifPresent(product::setPrice);
        ofNullable(request.imageUrl()).ifPresent(product::setImageUrl);
        ofNullable(request.active()).ifPresent(product::setActive);
        ofNullable(request.categoryId()).ifPresent(
                newCategoryId -> product.setCategory(categoryService.getCategory(newCategoryId)));
    }
}
