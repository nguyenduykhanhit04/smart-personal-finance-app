package com.example.financebackend.service;

import com.example.financebackend.dto.ReportDTO;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private Category foodCategory;
    private Category transportCategory;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        foodCategory = Category.builder()
                .categoryId(1)
                .categoryName("Ăn uống")
                .categoryType("expense")
                .build();

        transportCategory = Category.builder()
                .categoryId(2)
                .categoryName("Di chuyển")
                .categoryType("expense")
                .build();

        salaryCategory = Category.builder()
                .categoryId(3)
                .categoryName("Tiền lương")
                .categoryType("income")
                .build();
    }

    @Test
    void testGetDailyReport_CalculatesIncomeAndExpenseCorrectly() {
        LocalDate today = LocalDate.of(2026, 5, 15);

        Transaction txIncome = Transaction.builder()
                .transactionId(1)
                .amount(new BigDecimal("1000000"))
                .transactionType("income")
                .category(salaryCategory)
                .transactionDate(today)
                .build();

        Transaction txExpense = Transaction.builder()
                .transactionId(2)
                .amount(new BigDecimal("350000"))
                .transactionType("expense")
                .category(foodCategory)
                .transactionDate(today)
                .build();

        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(1, today, today))
                .thenReturn(Arrays.asList(txIncome, txExpense));

        ReportDTO report = reportService.getDailyReport(1, today);

        assertNotNull(report);
        assertEquals(1, report.getUserId());
        assertEquals("daily", report.getReportType());
        assertEquals(new BigDecimal("1000000"), report.getTotalIncome());
        assertEquals(new BigDecimal("350000"), report.getTotalExpense());
        assertEquals(new BigDecimal("650000"), report.getNetAmount());
    }

    @Test
    void testGetMonthlyReport_GeneratesDailyBreakdown() {
        int year = 2026;
        int month = 5;

        LocalDate day1 = LocalDate.of(2026, 5, 2);
        LocalDate day2 = LocalDate.of(2026, 5, 10);

        Transaction tx1 = Transaction.builder()
                .transactionId(1)
                .amount(new BigDecimal("200000"))
                .transactionType("expense")
                .category(foodCategory)
                .transactionDate(day1)
                .build();

        Transaction tx2 = Transaction.builder()
                .transactionId(2)
                .amount(new BigDecimal("5000000"))
                .transactionType("income")
                .category(salaryCategory)
                .transactionDate(day2)
                .build();

        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(eq(1), any(), any()))
                .thenReturn(Arrays.asList(tx1, tx2));

        ReportDTO report = reportService.getMonthlyReport(1, year, month);

        assertNotNull(report);
        assertEquals("monthly", report.getReportType());
        assertEquals("2026-05", report.getPeriod());
        assertEquals(new BigDecimal("5000000"), report.getTotalIncome());
        assertEquals(new BigDecimal("200000"), report.getTotalExpense());
        assertEquals(new BigDecimal("4800000"), report.getNetAmount());

        assertNotNull(report.getDailyBreakdowns());
        assertEquals(2, report.getDailyBreakdowns().size());
    }

    @Test
    void testGetByCategory_ComputesPercentagesAndSortsDescending() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        // Food: 600,000, Transport: 400,000 => Total = 1,000,000
        Transaction txFood1 = Transaction.builder()
                .transactionId(1)
                .amount(new BigDecimal("400000"))
                .transactionType("expense")
                .category(foodCategory)
                .transactionDate(start)
                .build();

        Transaction txFood2 = Transaction.builder()
                .transactionId(2)
                .amount(new BigDecimal("200000"))
                .transactionType("expense")
                .category(foodCategory)
                .transactionDate(start)
                .build();

        Transaction txTransport = Transaction.builder()
                .transactionId(3)
                .amount(new BigDecimal("400000"))
                .transactionType("expense")
                .category(transportCategory)
                .transactionDate(start)
                .build();

        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(1, start, end))
                .thenReturn(Arrays.asList(txFood1, txFood2, txTransport));

        ReportDTO report = reportService.getByCategory(1, start, end);

        assertNotNull(report);
        List<ReportDTO.CategoryBreakdown> breakdowns = report.getCategoryBreakdowns();
        assertNotNull(breakdowns);
        assertEquals(2, breakdowns.size());

        // First item must be Food (highest amount: 600,000)
        ReportDTO.CategoryBreakdown first = breakdowns.get(0);
        assertEquals("Ăn uống", first.getCategoryName());
        assertEquals(new BigDecimal("600000"), first.getTotalAmount());
        assertEquals(60.0, first.getPercentage());

        // Second item must be Transport (400,000)
        ReportDTO.CategoryBreakdown second = breakdowns.get(1);
        assertEquals("Di chuyển", second.getCategoryName());
        assertEquals(new BigDecimal("400000"), second.getTotalAmount());
        assertEquals(40.0, second.getPercentage());
    }

    @Test
    void testGetDailyReport_NoTransactions_ReturnsZeroValues() {
        LocalDate today = LocalDate.of(2026, 5, 15);

        when(transactionRepository.findByUserUserIdAndTransactionDateBetween(1, today, today))
                .thenReturn(Collections.emptyList());

        ReportDTO report = reportService.getDailyReport(1, today);

        assertNotNull(report);
        assertEquals(BigDecimal.ZERO, report.getTotalIncome());
        assertEquals(BigDecimal.ZERO, report.getTotalExpense());
        assertEquals(BigDecimal.ZERO, report.getNetAmount());
        assertTrue(report.getCategoryBreakdowns().isEmpty());
    }
}
