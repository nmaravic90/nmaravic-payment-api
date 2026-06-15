package com.nmaravic.payment.api.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private UUID transactionId;
    private PaymentType paymentType;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String userId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static PaymentEvent buildPaymentEvent(PaymentEvent event, TransactionStatus status) {
        return PaymentEvent.builder()
                .transactionId(event.getTransactionId())
                .paymentType(event.getPaymentType())
                .status(status)
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .userId(event.getUserId())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
