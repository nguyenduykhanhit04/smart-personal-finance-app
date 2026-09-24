package com.example.financebackend.service;

import com.example.financebackend.dto.CategoryDTO;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryDTO> getCategoriesByUserId(Integer userId) {
        return categoryRepository.findByUserUserIdOrIsDefaultTrue(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Category findCategoryByKeyword(Integer userId, String keyword) {
        List<Category> categories = categoryRepository.findByUserUserIdOrIsDefaultTrue(userId);
        String normalizedKeyword = keyword != null ? keyword.toLowerCase(Locale.ROOT) : "";
        String vietnameseName = mapKeywordToVietnameseName(normalizedKeyword);

        for (Category category : categories) {
            String categoryName = category.getCategoryName();
            if (categoryName == null) {
                continue;
            }
            String normalizedCategoryName = categoryName.toLowerCase(Locale.ROOT);
            if (categoryName.equalsIgnoreCase(vietnameseName)
                    || (!normalizedKeyword.isBlank() && normalizedCategoryName.contains(normalizedKeyword))) {
                return category;
            }
        }

        for (Category category : categories) {
            if ("expense".equalsIgnoreCase(category.getCategoryType())) {
                return category;
            }
        }

        return null;
    }

    private String mapKeywordToVietnameseName(String keyword) {
        switch (keyword) {
            case "food":
                return "Ăn uống";
            case "transport":
                return "Di chuyển";
            case "shopping":
                return "Mua sắm";
            case "bills":
                return "Hóa đơn";
            case "entertainment":
                return "Giải trí";
            case "health":
                return "Sức khỏe";
            case "education":
                return "Giáo dục";
            default:
                return "Khác";
        }
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
        }

        Category category = Category.builder()
                .user(user)
                .categoryName(dto.getCategoryName())
                .categoryType(dto.getCategoryType())
                .icon(dto.getIcon())
                .color(dto.getColor())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();

        category = categoryRepository.save(category);
        return toDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Integer categoryId, CategoryDTO dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        if (dto.getCategoryName() != null) category.setCategoryName(dto.getCategoryName());
        if (dto.getCategoryType() != null) category.setCategoryType(dto.getCategoryType());
        if (dto.getIcon() != null) category.setIcon(dto.getIcon());
        if (dto.getColor() != null) category.setColor(dto.getColor());

        category = categoryRepository.save(category);
        return toDTO(category);
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    public CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .userId(category.getUser() != null ? category.getUser().getUserId() : null)
                .categoryName(category.getCategoryName())
                .categoryType(category.getCategoryType())
                .icon(category.getIcon())
                .color(category.getColor())
                .isDefault(category.getIsDefault())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
