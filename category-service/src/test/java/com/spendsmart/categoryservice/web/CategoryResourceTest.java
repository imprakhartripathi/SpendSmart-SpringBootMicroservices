package com.spendsmart.categoryservice.web;

import com.spendsmart.categoryservice.domain.Category;
import com.spendsmart.categoryservice.enums.CategoryType;
import com.spendsmart.categoryservice.service.CategoryService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryResource.class)
class CategoryResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void createCategoryReturnsCreatedCategory() throws Exception {
        Category category = new Category();
        category.setCategoryId(8L);
        category.setUserId(7L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setIcon("restaurant");
        category.setColorCode("#EF4444");
        category.setDefault(false);
        category.setBudgetLimit(new BigDecimal("10000"));

        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(
                        post("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": 7,
                                          "name": "Food",
                                          "type": "EXPENSE",
                                          "icon": "restaurant",
                                          "colorCode": "#EF4444",
                                          "defaultCategory": false
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(8))
                .andExpect(jsonPath("$.name").value("Food"));
    }
}
