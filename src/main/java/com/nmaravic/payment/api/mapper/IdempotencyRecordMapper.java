package com.nmaravic.payment.api.mapper;

import com.nmaravic.payment.api.database.entitymodel.IdempotencyRecord;
import com.nmaravic.payment.api.model.TransactionResponse;
import com.nmaravic.payment.api.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class IdempotencyRecordMapper {

    public IdempotencyRecord toEntity(UUID idempotencyKey, TransactionResponse response) {
        return IdempotencyRecord.builder()
                .id(idempotencyKey)
                .responseBody(JsonUtil.serialize(response))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public TransactionResponse toResponse(IdempotencyRecord idempotencyRecord) {
        return JsonUtil.deserialize(idempotencyRecord.getResponseBody(), TransactionResponse.class);
    }
}
