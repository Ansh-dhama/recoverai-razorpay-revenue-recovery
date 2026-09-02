package com.example.razorpay_recover_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreatePaymentOrderRequest(

        @NotBlank
        String merchantOrderId,

        @NotBlank
        String customerId,

        @NotNull
        @Positive
        Long amount,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String currency

) {
}