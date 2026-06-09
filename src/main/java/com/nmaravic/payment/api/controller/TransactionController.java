package com.nmaravic.payment.api.controller;

import com.nmaravic.payment.api.TransactionsApi;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionDetailResponse;
import com.nmaravic.payment.api.model.TransactionHistoryResponse;
import com.nmaravic.payment.api.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionsApi {

    private final TransactionService transactionService;

    @Override
    public ResponseEntity<TransactionDetailResponse> getTransaction(String transactionId){
        return ResponseEntity.ok(
                transactionService.getTransaction(transactionId)
        );
    }

    @Override
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(String userId, Integer page, Integer size, PaymentType type) {
        return ResponseEntity.ok(
                transactionService.getTransactionHistory(userId, page, size, type)
        );
    }
}
