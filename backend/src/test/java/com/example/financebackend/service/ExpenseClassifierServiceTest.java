package com.example.financebackend.service;

import com.example.financebackend.repository.AiScanLogRepository;
import com.example.financebackend.service.ocr.ReceiptFeatureExtractor;
import com.example.financebackend.service.ocr.ReceiptParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ExpenseClassifierServiceTest {

    @Mock
    private AiScanLogRepository aiScanLogRepository;
    @Mock
    private ReceiptParser receiptParser;
    @Mock
    private ReceiptFeatureExtractor receiptFeatureExtractor;

    private ExpenseClassifierService classifierService;

    @BeforeEach
    void setUp() {
        classifierService = new ExpenseClassifierService(
                aiScanLogRepository,
                receiptParser,
                receiptFeatureExtractor
        );
        // Explicitly call init() since we construct manually in unit test
        classifierService.init();
    }

    @Test
    void testClassify_FoodAndBeverage() {
        assertEquals("food", classifierService.classify("com tam"));
        assertEquals("food", classifierService.classify("ca phe sua"));
        assertEquals("food", classifierService.classify("pho bo"));
    }

    @Test
    void testClassify_Transport() {
        assertEquals("transport", classifierService.classify("grab bike"));
        assertEquals("transport", classifierService.classify("tien do xang"));
        assertEquals("transport", classifierService.classify("taxi mai linh"));
    }

    @Test
    void testClassify_Shopping() {
        assertEquals("shopping", classifierService.classify("mua ao so mi"));
        assertEquals("shopping", classifierService.classify("giay the thao"));
    }

    @Test
    void testClassify_Bills() {
        assertEquals("bills", classifierService.classify("tien dien thang 5"));
        assertEquals("bills", classifierService.classify("hoa don nuoc"));
    }

    @Test
    void testClassify_Entertainment() {
        assertEquals("entertainment", classifierService.classify("ve xem phim cgv"));
        assertEquals("entertainment", classifierService.classify("nap game"));
    }

    @Test
    void testClassify_Health() {
        assertEquals("health", classifierService.classify("mua thuoc tay"));
        assertEquals("health", classifierService.classify("kham benh vien"));
    }

    @Test
    void testClassify_Education() {
        assertEquals("education", classifierService.classify("mua sach giao khoa"));
        assertEquals("education", classifierService.classify("hoc phi tieng anh"));
    }

    @Test
    void testClassify_FallbackToOther() {
        assertEquals("other", classifierService.classify("bàn ghế gỗ"));
        assertEquals("other", classifierService.classify(""));
        assertEquals("other", classifierService.classify((String) null));
    }
}
