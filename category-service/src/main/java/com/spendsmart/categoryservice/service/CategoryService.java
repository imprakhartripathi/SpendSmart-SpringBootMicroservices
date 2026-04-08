package com.spendsmart.categoryservice.service;

import com.spendsmart.categoryservice.domain.Category;
import com.spendsmart.categoryservice.enums.CategoryType;
import java.math.BigDecimal;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);

    List<Category> getByUserId(Long userId);

    Category getCategoryById(Long categoryId);

    List<Category> getByUserAndType(Long userId, CategoryType type);

    Category updateCategory(Long categoryId, Category category);

    void deleteCategory(Long categoryId);

    List<Category> getDefaultCategories();

    List<Category> initDefaultCategories(Long userId);

    Category setCategoryBudget(Long categoryId, BigDecimal budgetLimit);

    long getCategoryCount(Long userId);
}
