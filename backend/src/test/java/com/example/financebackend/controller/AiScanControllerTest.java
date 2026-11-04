package com.example.financebackend.controller;

import com.example.financebackend.dto.request.AiScanClassifyRequest;
import com.example.financebackend.dto.response.GeminiReceiptResponse;
import com.example.financebackend.model.AiScanLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.service.CategoryService;
import com.example.financebackend.service.ai.AiScanService;
import com.example.financebackend.service.ai.GeminiService;
import com.example.financebackend.service.ai.ocr.OcrTextNormalizer;
import com.example.financebackend.service.ai.ocr.ReceiptParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiScanControllerTest {

    @Mock
    private AiScanService aiScanService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private GeminiService geminiService;

    private ReceiptParser receiptParser;
    private AiScanController controller;

    @BeforeEach
    void setUp() {
        receiptParser = new ReceiptParser(new OcrTextNormalizer());
        controller = new AiScanController(aiScanService, categoryService, geminiService, receiptParser);
    }

    @Test
    void classifyBill_GeminiSuccess_UsesGeminiData() {
        AiScanClassifyRequest request = new AiScanClassifyRequest();
        request.setUserId(1);
        request.setRawOcrText("Highlands Coffee\nTong cong: 89.000 VND\nNgay: 05/11/2026");

        GeminiReceiptResponse geminiResponse = new GeminiReceiptResponse();
        geminiResponse.setMerchant("Highlands Coffee");
        geminiResponse.setAmount(BigDecimal.valueOf(89000));
        geminiResponse.setDate(LocalDate.of(2026, 11, 5));
        geminiResponse.setCategory("food");

        when(geminiService.analyzeReceipt(anyString())).thenReturn(geminiResponse);

        Category cat = Category.builder().categoryId(1).categoryName("An uong").build();
        when(categoryService.findCategoryByKeyword(eq(1), eq("food"))).thenReturn(cat);

        AiScanLog mockLog = AiScanLog.builder().aiScanLogId(10).build();
        when(aiScanService.saveLog(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockLog);

        var response = controller.classifyBill(request);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Highlands Coffee", response.getBody().getData().getDetectedMerchant());
        assertEquals(BigDecimal.valueOf(89000), response.getBody().getData().getDetectedAmount());
        assertEquals(BigDecimal.valueOf(0.95), response.getBody().getData().getConfidenceScore());
    }

    @Test
    void classifyBill_GeminiFails_FallbacksToReceiptParser() {
        AiScanClassifyRequest request = new AiScanClassifyRequest();
        request.setUserId(1);
        request.setRawOcrText("WinMart\nTong cong: 150.000\nNgay: 05/11/2026");

        // Giả lập Gemini bị timeout hoặc lỗi mạng
        when(geminiService.analyzeReceipt(anyString())).thenThrow(new RuntimeException("Connection timeout"));

        Category cat = Category.builder().categoryId(3).categoryName("Mua sam").build();
        when(categoryService.findCategoryByKeyword(eq(1), anyString())).thenReturn(cat);

        AiScanLog mockLog = AiScanLog.builder().aiScanLogId(11).build();
        when(aiScanService.saveLog(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockLog);

        var response = controller.classifyBill(request);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        // Kiem tra fallback regex hoat dong dung: trich xuat merchant, tien, va confidence = 0.65
        assertEquals("WinMart", response.getBody().getData().getDetectedMerchant());
        assertEquals(BigDecimal.valueOf(150000), response.getBody().getData().getDetectedAmount());
        assertEquals(BigDecimal.valueOf(0.65), response.getBody().getData().getConfidenceScore());
    }
}

