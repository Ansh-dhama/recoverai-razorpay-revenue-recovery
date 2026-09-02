package com.example.razorpay_recover_ai.dto;


import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;

import java.time.LocalDateTime;

public record RecoveryCaseResponse(

        Long id,

        Long paymentId,

        RecoveryStatus status,

        RecoveryAction recommendedAction,

        Double confidence,

        String reason,

        Integer attemptCount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}