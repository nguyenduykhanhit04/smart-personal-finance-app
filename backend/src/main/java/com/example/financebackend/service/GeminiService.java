package com.example.financebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
@PropertySource(value = "classpath:ai-settings.properties", ignoreResourceNotFound = true)
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public GeminiReceiptResult analyzeReceipt(String rawOcrText) {
        String url = apiUrl + "?key=" + apiKey;

        String prompt = "Phân tích đoạn văn bản OCR từ hóa đơn sau và trích xuất các thông tin:\n"
                + "1. Tên cửa hàng/thương hiệu (merchant) - trích xuất tên thương hiệu ngắn gọn (ví dụ: WinMart, Highlands Coffee, Grab) hoặc null nếu không thể xác định.\n"
                + "2. Tổng số tiền thanh toán thực tế (amount) - lấy tổng tiền khách hàng phải thanh toán cuối cùng sau khi đã giảm giá hoặc cộng thuế (trả về kiểu số nguyên hoặc null nếu không thấy số tiền hợp lý).\n"
                + "3. Ngày giao dịch (date) bắt buộc trả về theo định dạng YYYY-MM-DD (Ví dụ: 2026-06-05). Nếu chỉ thấy ngày dạng DD/MM/YYYY hoặc khác trong văn bản, bạn bắt buộc phải chuyển đổi sang YYYY-MM-DD trước khi trả về. Trả về null nếu không tìm thấy.\n"
                + "4. Gợi ý từ khóa danh mục chi tiêu phù hợp nhất (category) từ danh sách sau: [food, transport, shopping, bills, entertainment, health, other]. Quy tắc gợi ý: \n"
                + "   - food: Ăn uống, quán cafe, nhà hàng, trà sữa...\n"
                + "   - transport: Đi lại, xăng dầu, Grab, Be, taxi, vé xe...\n"
                + "   - shopping: Mua sắm quần áo, mỹ phẩm, siêu thị Winmart/Coopmart mua đồ dùng...\n"
                + "   - bills: Hóa đơn điện, nước, internet, chung cư, bảo hiểm...\n"
                + "   - entertainment: Xem phim, ca nhạc, trò chơi, du lịch...\n"
                + "   - health: Thuốc men, khám bệnh, phòng gym...\n"
                + "   - other: Các chi phí khác không thuộc nhóm trên.\n\n"
                + "Văn bản OCR:\n" + rawOcrText;

        // Construct request payload
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{part});
        requestBody.put("contents", new Object[]{content});

        // Generation Config for JSON Structured Output
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> responseSchema = new HashMap<>();
        responseSchema.put("type", "OBJECT");

        Map<String, Object> properties = new HashMap<>();
        properties.put("merchant", Map.of("type", "STRING"));
        properties.put("amount", Map.of("type", "INTEGER"));
        properties.put("date", Map.of("type", "STRING"));
        properties.put("category", Map.of("type", "STRING"));

        responseSchema.put("properties", properties);
        generationConfig.put("responseSchema", responseSchema);

        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.info("Sending request to Gemini API...");
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseGeminiResponse(response.getBody());
            } else {
                logger.error("Gemini API returned non-success status: {}", response.getStatusCode());
                throw new RuntimeException("Gemini API call failed with status " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi kết nối Gemini API: " + e.getMessage(), e);
        }
    }

    private GeminiReceiptResult parseGeminiResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode textNode = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text");

                String rawTextJson = textNode.asText();
                logger.info("Raw JSON response text from Gemini: {}", rawTextJson);

                JsonNode resultNode = objectMapper.readTree(rawTextJson);
                GeminiReceiptResult result = new GeminiReceiptResult();

                if (resultNode.has("merchant") && !resultNode.get("merchant").isNull()) {
                    result.setMerchant(resultNode.get("merchant").asText());
                }

                if (resultNode.has("amount") && !resultNode.get("amount").isNull()) {
                    result.setAmount(new BigDecimal(resultNode.get("amount").asLong()));
                }

                if (resultNode.has("date") && !resultNode.get("date").isNull()) {
                    String dateStr = resultNode.get("date").asText();
                    result.setDate(parseDate(dateStr));
                }

                if (resultNode.has("category") && !resultNode.get("category").isNull()) {
                    result.setCategory(resultNode.get("category").asText());
                } else {
                    result.setCategory("other");
                }

                return result;
            }
        } catch (Exception e) {
            logger.error("Error parsing Gemini JSON response", e);
            throw new RuntimeException("Không thể giải mã phản hồi từ Gemini API", e);
        }
        throw new RuntimeException("Phản hồi từ Gemini API không đúng cấu trúc mong đợi");
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }
        dateStr = dateStr.trim();
        try {
            return LocalDate.parse(dateStr); // YYYY-MM-DD
        } catch (DateTimeParseException e) {
            // Try DD/MM/YYYY
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy");
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ex) {
                // Try YYYY/MM/DD
                try {
                    java.time.format.DateTimeFormatter formatter2 = java.time.format.DateTimeFormatter.ofPattern("yyyy/M/d");
                    return LocalDate.parse(dateStr, formatter2);
                } catch (DateTimeParseException ex2) {
                    // Try DD-MM-YYYY
                    try {
                        java.time.format.DateTimeFormatter formatter3 = java.time.format.DateTimeFormatter.ofPattern("d-M-yyyy");
                        return LocalDate.parse(dateStr, formatter3);
                    } catch (DateTimeParseException ex3) {
                        logger.warn("Failed to parse date '{}' with all formats, fallback to current date or null", dateStr);
                        return null;
                    }
                }
            }
        }
    }

    @Data
    public static class GeminiReceiptResult {
        private String merchant;
        private BigDecimal amount;
        private LocalDate date;
        private String category;
    }
}
