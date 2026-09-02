package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.dto.RecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;

public interface RuleBasedRecoveryDecisionService {

    RecoveryDecision decide(
            Payment payment,
            RecoveryCase recoveryCase
    );
}