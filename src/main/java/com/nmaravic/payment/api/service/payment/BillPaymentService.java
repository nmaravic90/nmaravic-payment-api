package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.BillPaymentRequest;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BillPaymentService extends AbstractPaymentService<BillPaymentRequest> {

    public BillPaymentService(IdempotencyService idempotencyService,
                              TransactionRepository transactionRepository,
                              TransactionMapper transactionMapper,
                              BalanceService balanceService,
                              PaymentEventProducer paymentEventProducer) {
        super(idempotencyService, transactionRepository, transactionMapper, balanceService, paymentEventProducer);
    }

    @Override
    @Transactional
    public TransactionResponse process(UUID idempotencyKey, BillPaymentRequest request) {
        return executePayment(idempotencyKey, request);
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BILL;
    }

    @Override
    protected void validateAndDeduct(BillPaymentRequest request) {
        balanceService.validateAndSubtractBalance(request.getUserId(), BigDecimal.valueOf(request.getAmount()));
    }

    @Override
    protected Transaction toTransaction(BillPaymentRequest request) {
        return transactionMapper.toEntity(request);
    }
}
