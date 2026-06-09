package com.nmaravic.payment.api.mapper;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.model.*;
import com.nmaravic.payment.api.util.PaymentAmountUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionMapper {

    public Transaction toEntity(BillPaymentRequest request) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .type(PaymentType.BILL)
                .status(TransactionStatus.SUCCESS)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .currency(request.getCurrency())
                .userId(request.getUserId())
                .billCode(request.getBillCode())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Transaction toEntity(TransferRequest request) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .type(PaymentType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .currency(request.getCurrency())
                .userId(request.getSenderId())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Transaction toEntity(ParkingPaymentRequest request) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .type(PaymentType.PARKING)
                .status(TransactionStatus.SUCCESS)
                .amount(PaymentAmountUtil.calculateParkingAmount(
                        request.getZone(),
                        request.getDurationMinutes()
                ))
                .currency(request.getCurrency())
                .userId(request.getUserId())
                .licensePlate(request.getLicensePlate())
                .zone(request.getZone())
                .validUntil(LocalDateTime.now().plusMinutes(request.getDurationMinutes()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Transaction toEntity(QrCodePaymentRequest request) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .type(PaymentType.QR_CODE)
                .status(TransactionStatus.SUCCESS)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .currency(request.getCurrency())
                .userId(request.getUserId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse()
                .transactionId(transaction.getId().toString())
                .status(transaction.getStatus())
                .amount(transaction.getAmount().doubleValue())
                .currency(transaction.getCurrency())
                .timestamp(transaction.getCreatedAt());
    }

    public TransactionDetailResponse toDetailResponse(Transaction transaction) {
        return new TransactionDetailResponse()
                .transactionId(transaction.getId().toString())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount().doubleValue())
                .currency(transaction.getCurrency())
                .senderId(transaction.getSenderId())
                .receiverId(transaction.getReceiverId())
                .billCode(transaction.getBillCode())
                .licensePlate(transaction.getLicensePlate())
                .timestamp(transaction.getCreatedAt());
    }
}
