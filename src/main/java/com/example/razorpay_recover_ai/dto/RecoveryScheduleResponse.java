package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;

import java.time.LocalDateTime;

public record RecoveryScheduleResponse(

        Long recoveryCaseId,

        RecoveryStatus status,

        RecoveryAction recommendedAction,

        Integer attemptCount,

        LocalDateTime nextRetryAt,

        LocalDateTime lastAttemptAt

) {
}