package com.example.financebackend.controller;

import com.example.financebackend.dto.UserDTO;
import com.example.financebackend.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @Test
    void firebaseLogin_ValidToken_ReturnsUserDTO() throws Exception {
        String fakeToken = "valid.firebase.token";

        FirebaseToken decodedToken = mock(FirebaseToken.class);
        UserDTO userDTO = UserDTO.builder()
                .userId(1)
                .email("user@example.com")
                .fullName("Nguyen Van A")
                .firebaseUid("firebase_uid_123")
                .authProvider("google")
                .build();

        when(firebaseAuth.verifyIdToken(fakeToken)).thenReturn(decodedToken);
        when(userService.syncFirebaseUserWithToken(decodedToken)).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/firebase-login")
                        .header("Authorization", "Bearer " + fakeToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"));

        verify(firebaseAuth).verifyIdToken(fakeToken);
        verify(userService).syncFirebaseUserWithToken(decodedToken);
    }

    @Test
    void firebaseLogin_MissingAuthorizationHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/firebase-login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
