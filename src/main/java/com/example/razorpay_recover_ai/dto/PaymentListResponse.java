package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.PaymentStatus;

public record PaymentListResponse(

        Long id,

        String razorpayPaymentId,

        String orderId,

        String customerId,

        Long amount,

        String currency,

        PaymentMethod paymentMethod,

        PaymentStatus status,

        String failureReason,

        String failureDescription

) {
}