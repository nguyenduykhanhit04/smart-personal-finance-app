package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.dto.UserDTO;
import com.example.financebackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        try {
            // Re-use userDTO for updates
            UserDTO updated = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<UserDTO>> uploadAvatar(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
            }

            // Create uploads folder if not exists
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + id + "_" + UUID.randomUUID().toString() + extension;

            // Save file
            Path filepath = Paths.get("uploads", filename);
            Files.copy(file.getInputStream(), filepath);

            // Construct image URL (relative to server root)
            String imageUrl = "/uploads/" + filename;

            // Update user in DB
            UserDTO userDTO = userService.getUserById(id);
            String oldAvatarUrl = userDTO.getAvatarUrl();
            userDTO.setAvatarUrl(imageUrl);
            UserDTO updated = userService.updateUser(id, userDTO);

            // Try to delete old avatar file if it was a local uploads file
            if (oldAvatarUrl != null && oldAvatarUrl.startsWith("/uploads/")) {
                String oldFilename = oldAvatarUrl.substring("/uploads/".length());
                try {
                    Files.deleteIfExists(Paths.get("uploads", oldFilename));
                } catch (IOException ignored) {}
            }

            return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", updated));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to save avatar file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
