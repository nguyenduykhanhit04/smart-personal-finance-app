package com.example.financebackend.service.ocr;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ReceiptFeatures {
    BigDecimal amount;
    int amountBucket;
    int hour;
    int dayOfWeek;
    int merchantSupermarket;
    int merchantCafe;
    int merchantTransport;
    int merchantConvenienceStore;
    int hasFoodKeyword;
    int hasDrinkKeyword;
    int hasTransportKeyword;
    int hasShoppingKeyword;
    int hasBillKeyword;
    int foodKeywordCount;
    int drinkKeywordCount;
    int transportKeywordCount;
    int shoppingKeywordCount;
    int billKeywordCount;
    List<Double> vector;
}
