package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.dto.HybridRecoveryDecisionResult;
import com.example.razorpay_recover_ai.dto.RecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;

public interface HybridRecoveryDecisionService {

    RecoveryDecision decide(
            Payment payment,
            RecoveryCase recoveryCase
    );

    HybridRecoveryDecisionResult decideWithSource(
            Payment payment,
            RecoveryCase recoveryCase
    );
}