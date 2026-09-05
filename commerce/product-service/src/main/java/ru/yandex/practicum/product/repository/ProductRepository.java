package ru.yandex.practicum.product.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.product.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> getProductsByCategoryIdAndIsActive(@NonNull Long categoryId, boolean active);

    @NonNull
    @EntityGraph(attributePaths = "category")
    List<Product> findAll();

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByIsActive(boolean isActive);

    @NonNull
    @EntityGraph(attributePaths = "category")
    Optional<Product> findById(@NonNull Long id);

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByNameContainsIgnoreCaseAndIsActive(@NonNull String name, boolean active);
}
