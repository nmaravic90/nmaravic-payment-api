package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.model.PaymentType;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceFactory {

    private final TransferPaymentService transferPaymentService;
    private final BillPaymentService billPaymentService;
    private final ParkingPaymentService parkingPaymentService;
    private final QrCodePaymentService qrCodePaymentService;

    public PaymentServiceFactory(TransferPaymentService transferPaymentService,
                                 BillPaymentService billPaymentService,
                                 ParkingPaymentService parkingPaymentService,
                                 QrCodePaymentService qrCodePaymentService) {
        this.transferPaymentService = transferPaymentService;
        this.billPaymentService = billPaymentService;
        this.parkingPaymentService = parkingPaymentService;
        this.qrCodePaymentService = qrCodePaymentService;
    }

    @SuppressWarnings("unchecked")
    public <T> PaymentService<T> getService(PaymentType type) {
        return (PaymentService<T>) switch (type) {
            case TRANSFER -> transferPaymentService;
            case BILL     -> billPaymentService;
            case PARKING  -> parkingPaymentService;
            case QR_CODE  -> qrCodePaymentService;
        };
    }
}
