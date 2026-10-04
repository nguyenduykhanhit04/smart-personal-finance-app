package com.example.financebackend.service;

import com.example.financebackend.exception.ResourceNotFoundException;
import com.example.financebackend.model.Transaction;
import com.example.financebackend.model.TransactionImage;
import com.example.financebackend.repository.TransactionImageRepository;
import com.example.financebackend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionImageService {

    private final TransactionRepository transactionRepository;
    private final TransactionImageRepository transactionImageRepository;

    public TransactionImageService(TransactionRepository transactionRepository,
                                   TransactionImageRepository transactionImageRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionImageRepository = transactionImageRepository;
    }

    public String uploadImage(Integer transactionId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

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
        List<TransactionImage> existingImages = transactionImageRepository.findByTransactionTransactionId(transactionId);
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
            transactionImage.setUploadedAt(LocalDateTime.now());
        } else {
            // Create database record
            transactionImage = TransactionImage.builder()
                    .transaction(transaction)
                    .imageUrl(imageUrl)
                    .ocrText("") // Manual mode does not run OCR
                    .build();
        }

        transactionImageRepository.save(transactionImage);
        return imageUrl;
    }
}
