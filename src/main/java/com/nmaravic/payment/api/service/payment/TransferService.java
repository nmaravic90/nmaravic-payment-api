package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.exception.InvalidTransferException;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.model.TransferRequest;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService implements PaymentService<TransferRequest> {

    private final IdempotencyService idempotencyService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public TransactionResponse process(UUID idempotencyKey, TransferRequest transferRequest) {
        return idempotencyService.findCachedResponse(idempotencyKey)
                .orElseGet(() -> processPayment(idempotencyKey, transferRequest));
    }

    private TransactionResponse processPayment(UUID idempotencyKey, TransferRequest transferRequest) {
        if (transferRequest.getSenderId().equals(transferRequest.getReceiverId())) {
            throw new InvalidTransferException("Sender and receiver cannot be the same user");
        }

        balanceService.validateAndSubtractBalance(
                transferRequest.getSenderId(),
                BigDecimal.valueOf(transferRequest.getAmount())
        );
        Transaction transaction = transactionMapper.toEntity(transferRequest);
        transactionRepository.save(transaction);
        TransactionResponse transactionResponse = transactionMapper.toResponse(transaction);
        idempotencyService.saveResponse(idempotencyKey, transactionResponse);
        return transactionResponse;
    }
}