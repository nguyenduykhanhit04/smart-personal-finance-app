package com.example.financebackend.service.ocr;

import com.example.financebackend.config.AppTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReceiptParser {

    private final OcrTextNormalizer normalizer;

    public ReceiptParser(OcrTextNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public ReceiptData parse(String rawText) {
        String normalizedText = normalizer.normalize(rawText);

        return ReceiptData.builder()
                .rawText(rawText)
                .normalizedText(normalizedText)
                .merchant(extractMerchant(rawText))
                .amount(extractAmount(normalizedText))
                .date(extractDate(normalizedText))
                .hour(extractHour(normalizedText))
                .build();
    }

    private BigDecimal extractAmount(String text) {
        BigDecimal amountAfterTotalKeyword = extractLargestAmountAfterTotalKeyword(text);
        if (isRealisticAmount(amountAfterTotalKeyword)) {
            return amountAfterTotalKeyword;
        }

        Pattern[] totalPatterns = {
                Pattern.compile("(?i)(tong cong|tong tien|thanh tien|thanh toan|can tra|total|amount|cash)\\s*:?\\s*([0-9.,oO]+)"),
                Pattern.compile("(?i)(vnd|d)\\s*([0-9.,oO]+)")
        };

        for (Pattern pattern : totalPatterns) {
            Matcher matcher = pattern.matcher(text);
            BigDecimal latest = null;
            while (matcher.find()) {
                String value = matcher.groupCount() >= 2 && matcher.group(2).matches("[0-9.,oO]+")
                        ? matcher.group(2)
                        : matcher.group(1);
                BigDecimal parsed = parseMoney(value);
                if (isRealisticAmount(parsed)) {
                    latest = parsed;
                }
            }
            if (latest != null) {
                return latest;
            }
        }

        Matcher matcher = Pattern.compile("\\b([0-9]{1,3}([,.][0-9oO]{3})+|[0-9]{4,9})\\b").matcher(text);
        BigDecimal maxValue = BigDecimal.ZERO;
        while (matcher.find()) {
            BigDecimal parsed = parseMoney(matcher.group(1));
            if (isRealisticAmount(parsed) && parsed.compareTo(maxValue) > 0) {
                maxValue = parsed;
            }
        }

        return maxValue;
    }

    private BigDecimal extractLargestAmountAfterTotalKeyword(String text) {
        Matcher totalMatcher = Pattern.compile("(?i)(tong cong|tong tien|thanh toan|can tra|total)").matcher(text);
        if (!totalMatcher.find()) {
            return BigDecimal.ZERO;
        }

        int start = totalMatcher.end();
        int end = Math.min(text.length(), start + 1500);
        String window = text.substring(start, end);
        return findLargestMoneyCandidate(window);
    }

    private BigDecimal findLargestMoneyCandidate(String text) {
        Matcher matcher = Pattern.compile("\\b([0-9]{1,3}([,.][0-9oO]{3})+|[0-9]{4,9})\\b").matcher(text);
        BigDecimal maxValue = BigDecimal.ZERO;
        while (matcher.find()) {
            BigDecimal parsed = parseMoney(matcher.group(1));
            if (isRealisticAmount(parsed) && parsed.compareTo(maxValue) > 0) {
                maxValue = parsed;
            }
        }
        return maxValue;
    }

    private BigDecimal parseMoney(String value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        String cleanValue = value
                .replace('o', '0')
                .replace('O', '0')
                .replace(".", "")
                .replace(",", "")
                .trim();

        if (cleanValue.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isRealisticAmount(BigDecimal value) {
        return value != null
                && value.compareTo(BigDecimal.ZERO) > 0
                && value.compareTo(BigDecimal.valueOf(50_000_000)) < 0;
    }

    private LocalDate extractDate(String text) {
        Pattern dayFirst = Pattern.compile("\\b([0-3]?\\d)[/-]([01]?\\d)[/-](20\\d{2})\\b");
        Matcher dayFirstMatcher = dayFirst.matcher(text);
        if (dayFirstMatcher.find()) {
            LocalDate parsed = buildDate(
                    dayFirstMatcher.group(3),
                    dayFirstMatcher.group(2),
                    dayFirstMatcher.group(1)
            );
            if (parsed != null) {
                return parsed;
            }
        }

        Pattern yearFirst = Pattern.compile("\\b(20\\d{2})[/-]([01]?\\d)[/-]([0-3]?\\d)\\b");
        Matcher yearFirstMatcher = yearFirst.matcher(text);
        if (yearFirstMatcher.find()) {
            LocalDate parsed = buildDate(
                    yearFirstMatcher.group(1),
                    yearFirstMatcher.group(2),
                    yearFirstMatcher.group(3)
            );
            if (parsed != null) {
                return parsed;
            }
        }

        return AppTime.today();
    }

    private LocalDate buildDate(String year, String month, String day) {
        try {
            return LocalDate.of(
                    Integer.parseInt(year),
                    Integer.parseInt(month),
                    Integer.parseInt(day)
            );
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Integer extractHour(String text) {
        Matcher matcher = Pattern.compile("\\b([01]?\\d|2[0-3]):[0-5]\\d\\b").matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private String extractMerchant(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "Cua hang ban le";
        }

        String normalizedText = normalizer.normalize(rawText);
        String knownMerchant = extractKnownMerchant(normalizedText);
        if (knownMerchant != null) {
            return knownMerchant;
        }

        String[] lines = rawText.split("\\R");
        for (int i = 0; i < Math.min(4, lines.length); i++) {
            String line = lines[i].trim();
            String normalizedLine = normalizer.normalize(line);
            if (line.length() > 2
                    && !normalizedLine.matches("[0-9\\s.,:/-]+")
                    && !normalizedLine.contains("hoa don")
                    && !normalizedLine.contains("dia chi")
                    && !normalizedLine.contains("ngay")) {
                return line;
            }
        }

        return "Cua hang ban le";
    }

    private String extractKnownMerchant(String normalizedText) {
        if (normalizedText.contains("tops market")) {
            return "Tops Market";
        }
        if (normalizedText.contains("winmart") || normalizedText.contains("win mart") || normalizedText.contains("vinmart")) {
            return "WinMart";
        }
        if (normalizedText.contains("coopmart") || normalizedText.contains("co.opmart")) {
            return "Coopmart";
        }
        if (normalizedText.contains("bach hoa xanh")) {
            return "Bach Hoa Xanh";
        }
        if (normalizedText.contains("lotte mart")) {
            return "Lotte Mart";
        }
        if (normalizedText.contains("circle k")) {
            return "Circle K";
        }
        if (normalizedText.contains("gs25")) {
            return "GS25";
        }
        if (normalizedText.contains("highlands")) {
            return "Highlands Coffee";
        }
        if (normalizedText.contains("phuc long")) {
            return "Phuc Long";
        }
        return null;
    }
}
