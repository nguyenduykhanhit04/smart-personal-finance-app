package com.example.financebackend.service.ocr;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class OcrTextNormalizer {

    public String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);

        normalized = normalized
                .replaceAll("(?<=\\d)[oO](?=\\d)", "0")
                .replaceAll("[^a-z0-9:/.,\\-\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return normalized;
    }
}
