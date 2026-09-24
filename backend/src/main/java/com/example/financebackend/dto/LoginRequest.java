package com.example.financebackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String firebaseUid;
    private String email;
    private String fullName;
    private String avatarUrl;
}
