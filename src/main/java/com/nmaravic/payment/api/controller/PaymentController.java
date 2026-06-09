package com.nmaravic.payment.api.controller;

import com.nmaravic.payment.api.PaymentsApi;

import com.nmaravic.payment.api.model.*;
import com.nmaravic.payment.api.service.payment.PaymentService;
import com.nmaravic.payment.api.service.payment.PaymentServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentsApi {

    private final PaymentServiceFactory paymentServiceFactory;

    @Override
    public ResponseEntity<TransactionResponse> payBill(UUID idempotencyKey, BillPaymentRequest billPaymentRequest) {
        PaymentService<BillPaymentRequest> service = paymentServiceFactory.getService(PaymentType.BILL);

        return ResponseEntity.status(201)
                .body(service.process(idempotencyKey, billPaymentRequest));
    }

    @Override
    public ResponseEntity<TransactionResponse> payParking(UUID idempotencyKey, ParkingPaymentRequest parkingPaymentRequest) {
        PaymentService<ParkingPaymentRequest> service = paymentServiceFactory.getService(PaymentType.PARKING);

        return ResponseEntity.status(201)
                .body(service.process(idempotencyKey, parkingPaymentRequest));
    }

    @Override
    public ResponseEntity<TransactionResponse> payQrCode(UUID idempotencyKey, QrCodePaymentRequest qrCodePaymentRequest) {
        PaymentService<QrCodePaymentRequest> service = paymentServiceFactory.getService(PaymentType.QR_CODE);

        return ResponseEntity.status(201)
                .body(service.process(idempotencyKey, qrCodePaymentRequest));
    }

    @Override
    public ResponseEntity<TransactionResponse> transferFunds(UUID idempotencyKey, TransferRequest transferRequest) {
        PaymentService<TransferRequest> service = paymentServiceFactory.getService(PaymentType.TRANSFER);

        return ResponseEntity.status(201)
                .body(service.process(idempotencyKey, transferRequest));
    }
}
