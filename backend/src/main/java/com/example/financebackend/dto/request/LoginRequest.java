package com.example.financebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String firebaseUid;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String idToken;
}
