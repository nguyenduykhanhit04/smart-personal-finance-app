package com.example.financebackend.service.ocr;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

@Component
public class ReceiptFeatureExtractor {

    private static final List<String> FOOD_KEYWORDS = List.of(
            "com", "pho", "bun", "banh", "sua", "thit", "ga", "bo", "mi", "my", "trung", "xoi", "chao"
    );
    private static final List<String> DRINK_KEYWORDS = List.of(
            "nuoc", "tra", "cafe", "coffee", "milk", "sinh to", "bia", "pepsi", "coca"
    );
    private static final List<String> TRANSPORT_KEYWORDS = List.of(
            "grab", "taxi", "xang", "petrol", "bus", "gui xe", "parking", "gojek", "be "
    );
    private static final List<String> SHOPPING_KEYWORDS = List.of(
            "ao", "quan", "giay", "dep", "my pham", "do gia dung", "khach hang", "sieuthi", "sieu thi"
    );
    private static final List<String> BILL_KEYWORDS = List.of(
            "dien", "internet", "wifi", "hoa don", "nuoc may", "cuoc", "phi dich vu"
    );

    public ReceiptFeatures extract(ReceiptData receipt) {
        String text = ((receipt.getMerchant() == null ? "" : receipt.getMerchant()) + " " + receipt.getNormalizedText())
                .toLowerCase(Locale.ROOT);

        BigDecimal amount = receipt.getAmount() != null ? receipt.getAmount() : BigDecimal.ZERO;
        int amountBucket = amountBucket(amount);
        int hour = receipt.getHour() != null ? receipt.getHour() : -1;
        int dayOfWeek = receipt.getDate() != null ? toZeroBasedDayOfWeek(receipt.getDate().getDayOfWeek()) : -1;

        int foodCount = countMatches(text, FOOD_KEYWORDS);
        int drinkCount = countMatches(text, DRINK_KEYWORDS);
        int transportCount = countMatches(text, TRANSPORT_KEYWORDS);
        int shoppingCount = countMatches(text, SHOPPING_KEYWORDS);
        int billCount = countMatches(text, BILL_KEYWORDS);

        int merchantSupermarket = containsAny(text, List.of("winmart", "vinmart", "coopmart", "bach hoa xanh", "lotte mart", "go ")) ? 1 : 0;
        int merchantCafe = containsAny(text, List.of("highlands", "phuc long", "the coffee house", "starbucks", "cafe")) ? 1 : 0;
        int merchantTransport = containsAny(text, List.of("grab", "taxi", "gojek", "be ")) ? 1 : 0;
        int merchantConvenienceStore = containsAny(text, List.of("circle k", "gs25", "familymart", "ministop")) ? 1 : 0;

        List<Double> vector = List.of(
                amount.doubleValue(),
                (double) amountBucket,
                (double) hour,
                (double) dayOfWeek,
                (double) merchantSupermarket,
                (double) merchantCafe,
                (double) merchantTransport,
                (double) merchantConvenienceStore,
                foodCount > 0 ? 1.0 : 0.0,
                drinkCount > 0 ? 1.0 : 0.0,
                transportCount > 0 ? 1.0 : 0.0,
                shoppingCount > 0 ? 1.0 : 0.0,
                billCount > 0 ? 1.0 : 0.0,
                (double) foodCount,
                (double) drinkCount,
                (double) transportCount,
                (double) shoppingCount,
                (double) billCount
        );

        return ReceiptFeatures.builder()
                .amount(amount)
                .amountBucket(amountBucket)
                .hour(hour)
                .dayOfWeek(dayOfWeek)
                .merchantSupermarket(merchantSupermarket)
                .merchantCafe(merchantCafe)
                .merchantTransport(merchantTransport)
                .merchantConvenienceStore(merchantConvenienceStore)
                .hasFoodKeyword(foodCount > 0 ? 1 : 0)
                .hasDrinkKeyword(drinkCount > 0 ? 1 : 0)
                .hasTransportKeyword(transportCount > 0 ? 1 : 0)
                .hasShoppingKeyword(shoppingCount > 0 ? 1 : 0)
                .hasBillKeyword(billCount > 0 ? 1 : 0)
                .foodKeywordCount(foodCount)
                .drinkKeywordCount(drinkCount)
                .transportKeywordCount(transportCount)
                .shoppingKeywordCount(shoppingCount)
                .billKeywordCount(billCount)
                .vector(vector)
                .build();
    }

    private int amountBucket(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(30_000)) < 0) {
            return 0;
        }
        if (amount.compareTo(BigDecimal.valueOf(100_000)) < 0) {
            return 1;
        }
        if (amount.compareTo(BigDecimal.valueOf(500_000)) < 0) {
            return 2;
        }
        return 3;
    }

    private int toZeroBasedDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue() - 1;
    }

    private int countMatches(String text, List<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAny(String text, List<String> keywords) {
        return countMatches(text, keywords) > 0;
    }
}
