package com.example.razorpay_recover_ai.exception;

public class InvalidPaymentException
        extends RuntimeException {

    public InvalidPaymentException(String message) {
        super(message);
    }
}