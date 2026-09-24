package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.dto.LoginRequest;
import com.example.financebackend.dto.UserDTO;
import com.example.financebackend.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<ApiResponse<UserDTO>> firebaseLogin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LoginRequest loginRequest) {
        try {
            String idToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                idToken = authHeader.substring(7);
            }

            if (idToken == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Missing Firebase ID Token in Authorization header"));
            }

            // Verify Firebase ID Token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // Sync user with details from decoded token
            UserDTO syncedUser = userService.syncFirebaseUserWithToken(decodedToken);
            return ResponseEntity.ok(ApiResponse.success("User synced successfully", syncedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication/Sync failed: " + e.getMessage()));
        }
    }
}
