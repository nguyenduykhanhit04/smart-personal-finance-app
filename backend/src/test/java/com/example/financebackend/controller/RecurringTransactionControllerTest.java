package com.example.financebackend.controller;

import com.example.financebackend.dto.RecurringTransactionDTO;
import com.example.financebackend.service.RecurringTransactionService;
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

@WebMvcTest(RecurringTransactionController.class)
class RecurringTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecurringTransactionService recurringTransactionService;

    private RecurringTransactionDTO sampleRecurring() {
        return RecurringTransactionDTO.builder()
                .recurringId(1)
                .userId(1)
                .accountId(1)
                .title("Tien thue nha")
                .amount(BigDecimal.valueOf(3000000))
                .transactionType("expense")
                .repeatType("MONTHLY")
                .repeatInterval(1)
                .startDate(LocalDate.of(2026, 1, 1))
                .isActive(true)
                .build();
    }

    @Test
    void getRecurringTransactions_ReturnsList() throws Exception {
        when(recurringTransactionService.getRecurringByUserId(1)).thenReturn(List.of(sampleRecurring()));

        mockMvc.perform(get("/api/recurring-transactions").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].recurringId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Tien thue nha"));

        verify(recurringTransactionService).getRecurringByUserId(1);
    }

    @Test
    void createRecurringTransaction_ValidPayload_ReturnsCreated() throws Exception {
        RecurringTransactionDTO input = sampleRecurring().toBuilder().recurringId(null).build();
        RecurringTransactionDTO created = sampleRecurring();

        when(recurringTransactionService.createRecurring(any(RecurringTransactionDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/recurring-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recurringId").value(1))
                .andExpect(jsonPath("$.data.title").value("Tien thue nha"));
    }

    @Test
    void updateRecurringTransaction_ValidPayload_ReturnsUpdated() throws Exception {
        RecurringTransactionDTO update = sampleRecurring().toBuilder().title("Tien thue moi").build();

        when(recurringTransactionService.updateRecurring(eq(1), any(RecurringTransactionDTO.class))).thenReturn(update);

        mockMvc.perform(put("/api/recurring-transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Tien thue moi"));
    }

    @Test
    void deleteRecurringTransaction_ReturnsSuccess() throws Exception {
        doNothing().when(recurringTransactionService).deleteRecurring(1);

        mockMvc.perform(delete("/api/recurring-transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(recurringTransactionService).deleteRecurring(1);
    }

    @Test
    void triggerRecurringTransactions_ReturnsSuccess() throws Exception {
        doNothing().when(recurringTransactionService).processRecurringTransactions();

        mockMvc.perform(post("/api/recurring-transactions/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(recurringTransactionService).processRecurringTransactions();
    }
}
