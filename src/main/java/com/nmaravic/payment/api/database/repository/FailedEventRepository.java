package com.nmaravic.payment.api.database.repository;

import com.nmaravic.payment.api.database.entitymodel.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedEventRepository extends JpaRepository<FailedEvent, UUID> {
}
