package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.RecoveryAction;

public record AiRecoveryDecision(

        RecoveryAction action,

        Double confidence,

        String reason

) {
}