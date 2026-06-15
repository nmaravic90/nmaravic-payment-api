package com.nmaravic.payment.api.kafka;

import com.nmaravic.payment.api.service.PaymentSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentSimulationService paymentSimulationService;

    @KafkaListener(topics = "${kafka.topic.in}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentEvent event) {
        log.info("Received payment event for transactionId: {}", event.getTransactionId());
        paymentSimulationService.simulate(event);
    }
}
