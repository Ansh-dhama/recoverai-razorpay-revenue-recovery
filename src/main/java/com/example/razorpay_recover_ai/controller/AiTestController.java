package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.AiRecoveryDecision;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiTestController {

    private final ChatClient chatClient;

    public AiTestController(
            ChatClient.Builder chatClientBuilder
    ) {
        this.chatClient =
                chatClientBuilder.build();
    }


    @GetMapping("/test")
    public ResponseEntity<String> testAi() {

        String response =
                chatClient
                        .prompt()
                        .user("""
                                Reply with exactly:
                                RecoverAI AI connection successful
                                """)
                        .call()
                        .content();

        return ResponseEntity.ok(
                response
        );
    }
    @GetMapping("/test-decision")
    public ResponseEntity<AiRecoveryDecision> testDecision() {

        AiRecoveryDecision decision =
                chatClient
                        .prompt()

                        .system("""
                            You are a payment recovery decision engine.

                            Choose exactly ONE action:

                            RETRY_NOW
                            RETRY_LATER
                            ALTERNATIVE_PAYMENT_METHOD
                            HUMAN_REVIEW
                            NO_ACTION

                            Rules:
                            - confidence must be between 0.0 and 1.0
                            - reason must be short and clear
                            - do not invent information
                            """)

                        .user("""
                            Analyze this failed payment:

                            Payment method: UPI
                            Amount: 49900 paise
                            Currency: INR
                            Failure reason: payment_timeout
                            Failure description: Bank response timed out
                            Previous recovery attempts: 0
                            """)

                        .call()

                        .entity(AiRecoveryDecision.class);

        return ResponseEntity.ok(decision);
    }
}