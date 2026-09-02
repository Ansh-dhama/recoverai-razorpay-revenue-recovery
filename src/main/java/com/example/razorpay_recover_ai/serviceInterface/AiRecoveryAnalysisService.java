package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.dto.AiRecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;

public interface AiRecoveryAnalysisService {

    AiRecoveryDecision analyze(
            Payment payment,
            RecoveryCase recoveryCase
    );
}