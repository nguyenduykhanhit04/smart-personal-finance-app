package com.example.financebackend.controller;

import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionDTO sampleTransaction() {
        return TransactionDTO.builder()
                .transactionId(1)
                .userId(1)
                .accountId(1)
                .categoryId(1)
                .title("Ca phe sang")
                .amount(BigDecimal.valueOf(35000))
                .transactionType("expense")
                .transactionDate(LocalDate.of(2026, 10, 1))
                .status("confirmed")
                .build();
    }

    @Test
    void getTransactions_NoDateRange_ReturnsAllByUser() throws Exception {
        when(transactionService.getTransactionsByUserId(1)).thenReturn(List.of(sampleTransaction()));

        mockMvc.perform(get("/api/transactions").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].transactionId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Ca phe sang"));

        verify(transactionService).getTransactionsByUserId(1);
    }

    @Test
    void getTransactions_WithDateRange_ReturnsFiltered() throws Exception {
        when(transactionService.getTransactionsByDateRange(eq(1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(sampleTransaction()));

        mockMvc.perform(get("/api/transactions")
                        .param("userId", "1")
                        .param("startDate", "2026-10-01")
                        .param("endDate", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionId").value(1));

        verify(transactionService).getTransactionsByDateRange(eq(1), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getTransactionById_Found_ReturnsTransaction() throws Exception {
        when(transactionService.getTransactionById(1)).thenReturn(sampleTransaction());

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Ca phe sang"));
    }

    @Test
    void createTransaction_ValidPayload_ReturnsCreated() throws Exception {
        TransactionDTO input = sampleTransaction().toBuilder().transactionId(null).build();
        TransactionDTO created = sampleTransaction();

        when(transactionService.createTransaction(any(TransactionDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(1));
    }

    @Test
    void updateTransaction_ValidPayload_ReturnsUpdated() throws Exception {
        TransactionDTO update = sampleTransaction().toBuilder().title("Ca phe updated").build();

        when(transactionService.updateTransaction(eq(1), any(TransactionDTO.class))).thenReturn(update);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Ca phe updated"));
    }

    @Test
    void deleteTransaction_ReturnsSuccess() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1);

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(transactionService).deleteTransaction(1);
    }
}
