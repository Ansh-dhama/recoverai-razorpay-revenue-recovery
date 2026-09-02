package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.AiRecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;

import com.example.razorpay_recover_ai.serviceInterface.AiRecoveryAnalysisService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiRecoveryAnalysisServiceImpl
        implements AiRecoveryAnalysisService {

    private final ChatClient chatClient;


    public AiRecoveryAnalysisServiceImpl(
            ChatClient.Builder chatClientBuilder
    ) {

        this.chatClient =
                chatClientBuilder.build();
    }


    @Override
    public AiRecoveryDecision analyze(
            Payment payment,
            RecoveryCase recoveryCase
    ) {

        final int attempts =
                recoveryCase.getAttemptCount() == null
                        ? 0
                        : recoveryCase.getAttemptCount();


        return chatClient
                .prompt()

                .system("""
                        You are a payment recovery decision engine.

                        Your task is to determine the safest and most effective
                        recovery strategy for a failed payment.

                        You MUST choose exactly one action:

                        RETRY_NOW
                        RETRY_LATER
                        ALTERNATIVE_PAYMENT_METHOD
                        HUMAN_REVIEW
                        NO_ACTION

                        Rules:

                        - confidence must be between 0.0 and 1.0
                        - reason must be concise
                        - never invent payment information
                        - never claim a payment succeeded
                        - prefer HUMAN_REVIEW when information is ambiguous
                          or repeated recovery attempts have failed
                        - do not recommend unlimited retries
                        """)

                .user(user -> user

                        .text("""
                                Analyze this failed payment.

                                Payment method:
                                {paymentMethod}

                                Amount:
                                {amount}

                                Currency:
                                {currency}

                                Failure reason:
                                {failureReason}

                                Failure description:
                                {failureDescription}

                                Previous recovery attempts:
                                {attemptCount}
                                """)

                        .param(
                                "paymentMethod",
                                safe(
                                        payment.getPaymentMethod()
                                )
                        )

                        .param(
                                "amount",
                                payment.getAmount()
                        )

                        .param(
                                "currency",
                                safe(
                                        payment.getCurrency()
                                )
                        )

                        .param(
                                "failureReason",
                                safe(
                                        payment.getFailureReason()
                                )
                        )

                        .param(
                                "failureDescription",
                                safe(
                                        payment.getFailureDescription()
                                )
                        )

                        .param(
                                "attemptCount",
                                attempts
                        )
                )

                .call()

                .entity(
                        AiRecoveryDecision.class
                );
    }


    private String safe(
            Object value
    ) {

        if (value == null) {
            return "UNKNOWN";
        }

        return String.valueOf(value);
    }
}