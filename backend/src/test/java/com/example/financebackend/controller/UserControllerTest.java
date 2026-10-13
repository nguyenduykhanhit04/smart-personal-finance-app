package com.example.financebackend.controller;

import com.example.financebackend.dto.UserDTO;
import com.example.financebackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO sampleUser() {
        return UserDTO.builder()
                .userId(1)
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .phone("0912345678")
                .authProvider("google")
                .build();
    }

    @Test
    void getUserById_Found_ReturnsUser() throws Exception {
        when(userService.getUserById(1)).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"));

        verify(userService).getUserById(1);
    }

    @Test
    void updateUser_ValidPayload_ReturnsUpdatedUser() throws Exception {
        UserDTO updateDTO = UserDTO.builder()
                .fullName("Nguyen Van B")
                .phone("0987654321")
                .build();
        UserDTO updatedUser = sampleUser();
        updatedUser.setFullName("Nguyen Van B");
        updatedUser.setPhone("0987654321");

        when(userService.updateUser(eq(1), any(UserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van B"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"));

        verify(userService).updateUser(eq(1), any(UserDTO.class));
    }
}
