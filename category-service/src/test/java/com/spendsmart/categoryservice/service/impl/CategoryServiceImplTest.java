package com.spendsmart.categoryservice.service.impl;

import com.spendsmart.categoryservice.domain.Category;
import com.spendsmart.categoryservice.enums.CategoryType;
import com.spendsmart.categoryservice.repository.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategoryPersistsCategory() {
        Category category = new Category();
        category.setUserId(7L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setIcon("restaurant");
        category.setColorCode("#EF4444");
        category.setDefault(false);

        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Food")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setCategoryId(8L);
            return saved;
        });

        Category saved = categoryService.createCategory(category);

        assertThat(saved.getCategoryId()).isEqualTo(8L);
        assertThat(saved.getName()).isEqualTo("Food");
    }

    @Test
    void initDefaultCategoriesCreatesMissingSeeds() {
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Food")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Transport")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Shopping")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Bills")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Health")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Entertainment")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Salary")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCase(7L, "Investment")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Category> created = categoryService.initDefaultCategories(7L);

        assertThat(created).hasSize(8);
        assertThat(created).extracting(Category::getType).contains(CategoryType.EXPENSE, CategoryType.INCOME);
    }

    @Test
    void getCategoryByIdThrowsWhenMissing() {
        when(categoryRepository.findByCategoryId(99L)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void setCategoryBudgetUpdatesLimit() {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setUserId(7L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setBudgetLimit(BigDecimal.ZERO);

        when(categoryRepository.findByCategoryId(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category saved = categoryService.setCategoryBudget(1L, new BigDecimal("2500"));

        assertThat(saved.getBudgetLimit()).isEqualByComparingTo("2500");
    }
}
