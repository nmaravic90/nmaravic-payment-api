package com.nmaravic.payment.api.controller;

import com.nmaravic.payment.api.model.*;
import com.nmaravic.payment.api.service.payment.PaymentService;
import com.nmaravic.payment.api.service.payment.PaymentServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentServiceFactory paymentServiceFactory;

    @Mock
    private PaymentService<Object> paymentService;

    private UUID idempotencyKey;
    private TransactionResponse response;

    @BeforeEach
    void setUp() {
        idempotencyKey = UUID.randomUUID();
        response = new TransactionResponse().transactionId("txn_123");
    }

    @Test
    void payBill_shouldReturnCreated() {
        BillPaymentRequest request = new BillPaymentRequest();
        when(paymentServiceFactory.<BillPaymentRequest>getService(PaymentType.BILL)).thenReturn(castService());
        when(paymentService.process(eq(idempotencyKey), any())).thenReturn(response);

        ResponseEntity<TransactionResponse> result = paymentController.payBill(idempotencyKey, request);

        verify(paymentServiceFactory).getService(PaymentType.BILL);
        verify(paymentService).process(idempotencyKey, request);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void payParking_shouldReturnCreated() {
        ParkingPaymentRequest request = new ParkingPaymentRequest();
        when(paymentServiceFactory.<ParkingPaymentRequest>getService(PaymentType.PARKING)).thenReturn(castService());
        when(paymentService.process(eq(idempotencyKey), any())).thenReturn(response);

        ResponseEntity<TransactionResponse> result = paymentController.payParking(idempotencyKey, request);

        verify(paymentServiceFactory).getService(PaymentType.PARKING);
        verify(paymentService).process(idempotencyKey, request);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void payQrCode_shouldReturnCreated() {
        QrCodePaymentRequest request = new QrCodePaymentRequest();
        when(paymentServiceFactory.<QrCodePaymentRequest>getService(PaymentType.QR_CODE))
                .thenReturn(castService());
        when(paymentService.process(eq(idempotencyKey), any())).thenReturn(response);

        ResponseEntity<TransactionResponse> result = paymentController.payQrCode(idempotencyKey, request);

        verify(paymentServiceFactory).getService(PaymentType.QR_CODE);
        verify(paymentService).process(idempotencyKey, request);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void transferFunds_shouldReturnCreated() {
        TransferRequest request = new TransferRequest();
        when(paymentServiceFactory.<TransferRequest>getService(PaymentType.TRANSFER)).thenReturn(castService());
        when(paymentService.process(eq(idempotencyKey), any())).thenReturn(response);

        ResponseEntity<TransactionResponse> result = paymentController.transferFunds(idempotencyKey, request);

        verify(paymentServiceFactory).getService(PaymentType.TRANSFER);
        verify(paymentService).process(idempotencyKey, request);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @SuppressWarnings("unchecked")
    private <T> PaymentService<T> castService() {
        return (PaymentService<T>) paymentService;
    }
}