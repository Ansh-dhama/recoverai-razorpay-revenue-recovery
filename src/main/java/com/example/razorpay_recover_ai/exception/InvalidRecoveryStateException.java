package com.example.razorpay_recover_ai.exception;

public class InvalidRecoveryStateException
        extends RuntimeException {

    public InvalidRecoveryStateException(
            String message
    ) {
        super(message);
    }
}