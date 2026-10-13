package com.example.financebackend.controller;

import com.example.financebackend.dto.AccountDTO;
import com.example.financebackend.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private AccountDTO sampleAccount() {
        return AccountDTO.builder()
                .accountId(1)
                .userId(1)
                .accountName("Vi tieu dung")
                .balance(BigDecimal.valueOf(500000))
                .build();
    }

    @Test
    void getAccountsByUserId_ReturnsList() throws Exception {
        when(accountService.getAccountsByUserId(1)).thenReturn(List.of(sampleAccount()));

        mockMvc.perform(get("/api/accounts").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].accountId").value(1))
                .andExpect(jsonPath("$.data[0].accountName").value("Vi tieu dung"));

        verify(accountService).getAccountsByUserId(1);
    }

    @Test
    void createAccount_ValidPayload_ReturnsCreatedAccount() throws Exception {
        AccountDTO input = AccountDTO.builder()
                .userId(1)
                .accountName("Vi tiet kiem")
                .balance(BigDecimal.valueOf(1000000))
                .build();
        AccountDTO created = input.toBuilder().accountId(2).build();

        when(accountService.createAccount(any(AccountDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountId").value(2))
                .andExpect(jsonPath("$.data.accountName").value("Vi tiet kiem"));
    }

    @Test
    void updateAccount_ValidPayload_ReturnsUpdated() throws Exception {
        AccountDTO update = AccountDTO.builder().accountName("Vi moi").build();
        AccountDTO updated = sampleAccount().toBuilder().accountName("Vi moi").build();

        when(accountService.updateAccount(eq(1), any(AccountDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountName").value("Vi moi"));
    }

    @Test
    void deleteAccount_ReturnsSuccess() throws Exception {
        doNothing().when(accountService).deleteAccount(1);

        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(accountService).deleteAccount(1);
    }
}
