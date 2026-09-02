package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.service.RazorpayWebhookProcessor;
import com.example.razorpay_recover_ai.service.RazorpayWebhookService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class RazorpayWebhookController {

    private final RazorpayWebhookService
            razorpayWebhookService;

    private final RazorpayWebhookProcessor
            razorpayWebhookProcessor;


    public RazorpayWebhookController(
            RazorpayWebhookService razorpayWebhookService,
            RazorpayWebhookProcessor razorpayWebhookProcessor
    ) {

        this.razorpayWebhookService =
                razorpayWebhookService;

        this.razorpayWebhookProcessor =
                razorpayWebhookProcessor;
    }


    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(

            @RequestBody
            byte[] rawBody,

            @RequestHeader(
                    value = "X-Razorpay-Signature",
                    required = false
            )
            String signature,

            @RequestHeader(
                    value = "x-razorpay-event-id",
                    required = false
            )
            String eventId
    ) {


        // =========================================
        // 1. VERIFY RAZORPAY SIGNATURE
        // =========================================

        boolean valid =
                razorpayWebhookService
                        .verifySignature(
                                rawBody,
                                signature
                        );


        if (!valid) {

            System.out.println(
                    "❌ INVALID RAZORPAY WEBHOOK"
            );


            return ResponseEntity
                    .status(401)
                    .body(
                            "Invalid webhook signature"
                    );
        }


        // =========================================
        // 2. READ EVENT TYPE
        // =========================================

        String eventType =
                razorpayWebhookService
                        .getEventType(
                                rawBody
                        );


        System.out.println(
                "================================="
        );

        System.out.println(
                "✅ VALID RAZORPAY WEBHOOK"
        );

        System.out.println(
                "Event ID   : "
                        + eventId
        );

        System.out.println(
                "Event Type : "
                        + eventType
        );

        System.out.println(
                "================================="
        );


        // =========================================
        // 3. PROCESS BUSINESS LOGIC
        // =========================================

        String result =
                razorpayWebhookProcessor
                        .process(
                                rawBody,
                                eventId
                        );


        System.out.println(
                "Webhook Result: "
                        + result
        );


        // =========================================
        // 4. RETURN 200 TO RAZORPAY
        // =========================================

        return ResponseEntity.ok(
                result
        );
    }
}