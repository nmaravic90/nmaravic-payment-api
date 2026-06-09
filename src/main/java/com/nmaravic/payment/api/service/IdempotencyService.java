package com.nmaravic.payment.api.service;

import com.nmaravic.payment.api.database.entitymodel.IdempotencyRecord;
import com.nmaravic.payment.api.database.repository.IdempotencyRecordRepository;
import com.nmaravic.payment.api.mapper.IdempotencyRecordMapper;
import com.nmaravic.payment.api.model.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository  idempotencyRecordRepository;
    private final IdempotencyRecordMapper idempotencyRecordMapper;

    public Optional<TransactionResponse> findCachedResponse(UUID idempotencyKey) {
        return idempotencyRecordRepository.findById(idempotencyKey)
                .map(idempotencyRecordMapper::toResponse);
    }

    public void saveResponse(UUID idempotencyKey, TransactionResponse response) {
        IdempotencyRecord idempotencyRecord = idempotencyRecordMapper.toEntity(idempotencyKey, response);
        idempotencyRecordRepository.save(idempotencyRecord);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredKeys() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        idempotencyRecordRepository.deleteByCreatedAtBefore(cutoff);
    }
}
