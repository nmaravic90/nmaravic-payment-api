package com.nmaravic.payment.api.service.transaction;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.database.repository.TransactionRepository;
import com.nmaravic.payment.api.exception.TransactionNotFoundException;
import com.nmaravic.payment.api.mapper.TransactionMapper;
import com.nmaravic.payment.api.model.PaymentType;
import com.nmaravic.payment.api.model.TransactionDetailResponse;
import com.nmaravic.payment.api.model.TransactionHistoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Test
    void getTransaction_whenExists_shouldReturnResponse() {
        UUID id = UUID.randomUUID();
        Transaction transaction = new Transaction();
        TransactionDetailResponse expected = new TransactionDetailResponse().transactionId(id.toString());

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDetailResponse(transaction)).thenReturn(expected);

        TransactionDetailResponse result = transactionService.getTransaction(id.toString());

        assertThat(result.getTransactionId()).isEqualTo(id.toString());
    }

    @Test
    void getTransaction_whenMissing_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        String transactionId = id.toString();

        assertThatThrownBy(() -> transactionService.getTransaction(transactionId)).isInstanceOf(TransactionNotFoundException.class);

        verify(transactionMapper, never()).toDetailResponse(any());
    }

    @Test
    void getTransactionHistory_withoutType_shouldQueryByUserId() {
        String userId = "user_001";
        Transaction transaction = new Transaction();
        Page<Transaction> page = new PageImpl<>(List.of(transaction), PageRequest.of(0, 10), 1);

        when(transactionRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toDetailResponse(transaction)).thenReturn(new TransactionDetailResponse().transactionId("txn_1"));

        TransactionHistoryResponse response = transactionService.getTransactionHistory(userId, 0, 10, null);

        verify(transactionRepository).findByUserId(eq(userId), any(Pageable.class));
        verify(transactionRepository, never()).findByUserIdAndType(any(), any(), any());

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isZero();
        assertThat(response.getPageSize()).isEqualTo(10);
    }

    @Test
    void getTransactionHistory_withType_shouldQueryByUserIdAndType() {
        String userId = "user_001";
        PaymentType type = PaymentType.TRANSFER;
        Transaction transaction = new Transaction();
        Page<Transaction> page = new PageImpl<>(List.of(transaction), PageRequest.of(0, 10), 1);

        when(transactionRepository.findByUserIdAndType(eq(userId), eq(type), any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toDetailResponse(transaction)).thenReturn(new TransactionDetailResponse().transactionId("txn_1"));

        TransactionHistoryResponse response = transactionService.getTransactionHistory(userId, 0, 10, type);
        verify(transactionRepository).findByUserIdAndType(eq(userId), eq(type), any(Pageable.class));
        verify(transactionRepository, never()).findByUserId(any(), any());

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void getTransactionHistory_shouldMapPaginationMetadata() {
        String userId = "user_001";
        List<Transaction> items = List.of(new Transaction(), new Transaction());
        Page<Transaction> page = new PageImpl<>(items, PageRequest.of(1, 10), 25);

        when(transactionRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);
        when(transactionMapper.toDetailResponse(any())).thenReturn(new TransactionDetailResponse());

        TransactionHistoryResponse response = transactionService.getTransactionHistory(userId, 1, 10, null);

        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(10);
    }

    @Test
    void getTransactionHistory_whenEmpty_shouldReturnEmptyContent() {
        String userId = "user_001";
        Page<Transaction> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(transactionRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

        TransactionHistoryResponse response = transactionService.getTransactionHistory(userId, 0, 10, null);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
        verify(transactionMapper, never()).toDetailResponse(any());
    }
}