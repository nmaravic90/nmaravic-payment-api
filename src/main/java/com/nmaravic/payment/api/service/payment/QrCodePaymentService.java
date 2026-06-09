package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.QrCodePaymentRequest;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodePaymentService implements PaymentService<QrCodePaymentRequest> {

    private final IdempotencyService idempotencyService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public TransactionResponse process(UUID idempotencyKey, QrCodePaymentRequest qrCodePaymentRequest) {
        return idempotencyService.findCachedResponse(idempotencyKey)
                .orElseGet(() -> processPayment(idempotencyKey, qrCodePaymentRequest));
    }

    private TransactionResponse processPayment(UUID idempotencyKey, QrCodePaymentRequest qrCodePaymentRequest) {
        balanceService.validateAndSubtractBalance(
                qrCodePaymentRequest.getUserId(),
                BigDecimal.valueOf(qrCodePaymentRequest.getAmount()));

        Transaction transaction = transactionMapper.toEntity(qrCodePaymentRequest);
        transactionRepository.save(transaction);
        TransactionResponse transactionResponse = transactionMapper.toResponse(transaction);
        idempotencyService.saveResponse(idempotencyKey, transactionResponse);
        return transactionResponse;
    }
}
