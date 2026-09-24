package com.example.financebackend.config;

import java.time.LocalDate;
import java.time.ZoneId;

public final class AppTime {

    public static final String TIME_ZONE = "Asia/Ho_Chi_Minh";
    private static final ZoneId ZONE_ID = ZoneId.of(TIME_ZONE);

    private AppTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE_ID);
    }
}
