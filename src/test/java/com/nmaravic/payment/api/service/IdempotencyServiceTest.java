package com.nmaravic.payment.api.service;

import com.nmaravic.payment.api.database.entitymodel.IdempotencyRecord;
import com.nmaravic.payment.api.database.repository.IdempotencyRecordRepository;
import com.nmaravic.payment.api.mapper.IdempotencyRecordMapper;
import com.nmaravic.payment.api.model.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private IdempotencyRecordMapper idempotencyRecordMapper;

    @Test
    void findCachedResponse_whenRecordExists_shouldReturnResponse() {
        UUID key = UUID.randomUUID();
        IdempotencyRecord idempotencyRecord = new IdempotencyRecord();
        TransactionResponse expected = new TransactionResponse().transactionId("txn_123");

        when(idempotencyRecordRepository.findById(key)).thenReturn(Optional.of(idempotencyRecord));
        when(idempotencyRecordMapper.toResponse(idempotencyRecord)).thenReturn(expected);

        Optional<TransactionResponse> result = idempotencyService.findCachedResponse(key);

        assertThat(result).isPresent();
        TransactionResponse actual = result.orElseThrow();
        assertThat(actual.getTransactionId()).isEqualTo("txn_123");
    }

    @Test
    void findCachedResponse_whenRecordMissing_shouldReturnEmpty() {
        UUID key = UUID.randomUUID();
        when(idempotencyRecordRepository.findById(key)).thenReturn(Optional.empty());

        Optional<TransactionResponse> result = idempotencyService.findCachedResponse(key);

        verify(idempotencyRecordMapper, never()).toResponse(any());

        assertThat(result).isEmpty();
    }

    @Test
    void saveResponse_shouldMapAndPersist() {
        UUID key = UUID.randomUUID();
        TransactionResponse response = new TransactionResponse().transactionId("txn_123");
        IdempotencyRecord idempotencyRecord = new IdempotencyRecord();

        when(idempotencyRecordMapper.toEntity(key, response)).thenReturn(idempotencyRecord);

        idempotencyService.saveResponse(key, response);
        verify(idempotencyRecordMapper).toEntity(key, response);
        verify(idempotencyRecordRepository).save(idempotencyRecord);
    }

    @Test
    void cleanExpiredKeys_shouldDeleteOldRecords() {
        LocalDateTime before = LocalDateTime.now().minusHours(24);

        idempotencyService.cleanExpiredKeys();
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(idempotencyRecordRepository).deleteByCreatedAtBefore(captor.capture());

        LocalDateTime cutoff = captor.getValue();
        LocalDateTime after = LocalDateTime.now().minusHours(24);
        assertThat(cutoff).isBetween(before.minusSeconds(5), after.plusSeconds(5));
    }
}