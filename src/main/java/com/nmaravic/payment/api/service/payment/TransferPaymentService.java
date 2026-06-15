package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.model.TransferRequest;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferPaymentService extends AbstractPaymentService<TransferRequest> {

    public TransferPaymentService(IdempotencyService idempotencyService,
                                  TransactionRepository transactionRepository,
                                  TransactionMapper transactionMapper,
                                  BalanceService balanceService,
                                  PaymentEventProducer paymentEventProducer) {
        super(idempotencyService, transactionRepository, transactionMapper, balanceService, paymentEventProducer);
    }

    @Override
    @Transactional
    public TransactionResponse process(UUID idempotencyKey, TransferRequest request) {
        return executePayment(idempotencyKey, request);
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.TRANSFER;
    }

    @Override
    protected void validateAndDeduct(TransferRequest request) {
        balanceService.validateAndSubtractBalance(request.getSenderId(), BigDecimal.valueOf(request.getAmount()));
    }

    @Override
    protected Transaction toTransaction(TransferRequest request) {
        return transactionMapper.toEntity(request);
    }
}