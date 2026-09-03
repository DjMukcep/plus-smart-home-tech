package ru.yandex.practicum.product.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.product.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> getProductsByCategoryId(Long categoryId);

    @NonNull
    @EntityGraph(attributePaths = "category")
    List<Product> findAll();


    @EntityGraph(attributePaths = "category")
    List<Product> findAllByNameContainsIgnoreCase(String name);
}
