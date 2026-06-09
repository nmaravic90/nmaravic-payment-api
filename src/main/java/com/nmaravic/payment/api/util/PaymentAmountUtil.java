package com.nmaravic.payment.api.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class PaymentAmountUtil {

    public BigDecimal calculateParkingAmount(String zone, Integer durationMinutes) {
        BigDecimal pricePerMinute = switch (zone) {
            case "1" -> new BigDecimal("5.00");
            case "2" -> new BigDecimal("3.00");
            case "3" -> new BigDecimal("1.00");
            default  -> new BigDecimal("2.00");
        };
        return pricePerMinute.multiply(BigDecimal.valueOf(durationMinutes));
    }
}
