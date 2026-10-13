package com.example.financebackend.controller;

import com.example.financebackend.dto.ReportDTO;
import com.example.financebackend.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private ReportDTO sampleReport() {
        return ReportDTO.builder()
                .totalIncome(BigDecimal.valueOf(5000000))
                .totalExpense(BigDecimal.valueOf(2000000))
                .netBalance(BigDecimal.valueOf(3000000))
                .build();
    }

    @Test
    void getDailyReport_ValidParams_ReturnsReport() throws Exception {
        when(reportService.getDailyReport(eq(1), any(LocalDate.class))).thenReturn(sampleReport());

        mockMvc.perform(get("/api/reports/daily")
                        .param("userId", "1")
                        .param("date", "2026-10-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncome").value(5000000))
                .andExpect(jsonPath("$.data.totalExpense").value(2000000));

        verify(reportService).getDailyReport(eq(1), any(LocalDate.class));
    }

    @Test
    void getMonthlyReport_ValidParams_ReturnsReport() throws Exception {
        when(reportService.getMonthlyReport(1, 2026, 10)).thenReturn(sampleReport());

        mockMvc.perform(get("/api/reports/monthly")
                        .param("userId", "1")
                        .param("year", "2026")
                        .param("month", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.netBalance").value(3000000));

        verify(reportService).getMonthlyReport(1, 2026, 10);
    }

    @Test
    void getByCategoryReport_ValidParams_ReturnsReport() throws Exception {
        when(reportService.getByCategory(eq(1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sampleReport());

        mockMvc.perform(get("/api/reports/by-category")
                        .param("userId", "1")
                        .param("startDate", "2026-10-01")
                        .param("endDate", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIncome").value(5000000));

        verify(reportService).getByCategory(eq(1), any(LocalDate.class), any(LocalDate.class));
    }
}
