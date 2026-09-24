package com.example.financebackend.service;

import com.example.financebackend.dto.ReportDTO;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public ReportDTO getDailyReport(Integer userId, LocalDate date) {
        List<Transaction> transactions = transactionRepository
                .findByUserUserIdAndTransactionDateBetween(userId, date, date);

        return buildReport(userId, "daily", date.toString(), transactions);
    }

    public ReportDTO getMonthlyReport(Integer userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserUserIdAndTransactionDateBetween(userId, startDate, endDate);

        ReportDTO report = buildReport(userId, "monthly", year + "-" + String.format("%02d", month), transactions);

        // Add daily breakdowns for monthly report
        Map<LocalDate, List<Transaction>> byDate = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionDate));

        List<ReportDTO.DailyBreakdown> dailyBreakdowns = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            List<Transaction> dayTx = byDate.getOrDefault(d, Collections.emptyList());
            BigDecimal income = dayTx.stream()
                    .filter(t -> "income".equalsIgnoreCase(t.getTransactionType()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = dayTx.stream()
                    .filter(t -> "expense".equalsIgnoreCase(t.getTransactionType()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (income.compareTo(BigDecimal.ZERO) > 0 || expense.compareTo(BigDecimal.ZERO) > 0) {
                dailyBreakdowns.add(ReportDTO.DailyBreakdown.builder()
                        .date(d.toString())
                        .income(income)
                        .expense(expense)
                        .build());
            }
        }
        report.setDailyBreakdowns(dailyBreakdowns);

        return report;
    }

    public ReportDTO getByCategory(Integer userId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository
                .findByUserUserIdAndTransactionDateBetween(userId, startDate, endDate);

        return buildReport(userId, "by-category", startDate + " to " + endDate, transactions);
    }

    private ReportDTO buildReport(Integer userId, String reportType, String period, List<Transaction> transactions) {
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> "income".equalsIgnoreCase(t.getTransactionType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> "expense".equalsIgnoreCase(t.getTransactionType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        // Category breakdowns
        BigDecimal totalAll = totalIncome.add(totalExpense);
        Map<String, List<Transaction>> byCategory = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t ->
                        t.getCategory().getCategoryId() + "|" +
                        t.getCategory().getCategoryName() + "|" +
                        t.getCategory().getCategoryType()));

        List<ReportDTO.CategoryBreakdown> categoryBreakdowns = byCategory.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|");
                    BigDecimal catTotal = entry.getValue().stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double percentage = totalAll.compareTo(BigDecimal.ZERO) > 0
                            ? catTotal.multiply(BigDecimal.valueOf(100))
                                    .divide(totalAll, 2, RoundingMode.HALF_UP).doubleValue()
                            : 0.0;

                    return ReportDTO.CategoryBreakdown.builder()
                            .categoryId(Integer.parseInt(parts[0]))
                            .categoryName(parts[1])
                            .categoryType(parts.length > 2 ? parts[2] : null)
                            .totalAmount(catTotal)
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());

        return ReportDTO.builder()
                .userId(userId)
                .reportType(reportType)
                .period(period)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netAmount(netAmount)
                .categoryBreakdowns(categoryBreakdowns)
                .build();
    }
}
