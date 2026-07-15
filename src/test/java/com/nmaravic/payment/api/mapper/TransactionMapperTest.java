package com.nmaravic.payment.api.mapper;

import com.nmaravic.payment.api.database.entitymodel.Transaction;
import com.nmaravic.payment.api.kafka.PaymentEvent;
import com.nmaravic.payment.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();
    }

    @Test
    void toEntity_billPayment_shouldMapCorrectly() {
        BillPaymentRequest request = new BillPaymentRequest()
                .userId("user_001")
                .billCode("123456789")
                .amount(3500.00)
                .currency("RSD");

        Transaction transaction = mapper.toEntity(request);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getType()).isEqualTo(PaymentType.BILL);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3500.00));
        assertThat(transaction.getCurrency()).isEqualTo("RSD");
        assertThat(transaction.getUserId()).isEqualTo("user_001");
        assertThat(transaction.getBillCode()).isEqualTo("123456789");
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    void toEntity_transfer_shouldMapCorrectly() {
        TransferRequest request = new TransferRequest()
                .senderId("user_001")
                .receiverId("user_002")
                .amount(150.00)
                .currency("RSD");

        Transaction transaction = mapper.toEntity(request);

        assertThat(transaction.getType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(transaction.getUserId()).isEqualTo("user_001");      // sender kao userId
        assertThat(transaction.getSenderId()).isEqualTo("user_001");
        assertThat(transaction.getReceiverId()).isEqualTo("user_002");
    }

    @Test
    @DisplayName("ParkingPaymentRequest should map to PARKING and set validUntil in the future")
    void toEntity_parking_shouldMapCorrectly() {
        ParkingPaymentRequest request = new ParkingPaymentRequest()
                .userId("user_001")
                .licensePlate("BG123AB")
                .zone("1")
                .durationMinutes(60)
                .currency("RSD");

        Transaction transaction = mapper.toEntity(request);

        assertThat(transaction.getType()).isEqualTo(PaymentType.PARKING);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getLicensePlate()).isEqualTo("BG123AB");
        assertThat(transaction.getZone()).isEqualTo("1");
        assertThat(transaction.getValidUntil()).isAfter(LocalDateTime.now());
        assertThat(transaction.getAmount()).isNotNull();
    }

    @Test
    void toEntity_qrCode_shouldMapCorrectly() {
        QrCodePaymentRequest request = new QrCodePaymentRequest()
                .userId("user_001")
                .qrPayload("00020101021226370016RS.NBS.IPS...")
                .amount(150.00)
                .currency("RSD");

        Transaction transaction = mapper.toEntity(request);
        assertThat(transaction.getType()).isEqualTo(PaymentType.QR_CODE);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(transaction.getUserId()).isEqualTo("user_001");
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        Transaction transaction = buildTransaction();

        TransactionResponse response = mapper.toResponse(transaction);

        assertThat(response.getTransactionId()).isEqualTo(transaction.getId().toString());
        assertThat(response.getStatus()).isEqualTo(transaction.getStatus());
        assertThat(response.getAmount()).isEqualTo(transaction.getAmount().doubleValue());
        assertThat(response.getCurrency()).isEqualTo("RSD");
        assertThat(response.getTimestamp()).isEqualTo(transaction.getCreatedAt());
    }

    @Test
    @DisplayName("toDetailResponse should include type, sender and receiver")
    void toDetailResponse_shouldMapCorrectly() {
        Transaction transaction = buildTransaction();

        TransactionDetailResponse response = mapper.toDetailResponse(transaction);

        assertThat(response.getTransactionId()).isEqualTo(transaction.getId().toString());
        assertThat(response.getType()).isEqualTo(transaction.getType());
        assertThat(response.getStatus()).isEqualTo(transaction.getStatus());
        assertThat(response.getSenderId()).isEqualTo(transaction.getSenderId());
        assertThat(response.getReceiverId()).isEqualTo(transaction.getReceiverId());
    }

    @Test
    void toEvent_shouldMapCorrectly() {
        Transaction transaction = buildTransaction();

        PaymentEvent event = mapper.toEvent(transaction, PaymentType.TRANSFER);

        assertThat(event.getTransactionId()).isEqualTo(transaction.getId());
        assertThat(event.getPaymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(event.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(event.getAmount()).isEqualByComparingTo(transaction.getAmount());
        assertThat(event.getCurrency()).isEqualTo(transaction.getCurrency());
        assertThat(event.getUserId()).isEqualTo(transaction.getUserId());
        assertThat(event.getCreatedAt()).isEqualTo(transaction.getCreatedAt());
    }

    private Transaction buildTransaction() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .type(PaymentType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("150.00"))
                .currency("RSD")
                .userId("user_001")
                .senderId("user_001")
                .receiverId("user_002")
                .createdAt(LocalDateTime.now())
                .build();
    }
}