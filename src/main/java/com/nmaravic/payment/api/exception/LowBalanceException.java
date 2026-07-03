package com.nmaravic.payment.api.exception;


public class LowBalanceException extends RuntimeException {

    public LowBalanceException(String userId) {
        super("Payment declined. Not enough balance for user" + userId);
    }
}
