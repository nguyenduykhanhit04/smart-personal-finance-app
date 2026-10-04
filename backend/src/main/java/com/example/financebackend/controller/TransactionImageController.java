package com.example.financebackend.controller;

import com.example.financebackend.dto.response.ApiResponse;
import com.example.financebackend.service.TransactionImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/transaction-images")
public class TransactionImageController {

    private final TransactionImageService transactionImageService;

    public TransactionImageController(TransactionImageService transactionImageService) {
        this.transactionImageService = transactionImageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> uploadImage(
            @RequestParam("transactionId") Integer transactionId,
            @RequestParam("file") MultipartFile file) {

        try {
            transactionImageService.uploadImage(transactionId, file);
            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to save image file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
