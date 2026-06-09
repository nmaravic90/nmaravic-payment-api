package com.nmaravic.payment.api.service;

import com.nmaravic.payment.api.database.entitymodel.User;
import com.nmaravic.payment.api.database.repository.UserRepository;
import com.nmaravic.payment.api.exception.LowBalanceException;
import com.nmaravic.payment.api.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceService {

    private final UserRepository userRepository;

    public BalanceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateAndSubtractBalance(String userId, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));
        validateBalance(user, amount);
        subtractBalance(user, amount);
    }

    private void validateBalance(User user, BigDecimal amount) {
        if (user.getBalance().compareTo(amount) < 0) {
            throw new LowBalanceException(user.getId().toString());
        }
    }

    private void subtractBalance(User user, BigDecimal amount) {
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
    }
}
