package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(

        String razorpayPaymentId,

        @NotBlank(message = "orderId is required")
        String orderId,

        @NotBlank(message = "customerId is required")
        String customerId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        Long amount,

        @NotBlank(message = "currency is required")
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "currency must be a 3-letter code"
        )
        String currency,

        @NotNull(message = "paymentMethod is required")
        PaymentMethod paymentMethod,

        @NotNull(message = "status is required")
        PaymentStatus status,

        String failureReason,

        String failureDescription

) {
}