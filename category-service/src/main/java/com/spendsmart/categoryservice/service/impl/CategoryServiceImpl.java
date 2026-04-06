package com.spendsmart.categoryservice.service.impl;

import com.spendsmart.categoryservice.domain.Category;
import com.spendsmart.categoryservice.enums.CategoryType;
import com.spendsmart.categoryservice.repository.CategoryRepository;
import com.spendsmart.categoryservice.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final List<DefaultCategorySeed> DEFAULT_CATEGORIES = List.of(
            new DefaultCategorySeed("Food", CategoryType.EXPENSE, "restaurant", "#EF4444"),
            new DefaultCategorySeed("Transport", CategoryType.EXPENSE, "bus", "#F97316"),
            new DefaultCategorySeed("Shopping", CategoryType.EXPENSE, "shopping_bag", "#8B5CF6"),
            new DefaultCategorySeed("Bills", CategoryType.EXPENSE, "receipt_long", "#0EA5E9"),
            new DefaultCategorySeed("Health", CategoryType.EXPENSE, "health_and_safety", "#10B981"),
            new DefaultCategorySeed("Entertainment", CategoryType.EXPENSE, "movie", "#EC4899"),
            new DefaultCategorySeed("Salary", CategoryType.INCOME, "payments", "#22C55E"),
            new DefaultCategorySeed("Investment", CategoryType.INCOME, "trending_up", "#3B82F6")
    );

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category) {
        categoryRepository.findByUserIdAndNameIgnoreCase(category.getUserId(), category.getName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category with same name already exists for this user");
                });
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getByUserId(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getByUserAndType(Long userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public Category updateCategory(Long categoryId, Category category) {
        Category existing = getCategoryById(categoryId);
        existing.setName(category.getName());
        existing.setType(category.getType());
        existing.setIcon(category.getIcon());
        existing.setColorCode(category.getColorCode());
        return categoryRepository.save(existing);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found");
        }
        categoryRepository.deleteByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getDefaultCategories() {
        return categoryRepository.findByIsDefault(true);
    }

    @Override
    public List<Category> initDefaultCategories(Long userId) {
        List<Category> created = new ArrayList<>();
        for (DefaultCategorySeed seed : DEFAULT_CATEGORIES) {
            if (categoryRepository.findByUserIdAndNameIgnoreCase(userId, seed.name()).isPresent()) {
                continue;
            }
            Category category = new Category();
            category.setUserId(userId);
            category.setName(seed.name());
            category.setType(seed.type());
            category.setIcon(seed.icon());
            category.setColorCode(seed.colorCode());
            category.setDefault(true);
            created.add(categoryRepository.save(category));
        }
        return created;
    }

    @Override
    public Category setCategoryBudget(Long categoryId, BigDecimal budgetLimit) {
        Category existing = getCategoryById(categoryId);
        existing.setBudgetLimit(budgetLimit);
        return categoryRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCategoryCount(Long userId) {
        return categoryRepository.countByUserId(userId);
    }

    private record DefaultCategorySeed(String name, CategoryType type, String icon, String colorCode) {
    }
}
