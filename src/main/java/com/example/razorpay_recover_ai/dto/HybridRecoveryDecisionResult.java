package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.AnalysisSource;

public record HybridRecoveryDecisionResult(

        RecoveryDecision decision,

        AnalysisSource source

) {
}