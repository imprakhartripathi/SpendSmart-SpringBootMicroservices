package com.spendsmart.categoryservice.web;

import com.spendsmart.categoryservice.domain.Category;
import com.spendsmart.categoryservice.dto.CategoryBudgetRequest;
import com.spendsmart.categoryservice.dto.CategoryUpsertRequest;
import com.spendsmart.categoryservice.enums.CategoryType;
import com.spendsmart.categoryservice.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    private final CategoryService categoryService;

    public CategoryResource(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody CategoryUpsertRequest request) {
        return categoryService.createCategory(mapRequest(request));
    }

    @GetMapping("/user/{userId}")
    public List<Category> getByUserId(@PathVariable Long userId) {
        return categoryService.getByUserId(userId);
    }

    @GetMapping("/{categoryId}")
    public Category getById(@PathVariable Long categoryId) {
        return categoryService.getCategoryById(categoryId);
    }

    @GetMapping("/type")
    public List<Category> getByUserAndType(@RequestParam Long userId, @RequestParam CategoryType type) {
        return categoryService.getByUserAndType(userId, type);
    }

    @PutMapping("/{categoryId}")
    public Category updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryUpsertRequest request) {
        return categoryService.updateCategory(categoryId, mapRequest(request));
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }

    @GetMapping("/defaults")
    public List<Category> getDefaultCategories() {
        return categoryService.getDefaultCategories();
    }

    @PostMapping("/defaults/init/{userId}")
    public List<Category> initDefaults(@PathVariable Long userId) {
        return categoryService.initDefaultCategories(userId);
    }

    @PutMapping("/{categoryId}/budget")
    public Category setBudget(@PathVariable Long categoryId, @Valid @RequestBody CategoryBudgetRequest request) {
        return categoryService.setCategoryBudget(categoryId, request.budgetLimit());
    }

    @GetMapping("/count/{userId}")
    public long getCount(@PathVariable Long userId) {
        return categoryService.getCategoryCount(userId);
    }

    private Category mapRequest(CategoryUpsertRequest request) {
        Category category = new Category();
        category.setUserId(request.userId());
        category.setName(request.name());
        category.setType(request.type());
        category.setIcon(request.icon());
        category.setColorCode(request.colorCode());
        category.setDefault(request.defaultCategory());
        return category;
    }
}
