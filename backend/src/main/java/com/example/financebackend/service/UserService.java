package com.example.financebackend.service;

import com.example.financebackend.dto.UserDTO;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountService accountService;

    public UserService(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

    @Transactional
    public UserDTO syncFirebaseUserWithToken(FirebaseToken decodedToken) {
        String firebaseUid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String fullName = (String) decodedToken.getClaims().get("name");
        String avatarUrl = (String) decodedToken.getClaims().get("picture");

        // Determine auth_provider from provider data inside the firebase claim map
        String signInProvider = null;
        if (decodedToken.getClaims().get("firebase") instanceof java.util.Map) {
            java.util.Map<?, ?> firebaseClaim = (java.util.Map<?, ?>) decodedToken.getClaims().get("firebase");
            signInProvider = (String) firebaseClaim.get("sign_in_provider");
        }

        String authProvider = "firebase";
        if ("google.com".equals(signInProvider)) {
            authProvider = "google";
        } else if ("facebook.com".equals(signInProvider)) {
            authProvider = "facebook";
        } else if ("password".equals(signInProvider)) {
            authProvider = "firebase";
        } else if (signInProvider != null) {
            authProvider = signInProvider;
        }

        Optional<User> existingUser = findExistingFirebaseUser(firebaseUid, email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setFirebaseUid(firebaseUid);
            if (fullName != null) user.setFullName(fullName);
            if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
            if (email != null) user.setEmail(email);
            user.setAuthProvider(authProvider);
            user = userRepository.save(user);
        } else {
            user = User.builder()
                    .firebaseUid(firebaseUid)
                    .email(email != null ? email : "")
                    .fullName(fullName != null ? fullName : (email != null ? email.split("@")[0] : "User"))
                    .avatarUrl(avatarUrl != null ? avatarUrl : "")
                    .authProvider(authProvider)
                    .build();
            user = userRepository.save(user);
        }

        // Automatically ensure default account "Ví chính" exists
        accountService.getOrCreateDefaultAccount(user.getUserId());

        return toDTO(user);
    }

    private Optional<User> findExistingFirebaseUser(String firebaseUid, String email) {
        Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
        if (existingUser.isPresent()) {
            return existingUser;
        }

        if (email != null && !email.isBlank()) {
            return userRepository.findByEmail(email);
        }

        return Optional.empty();
    }

    public UserDTO getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return toDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Integer userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (userDTO.getFullName() != null) user.setFullName(userDTO.getFullName());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getAvatarUrl() != null) user.setAvatarUrl(userDTO.getAvatarUrl());

        user = userRepository.save(user);
        return toDTO(user);
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .firebaseUid(user.getFirebaseUid())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
