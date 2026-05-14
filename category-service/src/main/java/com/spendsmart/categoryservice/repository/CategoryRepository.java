package com.spendsmart.categoryservice.repository;

import com.spendsmart.categoryservice.domain.Category;
import com.spendsmart.categoryservice.enums.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, CategoryType type);

    Optional<Category> findByCategoryId(Long categoryId);

    Optional<Category> findByUserIdAndNameIgnoreCase(Long userId, String name);

    List<Category> findByIsDefault(boolean isDefault);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByCategoryId(Long categoryId);
}
