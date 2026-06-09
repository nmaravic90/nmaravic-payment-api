package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.ParkingPaymentRequest;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import com.nmaravic.payment.api.util.PaymentAmountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParkingPaymentService implements PaymentService<ParkingPaymentRequest> {

    private final IdempotencyService idempotencyService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceService balanceService;

    @Override
    @Transactional
    public TransactionResponse process(UUID idempotencyKey, ParkingPaymentRequest parkingPaymentRequest) {
        return idempotencyService.findCachedResponse(idempotencyKey)
                .orElseGet(() -> processPayment(idempotencyKey, parkingPaymentRequest));
    }

    private TransactionResponse processPayment(UUID idempotencyKey, ParkingPaymentRequest parkingPaymentRequest) {
        balanceService.validateAndSubtractBalance(parkingPaymentRequest.getUserId(),
                PaymentAmountUtil.calculateParkingAmount(parkingPaymentRequest.getZone(), parkingPaymentRequest.getDurationMinutes()));

        Transaction transaction = transactionMapper.toEntity(parkingPaymentRequest);
        transactionRepository.save(transaction);
        TransactionResponse transactionResponse = transactionMapper.toResponse(transaction);
        idempotencyService.saveResponse(idempotencyKey, transactionResponse);
        return transactionResponse;
    }
}
