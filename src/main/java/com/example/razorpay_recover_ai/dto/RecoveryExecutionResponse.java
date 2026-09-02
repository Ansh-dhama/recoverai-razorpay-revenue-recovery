package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;

public record RecoveryExecutionResponse(

        Long recoveryCaseId,

        Long paymentId,

        RecoveryAction action,

        RecoveryStatus status,

        Integer attemptCount,

        boolean customerActionRequired,

        String message

) {
}