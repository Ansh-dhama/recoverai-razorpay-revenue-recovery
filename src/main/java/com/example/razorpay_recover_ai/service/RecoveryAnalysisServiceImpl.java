package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.RecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import com.example.razorpay_recover_ai.exception.InvalidRecoveryStateException;
import com.example.razorpay_recover_ai.repository.RecoveryCaseRepository;

import com.example.razorpay_recover_ai.serviceInterface.HybridRecoveryDecisionService;
import com.example.razorpay_recover_ai.serviceInterface.RecoveryAnalysisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RecoveryAnalysisServiceImpl
        implements RecoveryAnalysisService {

    private final RecoveryCaseRepository
            recoveryCaseRepository;
private  final HybridRecoveryDecisionService hybridRecoveryDecisionService;

    public RecoveryAnalysisServiceImpl(

            RecoveryCaseRepository recoveryCaseRepository,

            HybridRecoveryDecisionService
                    hybridRecoveryDecisionService
    ) {

        this.recoveryCaseRepository =
                recoveryCaseRepository;

        this.hybridRecoveryDecisionService =
                hybridRecoveryDecisionService;
    }


    // =========================================================
    // ANALYZE ONE RECOVERY CASE
    // =========================================================

    @Override
    @Transactional
    public RecoveryCase analyzeCase(
            Long recoveryCaseId
    ) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository
                        .findById(
                                recoveryCaseId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "RecoveryCase not found: "
                                                        + recoveryCaseId
                                        )
                        );


        // -----------------------------------------------------
        // VALIDATE CURRENT STATE
        // -----------------------------------------------------

        if (recoveryCase.getStatus()
                != RecoveryStatus.PENDING_ANALYSIS) {

            throw new InvalidRecoveryStateException(
                    "RecoveryCase cannot be analyzed from status: "
                            + recoveryCase.getStatus()
            );
        }


        // -----------------------------------------------------
        // PENDING_ANALYSIS → ANALYZING
        // -----------------------------------------------------

        recoveryCase.setStatus(
                RecoveryStatus.ANALYZING
        );


        recoveryCaseRepository.save(
                recoveryCase
        );


        // -----------------------------------------------------
        // GET FAILED PAYMENT
        // -----------------------------------------------------

        Payment payment =
                recoveryCase.getPayment();


        if (payment == null) {

            throw new IllegalStateException(
                    "RecoveryCase has no Payment"
            );
        }


        // -----------------------------------------------------
        // RUN DECISION ENGINE
        // -----------------------------------------------------

        RecoveryDecision decision =
                hybridRecoveryDecisionService
                        .decide(
                                payment,
                                recoveryCase
                        );

        // -----------------------------------------------------
        // STORE DECISION
        // -----------------------------------------------------

        recoveryCase.setRecommendedAction(
                decision.action()
        );


        recoveryCase.setConfidence(
                decision.confidence()
        );


        recoveryCase.setReason(
                decision.reason()
        );


        // -----------------------------------------------------
        // ANALYZING → RECOVERY_PLANNED
        // -----------------------------------------------------

        recoveryCase.setStatus(
                RecoveryStatus.RECOVERY_PLANNED
        );


        return recoveryCaseRepository.save(
                recoveryCase
        );
    }


    // =========================================================
    // ANALYZE ALL PENDING CASES
    // =========================================================

    @Override
    @Transactional
    public List<RecoveryCase> analyzePendingCases() {

        List<RecoveryCase> pendingCases =
                recoveryCaseRepository
                        .findByStatus(
                                RecoveryStatus.PENDING_ANALYSIS
                        );


        List<RecoveryCase> analyzedCases =
                new ArrayList<>();


        for (RecoveryCase recoveryCase
                : pendingCases) {

            RecoveryCase analyzed =
                    analyzeCase(
                            recoveryCase.getId()
                    );

            analyzedCases.add(
                    analyzed
            );
        }


        return analyzedCases;
    }


    // =========================================================
    // RECOVERY DECISION ENGINE
    // =========================================================

    private RecoveryDecision decideRecoveryAction(
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
        // RULE 1 — TOO MANY RECOVERY ATTEMPTS
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

                    "The payment failed because of insufficient funds. Retrying the same payment source is unlikely to succeed."
            );
        }


        // =====================================================
        // RULE 3 — TEMPORARY TECHNICAL FAILURE
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
        // RULE 4 — NETWORK / TIMEOUT
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
        // RULE 5 — CARD DECLINE
        // =====================================================

        if (paymentMethod == PaymentMethod.CARD
                &&
                containsAny(
                        failureText,

                        "card_declined",
                        "card declined",
                        "bank declined",
                        "declined by bank",
                        "issuer declined"
                )) {

            return new RecoveryDecision(

                    RecoveryAction.ALTERNATIVE_PAYMENT_METHOD,

                    0.89,

                    "The card or issuing bank declined the transaction. Another payment method is recommended."
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
                "3ds"
        )) {

            return new RecoveryDecision(

                    RecoveryAction.RETRY_NOW,

                    0.82,

                    "Payment authentication failed. The customer can retry the payment and complete authentication again."
            );
        }


        // =====================================================
        // RULE 7 — UPI FAILURE
        // =====================================================

        if (paymentMethod == PaymentMethod.UPI) {

            return new RecoveryDecision(

                    RecoveryAction.RETRY_NOW,

                    0.78,

                    "The failed payment used UPI and no permanent failure was identified. Retrying the payment is recommended."
            );
        }


        // =====================================================
        // DEFAULT RULE
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