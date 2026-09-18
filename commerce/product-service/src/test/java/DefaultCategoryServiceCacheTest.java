import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.practicum.product.ProductServiceApp;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.service.category.CategoryService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ProductServiceApp.class)
class DefaultCategoryServiceCacheTest {

    @MockBean
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Test
    void shouldReturnCategoryFromCache() {
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category first = categoryService.getCategory(1L);
        Category second = categoryService.getCategory(1L);

        assertSame(first, second);

        verify(categoryRepository, times(1))
                .findById(1L);
    }
}
