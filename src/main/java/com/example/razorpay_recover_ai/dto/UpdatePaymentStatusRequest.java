package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(

        @NotNull(message = "status is required")
        PaymentStatus status,

        String failureReason,

        String failureDescription

) {
}