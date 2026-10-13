package com.example.financebackend.controller;

import com.example.financebackend.dto.CategoryDTO;
import com.example.financebackend.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryDTO sampleCategory() {
        return CategoryDTO.builder()
                .categoryId(1)
                .categoryName("An uong")
                .type("expense")
                .color("#FF5722")
                .userId(1)
                .build();
    }

    @Test
    void getCategoriesByUserId_ReturnsList() throws Exception {
        when(categoryService.getCategoriesByUserId(1)).thenReturn(List.of(sampleCategory()));

        mockMvc.perform(get("/api/categories").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("An uong"));

        verify(categoryService).getCategoriesByUserId(1);
    }

    @Test
    void createCategory_ValidPayload_ReturnsCreated() throws Exception {
        CategoryDTO input = CategoryDTO.builder()
                .categoryName("Di lai")
                .type("expense")
                .color("#2196F3")
                .userId(1)
                .build();
        CategoryDTO created = input.toBuilder().categoryId(2).build();

        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value(2))
                .andExpect(jsonPath("$.data.categoryName").value("Di lai"));
    }

    @Test
    void updateCategory_ValidPayload_ReturnsUpdated() throws Exception {
        CategoryDTO update = CategoryDTO.builder().categoryName("An uong updated").build();
        CategoryDTO updated = sampleCategory().toBuilder().categoryName("An uong updated").build();

        when(categoryService.updateCategory(eq(1), any(CategoryDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("An uong updated"));
    }

    @Test
    void deleteCategory_ReturnsSuccess() throws Exception {
        doNothing().when(categoryService).deleteCategory(1);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(categoryService).deleteCategory(1);
    }
}
