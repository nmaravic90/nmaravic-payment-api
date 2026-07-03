package com.nmaravic.payment.api.config;

import com.nmaravic.payment.api.kafka.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaErrorHandlerConfig {

    @Value("${kafka.topic.error}")
    private String kafkaTopicError;

    @Bean
    public NewTopic errorTopic() {
        return new NewTopic(kafkaTopicError, 1, (short) 1);
    }

    @Bean
    public ConsumerRecordRecoverer recoverer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer delegate = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) -> new TopicPartition(kafkaTopicError, -1));

        return (consumerRecord, exception) -> {
            log.error("Retries exhausted for transactionId [{}]. Publishing to topic '{}'. Reason: {}",
                    consumerRecord.key(), kafkaTopicError, exception.getMessage());
            delegate.accept(consumerRecord, exception);
        };
    }

    @Bean
    public DefaultErrorHandler errorHandler(ConsumerRecordRecoverer recoverer) {
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
