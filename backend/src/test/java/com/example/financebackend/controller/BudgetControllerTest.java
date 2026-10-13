package com.example.financebackend.controller;

import com.example.financebackend.dto.BudgetDTO;
import com.example.financebackend.service.BudgetService;
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

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetService budgetService;

    private BudgetDTO sampleBudget() {
        return BudgetDTO.builder()
                .budgetId(1)
                .userId(1)
                .categoryId(1)
                .limitAmount(BigDecimal.valueOf(2000000))
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .build();
    }

    @Test
    void getBudgets_NoActiveDate_ReturnsAllByUser() throws Exception {
        when(budgetService.getBudgetsByUserId(1)).thenReturn(List.of(sampleBudget()));

        mockMvc.perform(get("/api/budgets").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].budgetId").value(1));

        verify(budgetService).getBudgetsByUserId(1);
    }

    @Test
    void getBudgets_WithActiveDate_ReturnsActiveBudgets() throws Exception {
        when(budgetService.getActiveBudgets(eq(1), any(LocalDate.class))).thenReturn(List.of(sampleBudget()));

        mockMvc.perform(get("/api/budgets")
                        .param("userId", "1")
                        .param("activeDate", "2026-10-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].budgetId").value(1));

        verify(budgetService).getActiveBudgets(eq(1), any(LocalDate.class));
    }

    @Test
    void getBudgetById_Found_ReturnsBudget() throws Exception {
        when(budgetService.getBudgetById(1)).thenReturn(sampleBudget());

        mockMvc.perform(get("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.budgetId").value(1));
    }

    @Test
    void createBudget_ValidPayload_ReturnsCreated() throws Exception {
        BudgetDTO input = sampleBudget().toBuilder().budgetId(null).build();
        BudgetDTO created = sampleBudget();

        when(budgetService.createBudget(any(BudgetDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.budgetId").value(1));
    }

    @Test
    void updateBudget_ValidPayload_ReturnsUpdated() throws Exception {
        BudgetDTO update = sampleBudget().toBuilder().limitAmount(BigDecimal.valueOf(3000000)).build();

        when(budgetService.updateBudget(eq(1), any(BudgetDTO.class))).thenReturn(update);

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.limitAmount").value(3000000));
    }

    @Test
    void deleteBudget_ReturnsSuccess() throws Exception {
        doNothing().when(budgetService).deleteBudget(1);

        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(budgetService).deleteBudget(1);
    }
}
