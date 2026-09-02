package com.example.razorpay_recover_ai.exception;

public class PaymentGatewayException
        extends RuntimeException {

    public PaymentGatewayException(
            String message
    ) {

        super(message);
    }
}