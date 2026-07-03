package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.kafka.PaymentEvent;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.service.BalanceService;
import com.nmaravic.payment.api.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractPaymentServiceTest {

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

    private TestPaymentService service;

    private static class TestPaymentService extends AbstractPaymentService<String> {

        boolean validateAndDeductCalled = false;

        TestPaymentService(IdempotencyService idempotencyService,
                           TransactionRepository transactionRepository,
                           TransactionMapper transactionMapper,
                           BalanceService balanceService,
                           PaymentEventProducer paymentEventProducer) {
            super(idempotencyService,
                    transactionRepository,
                    transactionMapper,
                    balanceService,
                    paymentEventProducer);
        }

        @Override
        protected Transaction toTransaction(String request) {
            return new Transaction();
        }

        @Override
        protected void validateAndDeduct(String request) {
            validateAndDeductCalled = true;
        }

        @Override
        public TransactionResponse process(UUID idempotencyKey, String request) {
            return executePayment(idempotencyKey, request);
        }

        @Override
        public PaymentType getPaymentType() {
            return PaymentType.BILL;
        }
    }

    @BeforeEach
    void setUp() {
        service = new TestPaymentService(idempotencyService, transactionRepository,
                transactionMapper, balanceService, paymentEventProducer);
    }

    @Test
    void executePayment_whenCachedResponseExists_shouldReturnCachedAndSkipProcessing() {
        UUID key = UUID.randomUUID();
        TransactionResponse cached = new TransactionResponse().transactionId("txn_cached");
        when(idempotencyService.findCachedResponse(key)).thenReturn(Optional.of(cached));

        TransactionResponse result = service.process(key, "request");
        verify(transactionRepository, never()).save(any());
        verify(paymentEventProducer, never()).sendPaymentEvent(any());
        verify(idempotencyService, never()).saveResponse(any(), any());

        assertThat(result.getTransactionId()).isEqualTo("txn_cached");
        assertThat(service.validateAndDeductCalled).isFalse();
    }

    @Test
    void executePayment_whenNoCachedResponse_shouldProcessFullFlow() {
        UUID key = UUID.randomUUID();
        TransactionResponse response = new TransactionResponse().transactionId("txn_new");
        PaymentEvent event = PaymentEvent.builder().build();

        when(idempotencyService.findCachedResponse(key)).thenReturn(Optional.empty());
        when(transactionMapper.toResponse(any())).thenReturn(response);
        when(transactionMapper.toEvent(any(), eq(PaymentType.BILL))).thenReturn(event);

        TransactionResponse result = service.process(key, "request");

        verify(transactionRepository).save(any(Transaction.class));
        verify(paymentEventProducer).sendPaymentEvent(event);
        verify(idempotencyService).saveResponse(key, response);

        assertThat(result.getTransactionId()).isEqualTo("txn_new");
        assertThat(service.validateAndDeductCalled).isTrue();
    }

    @Test
    void executePayment_shouldExecuteStepsInOrder() {
        UUID key = UUID.randomUUID();
        TransactionResponse response = new TransactionResponse();
        PaymentEvent event = PaymentEvent.builder().build();

        when(idempotencyService.findCachedResponse(key)).thenReturn(Optional.empty());
        when(transactionMapper.toResponse(any())).thenReturn(response);
        when(transactionMapper.toEvent(any(), any())).thenReturn(event);

        service.process(key, "request");

        InOrder inOrder = inOrder(transactionRepository, paymentEventProducer, idempotencyService);
        inOrder.verify(transactionRepository).save(any());
        inOrder.verify(paymentEventProducer).sendPaymentEvent(any());
        inOrder.verify(idempotencyService).saveResponse(any(), any());
    }
}