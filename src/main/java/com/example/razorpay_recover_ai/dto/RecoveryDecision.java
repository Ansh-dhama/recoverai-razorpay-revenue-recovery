package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.RecoveryAction;

public record RecoveryDecision(

        RecoveryAction action,

        double confidence,

        String reason

) {
}