package com.example.financebackend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Integer userId;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
