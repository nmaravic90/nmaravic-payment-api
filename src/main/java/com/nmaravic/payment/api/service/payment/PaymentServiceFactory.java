package com.nmaravic.payment.api.service.payment;

import com.nmaravic.payment.api.model.PaymentType;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceFactory {

    private final TransferService transferService;
    private final BillPaymentService billPaymentService;
    private final ParkingPaymentService parkingPaymentService;
    private final QrCodePaymentService qrCodePaymentService;

    public PaymentServiceFactory(TransferService transferService,
                                 BillPaymentService billPaymentService,
                                 ParkingPaymentService parkingPaymentService,
                                 QrCodePaymentService qrCodePaymentService) {
        this.transferService = transferService;
        this.billPaymentService = billPaymentService;
        this.parkingPaymentService = parkingPaymentService;
        this.qrCodePaymentService = qrCodePaymentService;
    }

    @SuppressWarnings("unchecked")
    public <T> PaymentService<T> getService(PaymentType type) {
        return (PaymentService<T>) switch (type) {
            case TRANSFER -> transferService;
            case BILL     -> billPaymentService;
            case PARKING  -> parkingPaymentService;
            case QR_CODE  -> qrCodePaymentService;
        };
    }
}
