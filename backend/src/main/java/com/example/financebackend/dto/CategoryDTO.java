package com.example.financebackend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CategoryDTO {
    private Integer categoryId;
    private Integer userId;
    private String categoryName;
    private String categoryType;
    private String icon;
    private String color;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
