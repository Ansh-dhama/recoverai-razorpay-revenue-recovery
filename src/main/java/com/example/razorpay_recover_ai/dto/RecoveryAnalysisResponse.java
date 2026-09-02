package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;

public record RecoveryAnalysisResponse(

        Long recoveryCaseId,
        RecoveryStatus status,
        RecoveryAction recommendedAction,
        Double confidence,
        String reason,
        Integer attemptCount

) {
}