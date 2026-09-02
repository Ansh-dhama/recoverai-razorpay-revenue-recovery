package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.AnalysisSource;
import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.RecoveryAction;

public record SimulationCaseResult(

        int caseNumber,

        long amount,

        PaymentMethod paymentMethod,

        String failureReason,

        int previousAttempts,

        RecoveryAction recommendedAction,

        double confidence,

        AnalysisSource analysisSource,

        boolean recovered,

        boolean retryStopped

) {
}