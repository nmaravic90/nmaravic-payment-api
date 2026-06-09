package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.BillPaymentRequest;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillPaymentService implements PaymentService<BillPaymentRequest> {

    private final IdempotencyService idempotencyService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public TransactionResponse process(UUID idempotencyKey, BillPaymentRequest billPaymentRequest) {
        return idempotencyService.findCachedResponse(idempotencyKey)
                .map(cached -> logAndReturnCached(idempotencyKey, cached))
                .orElseGet(() -> processPayment(idempotencyKey, billPaymentRequest));
    }

    private TransactionResponse processPayment(UUID idempotencyKey, BillPaymentRequest billPaymentRequest) {
        balanceService.validateAndSubtractBalance(billPaymentRequest.getUserId(), BigDecimal.valueOf(billPaymentRequest.getAmount()));
        Transaction transaction = transactionMapper.toEntity(billPaymentRequest);
        transactionRepository.save(transaction);
        TransactionResponse transactionResponse = transactionMapper.toResponse(transaction);
        idempotencyService.saveResponse(idempotencyKey, transactionResponse);
        return transactionResponse;
    }

    private TransactionResponse logAndReturnCached(UUID idempotencyKey, TransactionResponse cached) {
        log.info("Duplicate request detected for idempotency key: {}", idempotencyKey);
        return cached;
    }
}
