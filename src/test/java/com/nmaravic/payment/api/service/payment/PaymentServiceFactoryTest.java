package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.model.PaymentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentServiceFactoryTest {

    @InjectMocks
    private PaymentServiceFactory paymentServiceFactory;

    @Mock
    private TransferPaymentService transferPaymentService;

    @Mock
    private BillPaymentService billPaymentService;

    @Mock
    private ParkingPaymentService parkingPaymentService;

    @Mock
    private QrCodePaymentService qrCodePaymentService;


    @Test
    void getService_forTransfer_shouldReturnTransferService() {
        assertThat(paymentServiceFactory.getService(PaymentType.TRANSFER)).isSameAs(transferPaymentService);
    }

    @Test
    void getService_forBill_shouldReturnBillService() {
        assertThat(paymentServiceFactory.getService(PaymentType.BILL)).isSameAs(billPaymentService);
    }

    @Test
    void getService_forParking_shouldReturnParkingService() {
        assertThat(paymentServiceFactory.getService(PaymentType.PARKING)).isSameAs(parkingPaymentService);
    }

    @Test
    void getService_forQrCode_shouldReturnQrCodeService() {
        assertThat(paymentServiceFactory.getService(PaymentType.QR_CODE)).isSameAs(qrCodePaymentService);
    }
}