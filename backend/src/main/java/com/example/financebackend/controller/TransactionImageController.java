package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.model.TransactionImage;
import com.example.financebackend.repository.TransactionImageRepository;
import com.example.financebackend.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/transaction-images")
public class TransactionImageController {

    private final TransactionRepository transactionRepository;
    private final TransactionImageRepository transactionImageRepository;

    public TransactionImageController(TransactionRepository transactionRepository,
                                      TransactionImageRepository transactionImageRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionImageRepository = transactionImageRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> uploadImage(
            @RequestParam("transactionId") Integer transactionId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
            }

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

            // Create uploads folder if not exists
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filepath = Paths.get("uploads", filename);
            Files.copy(file.getInputStream(), filepath);

            // Construct image URL (relative to server root)
            String imageUrl = "/uploads/" + filename;

            // Check if record already exists for the transaction
            java.util.List<TransactionImage> existingImages = transactionImageRepository.findByTransactionTransactionId(transactionId);
            TransactionImage transactionImage;
            if (existingImages != null && !existingImages.isEmpty()) {
                transactionImage = existingImages.get(0);

                // Delete old file from disk if it exists
                String oldUrl = transactionImage.getImageUrl();
                if (oldUrl != null && oldUrl.startsWith("/uploads/")) {
                    String oldFilename = oldUrl.substring("/uploads/".length());
                    try {
                        Files.deleteIfExists(Paths.get("uploads", oldFilename));
                    } catch (IOException ignored) {}
                }

                transactionImage.setImageUrl(imageUrl);
                transactionImage.setUploadedAt(java.time.LocalDateTime.now());
            } else {
                // Create database record
                transactionImage = TransactionImage.builder()
                        .transaction(transaction)
                        .imageUrl(imageUrl)
                        .ocrText("") // Manual mode does not run OCR
                        .build();
            }

            TransactionImage saved = transactionImageRepository.save(transactionImage);

            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", null));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to save image file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
