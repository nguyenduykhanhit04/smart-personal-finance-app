package com.example.financebackend.service;

import com.example.financebackend.model.Transaction;
import com.example.financebackend.model.TransactionImage;
import com.example.financebackend.repository.TransactionImageRepository;
import com.example.financebackend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionImageServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionImageRepository transactionImageRepository;

    @InjectMocks
    private TransactionImageService transactionImageService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .transactionId(100)
                .build();
    }

    @Test
    void testUploadImage_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionImageService.uploadImage(100, emptyFile)
        );

        assertEquals("File is empty", ex.getMessage());
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transactionImageRepository);
    }

    @Test
    void testUploadImage_TransactionNotFound_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        when(transactionRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                transactionImageService.uploadImage(999, file)
        );

        assertTrue(ex.getMessage().contains("Transaction not found"));
        verify(transactionRepository).findById(999);
        verifyNoInteractions(transactionImageRepository);
    }

    @Test
    void testUploadImage_NewImage_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "receipt.png", "image/png", "sample data".getBytes());
        when(transactionRepository.findById(100)).thenReturn(Optional.of(transaction));
        when(transactionImageRepository.findByTransactionTransactionId(100)).thenReturn(new ArrayList<>());
        when(transactionImageRepository.save(any(TransactionImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String imageUrl = transactionImageService.uploadImage(100, file);

        assertNotNull(imageUrl);
        assertTrue(imageUrl.startsWith("/uploads/"));
        assertTrue(imageUrl.endsWith(".png"));
        verify(transactionRepository).findById(100);
        verify(transactionImageRepository).findByTransactionTransactionId(100);
        verify(transactionImageRepository).save(any(TransactionImage.class));
    }

    @Test
    void testUploadImage_ExistingImage_UpdatesRecord() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "receipt2.jpg", "image/jpeg", "sample data 2".getBytes());
        TransactionImage existing = TransactionImage.builder()
                .imageId(1)
                .transaction(transaction)
                .imageUrl("/uploads/old.jpg")
                .build();

        when(transactionRepository.findById(100)).thenReturn(Optional.of(transaction));
        when(transactionImageRepository.findByTransactionTransactionId(100)).thenReturn(List.of(existing));
        when(transactionImageRepository.save(any(TransactionImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String imageUrl = transactionImageService.uploadImage(100, file);

        assertNotNull(imageUrl);
        assertTrue(imageUrl.startsWith("/uploads/"));
        assertTrue(imageUrl.endsWith(".jpg"));
        assertEquals(imageUrl, existing.getImageUrl());
        verify(transactionImageRepository).save(existing);
    }
}
