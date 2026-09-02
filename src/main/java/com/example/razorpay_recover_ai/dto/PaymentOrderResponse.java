package com.example.razorpay_recover_ai.dto;


import com.example.razorpay_recover_ai.enums.PaymentOrderStatus;

import java.time.LocalDateTime;
public record PaymentOrderResponse(

        Long id,

        String merchantOrderId,

        String razorpayOrderId,

        String customerId,

        Long amount,

        String currency,

        String receipt,

        PaymentOrderStatus status,

        Integer attempts,

        LocalDateTime createdAt

) {
}