package com.nmaravic.payment.api.database.repository;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserId(String userId);

    Page<Transaction> findByUserId(String userId, Pageable pageable);

    Page<Transaction> findByUserIdAndType(String userId, PaymentType type, Pageable pageable);
}
