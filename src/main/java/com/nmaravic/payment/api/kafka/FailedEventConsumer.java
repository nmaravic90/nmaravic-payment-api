package com.nmaravic.payment.api.kafka;

import com.nmaravic.payment.api.database.entitymodel.FailedEvent;
import com.nmaravic.payment.api.database.repository.FailedEventRepository;
import com.nmaravic.payment.api.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedEventConsumer {

    private final FailedEventRepository failedEventRepository;

    @KafkaListener(topics = "${kafka.topic.error}", groupId = "${spring.kafka.consumer.group-id}-error")
    public void consumeFailedEvent(@Payload PaymentEvent event,
            @Header(name = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) byte[] originalTopic,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) byte[] exceptionClass,
            @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) byte[] exceptionMessage) {

        log.warn("Consuming failed event from ERROR topic for transactionId: {}", event.getTransactionId());
        try {
            FailedEvent failedEvent = buildFailedEvent(event, originalTopic, exceptionClass, exceptionMessage);
            failedEventRepository.save(failedEvent);
            log.info("Failed event stored in DB table for transactionId: {}", event.getTransactionId());
        }
        catch (Exception e) {
            log.error("Could not stored failed event for transactionId: {}", event.getTransactionId(), e);
        }
    }

    private FailedEvent buildFailedEvent(PaymentEvent event, byte[] originalTopic, byte[] exceptionClass, byte[] exceptionMessage) {
        FailedEvent failedEvent = new FailedEvent();
        failedEvent.setTransactionId(event.getTransactionId());
        failedEvent.setOriginalTopic(asString(originalTopic));
        failedEvent.setPayload(JsonUtil.serialize(event));
        failedEvent.setExceptionClass(asString(exceptionClass));
        failedEvent.setExceptionMessage(asString(exceptionMessage));
        failedEvent.setFailedAt(LocalDateTime.now());
        return failedEvent;
    }

    private String asString(byte[] header) {
        return header != null ? new String(header, StandardCharsets.UTF_8) : null;
    }
}

