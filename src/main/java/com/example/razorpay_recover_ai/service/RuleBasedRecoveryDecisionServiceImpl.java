package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.RecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.serviceInterface.RuleBasedRecoveryDecisionService;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RuleBasedRecoveryDecisionServiceImpl
        implements RuleBasedRecoveryDecisionService {


    @Override
    public RecoveryDecision decide(
            Payment payment,
            RecoveryCase recoveryCase
    ) {

        String failureReason =
                normalize(
                        payment.getFailureReason()
                );

        String failureDescription =
                normalize(
                        payment.getFailureDescription()
                );

        String failureText =
                failureReason
                        + " "
                        + failureDescription;


        PaymentMethod paymentMethod =
                payment.getPaymentMethod();


        int attempts =
                recoveryCase.getAttemptCount() == null
                        ? 0
                        : recoveryCase.getAttemptCount();


        // =====================================================
        // RULE 1 — TOO MANY ATTEMPTS
        // =====================================================

        if (attempts >= 3) {

            return new RecoveryDecision(
                    RecoveryAction.HUMAN_REVIEW,
                    0.95,
                    "Multiple recovery attempts have already failed. Manual review is recommended."
            );
        }


        // =====================================================
        // RULE 2 — INSUFFICIENT FUNDS
        // =====================================================

        if (containsAny(
                failureText,
                "insufficient_funds",
                "insufficient funds",
                "low balance",
                "balance insufficient"
        )) {

            return new RecoveryDecision(
                    RecoveryAction.ALTERNATIVE_PAYMENT_METHOD,
                    0.94,
                    "The payment failed because of insufficient funds. Another payment method is recommended."
            );
        }


        // =====================================================
        // RULE 3 — BANK / ISSUER DECLINE
        // Works for CARD, NETBANKING and similar bank-decline cases
        // =====================================================

        if (containsAny(
                failureText,
                "card_declined",
                "card declined",
                "bank declined",
                "declined by bank",
                "declined by the bank",
                "issuer declined",
                "payment declined",
                "transaction declined",
                "declined"
        )) {

            return new RecoveryDecision(
                    RecoveryAction.ALTERNATIVE_PAYMENT_METHOD,
                    0.90,
                    "The bank or payment provider declined the transaction. Another payment method is recommended."
            );
        }


        // =====================================================
        // RULE 4 — TEMPORARY TECHNICAL ISSUE
        // =====================================================

        if (containsAny(
                failureText,
                "temporary",
                "technical",
                "gateway",
                "server error",
                "processing error",
                "service unavailable"
        )) {

            return new RecoveryDecision(
                    RecoveryAction.RETRY_NOW,
                    0.90,
                    "The failure appears temporary, so an immediate retry has a high probability of succeeding."
            );
        }


        // =====================================================
        // RULE 5 — NETWORK / TIMEOUT
        // =====================================================

        if (containsAny(
                failureText,
                "timeout",
                "timed out",
                "network",
                "connection"
        )) {

            return new RecoveryDecision(
                    RecoveryAction.RETRY_LATER,
                    0.86,
                    "The payment appears to have failed because of a temporary network or timeout issue."
            );
        }


        // =====================================================
        // RULE 6 — AUTHENTICATION FAILURE
        // =====================================================

        if (containsAny(
                failureText,
                "authentication",
                "otp",
                "3d secure",
                "3ds",
                "authorization failed"
        )) {

            return new RecoveryDecision(
                    RecoveryAction.RETRY_NOW,
                    0.82,
                    "Payment authentication failed. The customer can retry and complete authentication again."
            );
        }


        // =====================================================
        // RULE 7 — UPI FALLBACK
        // =====================================================

        if (paymentMethod == PaymentMethod.UPI) {

            return new RecoveryDecision(
                    RecoveryAction.RETRY_NOW,
                    0.78,
                    "The payment used UPI and no permanent failure was identified. Retrying is recommended."
            );
        }


        // =====================================================
        // DEFAULT
        // =====================================================

        return new RecoveryDecision(
                RecoveryAction.RETRY_LATER,
                0.65,
                "The failure reason does not match a high-confidence recovery rule. A delayed retry is recommended."
        );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private boolean containsAny(
            String value,
            String... keywords
    ) {

        for (String keyword : keywords) {

            if (value.contains(
                    keyword.toLowerCase(
                            Locale.ROOT
                    )
            )) {

                return true;
            }
        }

        return false;
    }
}