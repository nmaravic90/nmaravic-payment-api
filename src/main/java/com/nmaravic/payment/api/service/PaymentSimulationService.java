package com.nmaravic.payment.api.service;

import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.kafka.PaymentEvent;
import com.nmaravic.payment.api.kafka.PaymentEventProducer;
import com.nmaravic.payment.api.model.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSimulationService {

    @Value("${payment.simulation.failure.rate:0.1}")
    private double simulationFailureRate;

    @Value("${payment.simulation.delay:2000}")
    private long simulationDelay;

    private final TransactionRepository transactionRepository;
    private final PaymentEventProducer paymentEventProducer;

    public void simulate(PaymentEvent event) {
        log.info("Simulating payment for transactionId: {}", event.getTransactionId());
        simulateDelay();
        TransactionStatus status = processPayment(event);
        updateTransactionStatus(event.getTransactionId(), status);
        paymentEventProducer.sendResultEvent(event, status);
    }

    private void simulateDelay() {
        log.info("Processing payment — estimated time: {}ms", simulationDelay);
        try {
            Thread.sleep(simulationDelay);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private TransactionStatus processPayment(PaymentEvent event) {
        boolean success = ThreadLocalRandom.current().nextDouble() > simulationFailureRate;
        if (success) {
            log.info("Payment successful for transactionId: {}", event.getTransactionId());
            return TransactionStatus.SUCCESS;
        }
        log.warn("Payment failed for transactionId: {}", event.getTransactionId());
        return TransactionStatus.FAILED;
    }

    private void updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        transactionRepository.findById(transactionId)
                .ifPresent(transaction -> {
                    transaction.setStatus(status);
                    transactionRepository.save(transaction);
                    log.info("Transaction {} updated to {}", transactionId, status);
                });
    }
}
