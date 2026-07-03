package com.nmaravic.payment.api.mapper;

import com.nmaravic.payment.api.database.entitymodel.IdempotencyRecord;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class IdempotencyRecordMapperTest {

    private IdempotencyRecordMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IdempotencyRecordMapper();
    }

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        UUID idempotencyKey = UUID.randomUUID();
        TransactionResponse response = buildResponse();

        IdempotencyRecord entity = mapper.toEntity(idempotencyKey, response);

        assertThat(entity.getId()).isEqualTo(idempotencyKey);
        assertThat(entity.getResponseBody()).isNotBlank();
        assertThat(entity.getResponseBody()).contains(response.getTransactionId());
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void toResponse_shouldDeserializeCorrectly() {
        UUID idempotencyKey = UUID.randomUUID();
        TransactionResponse original = buildResponse();
        IdempotencyRecord entity = mapper.toEntity(idempotencyKey, original);

        TransactionResponse result = mapper.toResponse(entity);

        assertThat(result.getTransactionId()).isEqualTo(original.getTransactionId());
        assertThat(result.getAmount()).isEqualByComparingTo(original.getAmount());
        assertThat(result.getCurrency()).isEqualTo(original.getCurrency());
        assertThat(result.getStatus()).isEqualTo(original.getStatus());
    }

    @Test
    void serializeAndDeserialize_shouldReturnIdenticalObject(){
        UUID idempotencyKey = UUID.randomUUID();
        TransactionResponse original = buildResponse();
        IdempotencyRecord entity = mapper.toEntity(idempotencyKey, original);
        TransactionResponse result = mapper.toResponse(entity);

        assertThat(result).usingRecursiveComparison().isEqualTo(original);
    }

    private TransactionResponse buildResponse() {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(UUID.randomUUID().toString());
        response.setStatus(TransactionStatus.SUCCESS);
        response.setAmount(Double.valueOf("150.00"));
        response.setCurrency("RSD");
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}