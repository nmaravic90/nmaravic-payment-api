package com.nmaravic.payment.api.service;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.exception.TransactionNotFoundException;
import com.nmaravic.payment.api.kafka.PaymentEvent;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSimulationServiceTest {

    @InjectMocks
    private PaymentSimulationService paymentSimulationService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentSimulationService, "simulationDelay", 0L);
    }

    @Test
    void simulate_pendingTransaction_shouldSucceed() {
        ReflectionTestUtils.setField(paymentSimulationService, "simulationFailureRate", 0.0);

        UUID transactionId = UUID.randomUUID();
        PaymentEvent event = buildEvent(transactionId);
        Transaction transaction = buildTransaction(transactionId, TransactionStatus.PENDING);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        paymentSimulationService.simulate(event);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        verify(paymentEventProducer).sendResultEvent(event, TransactionStatus.SUCCESS);
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    void simulate_pendingTransaction_shouldFail() {
        ReflectionTestUtils.setField(paymentSimulationService, "simulationFailureRate", 1.0);

        UUID transactionId = UUID.randomUUID();
        PaymentEvent event = buildEvent(transactionId);
        Transaction transaction = buildTransaction(transactionId, TransactionStatus.PENDING);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        paymentSimulationService.simulate(event);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.FAILED);
        verify(paymentEventProducer).sendResultEvent(event, TransactionStatus.FAILED);
    }

    @Test
    void simulate_alreadyProcessed_shouldSkip() {
        UUID transactionId = UUID.randomUUID();
        PaymentEvent event = buildEvent(transactionId);
        Transaction transaction = buildTransaction(transactionId, TransactionStatus.SUCCESS);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        paymentSimulationService.simulate(event);
        verify(transactionRepository, never()).save(any());
        verify(paymentEventProducer, never()).sendResultEvent(any(), any());
    }

    @Test
    void simulate_alreadyFailed_shouldSkip() {
        UUID transactionId = UUID.randomUUID();
        PaymentEvent event = buildEvent(transactionId);
        Transaction transaction = buildTransaction(transactionId, TransactionStatus.FAILED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        paymentSimulationService.simulate(event);
        verify(transactionRepository, never()).save(any());
        verify(paymentEventProducer, never()).sendResultEvent(any(), any());
    }

    @Test
    void simulate_transactionMissing_shouldThrow() {
        UUID transactionId = UUID.randomUUID();
        PaymentEvent event = buildEvent(transactionId);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentSimulationService.simulate(event)).isInstanceOf(TransactionNotFoundException.class);
        verify(transactionRepository, never()).save(any());
        verify(paymentEventProducer, never()).sendResultEvent(any(), any());
    }

    private PaymentEvent buildEvent(UUID transactionId) {
        return PaymentEvent.builder()
                .transactionId(transactionId)
                .build();
    }

    private Transaction buildTransaction(UUID id, TransactionStatus status) {
        return Transaction.builder()
                .id(id)
                .status(status)
                .build();
    }
}