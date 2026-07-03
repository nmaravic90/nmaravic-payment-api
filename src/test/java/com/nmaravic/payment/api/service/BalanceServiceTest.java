package com.nmaravic.payment.api.service;

import com.nmaravic.payment.api.database.entitymodel.User;
import com.nmaravic.payment.api.database.repository.UserRepository;
import com.nmaravic.payment.api.exception.LowBalanceException;
import com.nmaravic.payment.api.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private UserRepository userRepository;

    @Test
    void validateAndSubtractBalance_withSufficientFunds_shouldSubtract() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, new BigDecimal("1000.00"));
        when(userRepository.findByIdWithLock(userId)).thenReturn(Optional.of(user));

        balanceService.validateAndSubtractBalance(userId.toString(), new BigDecimal("300.00"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    void validateAndSubtractBalance_whenUserMissing_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdWithLock(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> balanceService.validateAndSubtractBalance(userId.toString(), new BigDecimal("100.00")))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void validateAndSubtractBalance_withInsufficientFunds_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, new BigDecimal("50.00"));
        when(userRepository.findByIdWithLock(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> balanceService.validateAndSubtractBalance(userId.toString(), new BigDecimal("100.00")))
                .isInstanceOf(LowBalanceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void validateAndSubtractBalance_withExactBalance_shouldSubtractToZero() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, new BigDecimal("100.00"));
        when(userRepository.findByIdWithLock(userId)).thenReturn(Optional.of(user));

        balanceService.validateAndSubtractBalance(userId.toString(), new BigDecimal("100.00"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private User buildUser(UUID id, BigDecimal balance) {
        User user = new User();
        user.setId(id);
        user.setBalance(balance);
        return user;
    }
}