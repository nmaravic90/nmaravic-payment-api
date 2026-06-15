package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.kafka.PaymentEvent;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AbstractPaymentService <T> implements PaymentService<T> {

    protected final IdempotencyService idempotencyService;
    protected final TransactionRepository transactionRepository;
    protected final TransactionMapper transactionMapper;
    protected final BalanceService balanceService;
    protected final PaymentEventProducer paymentEventProducer;

    protected abstract Transaction toTransaction(T request);

    protected abstract void validateAndDeduct(T request);

    protected TransactionResponse executePayment(UUID idempotencyKey, T request) {
        return idempotencyService.findCachedResponse(idempotencyKey)
                .map(cached -> logAndReturnCached(idempotencyKey, cached))
                .orElseGet(() -> processPayment(idempotencyKey, request));
    }

    private TransactionResponse processPayment(UUID idempotencyKey, T request) {
        validateAndDeduct(request);
        Transaction transaction = toTransaction(request);
        transactionRepository.save(transaction);
        simulatePayment(transaction, getPaymentType());
        TransactionResponse response = transactionMapper.toResponse(transaction);
        idempotencyService.saveResponse(idempotencyKey, response);
        return response;
    }

    private TransactionResponse logAndReturnCached(UUID idempotencyKey, TransactionResponse cached) {
        log.info("Duplicate request detected for idempotency key: {}", idempotencyKey);
        return cached;
    }

    protected void simulatePayment(Transaction transaction, PaymentType paymentType) {
        PaymentEvent event = transactionMapper.toEvent(transaction, paymentType);
        paymentEventProducer.sendPaymentEvent(event);
    }
}
