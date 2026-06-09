package com.nmaravic.payment.api.database.repository;

import com.nmaravic.payment.api.database.entitymodel.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public  interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
