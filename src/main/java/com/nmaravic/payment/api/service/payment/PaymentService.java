package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionResponse;

import java.util.UUID;

public interface PaymentService<T> {

    TransactionResponse process(UUID idempotencyKey, T request);

    PaymentType getPaymentType();
}
