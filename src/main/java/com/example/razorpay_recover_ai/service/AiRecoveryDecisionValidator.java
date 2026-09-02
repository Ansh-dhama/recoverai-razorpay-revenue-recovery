package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.AiRecoveryDecision;

import org.springframework.stereotype.Component;

@Component
public class AiRecoveryDecisionValidator {


    public void validate(
            AiRecoveryDecision decision
    ) {

        if (decision == null) {

            throw new IllegalArgumentException(
                    "AI recovery decision is null"
            );
        }


        if (decision.action() == null) {

            throw new IllegalArgumentException(
                    "AI recovery action is missing"
            );
        }


        if (decision.confidence() == null
                ||
                decision.confidence() < 0
                ||
                decision.confidence() > 1) {

            throw new IllegalArgumentException(
                    "AI confidence must be between 0 and 1"
            );
        }


        if (decision.reason() == null
                ||
                decision.reason().isBlank()) {

            throw new IllegalArgumentException(
                    "AI recovery reason is missing"
            );
        }
    }
}