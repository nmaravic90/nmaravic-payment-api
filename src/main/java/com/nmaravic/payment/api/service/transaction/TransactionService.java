package com.nmaravic.payment.api.service.transaction;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.exception.TransactionNotFoundException;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionDetailResponse;
import com.nmaravic.payment.api.model.TransactionHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionDetailResponse getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        return transactionMapper.toDetailResponse(transaction);
    }

    public TransactionHistoryResponse getTransactionHistory(String userId, Integer page, Integer size, PaymentType type) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = fetchTransactions(userId, type, pageable);
        return toHistoryResponse(transactions);
    }

    private Page<Transaction> fetchTransactions(String userId, PaymentType type, Pageable pageable) {
        if (type != null) {
            return transactionRepository.findByUserIdAndType(userId, type, pageable);
        }
        return transactionRepository.findByUserId(userId, pageable);
    }

    private TransactionHistoryResponse toHistoryResponse(Page<Transaction> transactions) {
        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setContent(transactions.getContent().stream()
                .map(transactionMapper::toDetailResponse)
                .toList());
        response.setTotalElements((int) transactions.getTotalElements());
        response.setTotalPages(transactions.getTotalPages());
        response.setCurrentPage(transactions.getNumber());
        response.setPageSize(transactions.getSize());
        return response;
    }
}
