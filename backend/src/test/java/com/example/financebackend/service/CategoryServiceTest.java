package com.example.financebackend.service;

import com.example.financebackend.dto.CategoryDTO;
import com.example.financebackend.exception.ResourceNotFoundException;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category foodCategory;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .fullName("Test User")
                .build();

        foodCategory = Category.builder()
                .categoryId(1)
                .categoryName("Ăn uống")
                .categoryType("expense")
                .icon("ic_food")
                .color("#FF5722")
                .isDefault(true)
                .build();

        salaryCategory = Category.builder()
                .categoryId(2)
                .categoryName("Tiền lương")
                .categoryType("income")
                .icon("ic_salary")
                .color("#4CAF50")
                .isDefault(true)
                .build();
    }

    @Test
    void testGetCategoriesByUserId_Success() {
        when(categoryRepository.findByUserUserIdOrIsDefaultTrue(1))
                .thenReturn(Arrays.asList(foodCategory, salaryCategory));

        List<CategoryDTO> result = categoryService.getCategoriesByUserId(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Ăn uống", result.get(0).getCategoryName());
        assertEquals("Tiền lương", result.get(1).getCategoryName());
    }

    @Test
    void testFindCategoryByKeyword_MapsToVietnamese_Food() {
        when(categoryRepository.findByUserUserIdOrIsDefaultTrue(1))
                .thenReturn(Arrays.asList(foodCategory, salaryCategory));

        Category matched = categoryService.findCategoryByKeyword(1, "food");

        assertNotNull(matched);
        assertEquals("Ăn uống", matched.getCategoryName());
    }

    @Test
    void testFindCategoryByKeyword_FallbackToFirstExpense() {
        when(categoryRepository.findByUserUserIdOrIsDefaultTrue(1))
                .thenReturn(Arrays.asList(salaryCategory, foodCategory));

        Category fallback = categoryService.findCategoryByKeyword(1, "unknown_random_keyword");

        assertNotNull(fallback);
        assertEquals("expense", fallback.getCategoryType());
        assertEquals("Ăn uống", fallback.getCategoryName());
    }

    @Test
    void testCreateCategory_Success() {
        CategoryDTO createDTO = CategoryDTO.builder()
                .userId(1)
                .categoryName("Mua sách")
                .categoryType("expense")
                .icon("ic_book")
                .color("#9C27B0")
                .isDefault(false)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setCategoryId(3);
            return c;
        });

        CategoryDTO result = categoryService.createCategory(createDTO);

        assertNotNull(result);
        assertEquals(3, result.getCategoryId());
        assertEquals("Mua sách", result.getCategoryName());
        assertEquals(1, result.getUserId());
        assertFalse(result.getIsDefault());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategory_UserNotFound_ThrowsException() {
        CategoryDTO createDTO = CategoryDTO.builder()
                .userId(999)
                .categoryName("Test")
                .build();

        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.createCategory(createDTO));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(foodCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDTO updateDTO = CategoryDTO.builder()
                .categoryName("Ăn uống & Cà phê")
                .color("#E64A19")
                .build();

        CategoryDTO result = categoryService.updateCategory(1, updateDTO);

        assertNotNull(result);
        assertEquals("Ăn uống & Cà phê", result.getCategoryName());
        assertEquals("#E64A19", result.getColor());
        verify(categoryRepository).save(foodCategory);
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.existsById(1)).thenReturn(true);

        categoryService.deleteCategory(1);

        verify(categoryRepository).deleteById(1);
    }

    @Test
    void testDeleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(999));
        verify(categoryRepository, never()).deleteById(any());
    }
}
