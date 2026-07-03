package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.ParkingPaymentRequest;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import com.nmaravic.payment.api.util.PaymentAmountUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingPaymentServiceTest {

    @InjectMocks
    private ParkingPaymentService service;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private BalanceService balanceService;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @Test
    void getPaymentType_shouldReturnParking() {
        assertThat(service.getPaymentType()).isEqualTo(PaymentType.PARKING);
    }

    @Test
    void validateAndDeduct_shouldDeductCalculatedAmount() {
        ParkingPaymentRequest request = new ParkingPaymentRequest()
                .userId("user_001")
                .zone("1")
                .durationMinutes(60);

        BigDecimal expectedAmount = PaymentAmountUtil.calculateParkingAmount("1", 60);
        service.validateAndDeduct(request);

        verify(balanceService).validateAndSubtractBalance("user_001", expectedAmount);
    }

    @Test
    void toTransaction_shouldDelegateToMapper() {
        ParkingPaymentRequest request = new ParkingPaymentRequest();
        Transaction expected = new Transaction();
        when(transactionMapper.toEntity(request)).thenReturn(expected);

        Transaction result = service.toTransaction(request);
        verify(transactionMapper).toEntity(request);
        assertThat(result).isSameAs(expected);
    }
}