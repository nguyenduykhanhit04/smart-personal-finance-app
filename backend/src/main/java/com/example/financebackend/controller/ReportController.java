package com.example.financebackend.controller;

import com.example.financebackend.dto.ReportDTO;
import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<ReportDTO>> getDailyReport(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ReportDTO report = reportService.getDailyReport(userId, date);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<ReportDTO>> getMonthlyReport(
            @RequestParam Integer userId,
            @RequestParam int year,
            @RequestParam int month) {
        ReportDTO report = reportService.getMonthlyReport(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<ReportDTO>> getByCategory(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportDTO report = reportService.getByCategory(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
