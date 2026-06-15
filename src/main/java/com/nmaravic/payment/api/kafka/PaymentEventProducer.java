package com.nmaravic.payment.api.kafka;

import com.nmaravic.payment.api.model.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    @Value("${kafka.topic.in}")
    private String topicIn;

    @Value("${kafka.topic.out}")
    private String topicOut;

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Sending payment event for transactionId: {}", event.getTransactionId());
        kafkaTemplate.send(topicIn, event.getTransactionId().toString(), event);
    }

    public void sendResultEvent(PaymentEvent event, TransactionStatus status) {
        PaymentEvent resultEvent = PaymentEvent.buildPaymentEvent(event, status);
        log.info("Sending result event with status {} for transactionId: {}", status, event.getTransactionId());
        kafkaTemplate.send(topicOut, event.getTransactionId().toString(), resultEvent);
    }
}


