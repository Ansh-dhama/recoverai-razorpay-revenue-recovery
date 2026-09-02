package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.PaymentOrder;
import com.example.razorpay_recover_ai.entity.WebhookEvent;

import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.PaymentOrderStatus;
import com.example.razorpay_recover_ai.enums.PaymentStatus;
import com.example.razorpay_recover_ai.enums.WebhookEventStatus;

import com.example.razorpay_recover_ai.repository.PaymentOrderRepository;
import com.example.razorpay_recover_ai.repository.PaymentRepository;
import com.example.razorpay_recover_ai.repository.WebhookEventRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class RazorpayWebhookProcessor {

    private final ObjectMapper objectMapper;

    private final WebhookEventRepository webhookEventRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentOrderRepository paymentOrderRepository;

    private final RecoveryCaseService recoveryCaseService;


    public RazorpayWebhookProcessor(
            ObjectMapper objectMapper,
            WebhookEventRepository webhookEventRepository,
            PaymentRepository paymentRepository,
            PaymentOrderRepository paymentOrderRepository,
            RecoveryCaseService recoveryCaseService
    ) {

        this.objectMapper = objectMapper;

        this.webhookEventRepository =
                webhookEventRepository;

        this.paymentRepository =
                paymentRepository;

        this.paymentOrderRepository =
                paymentOrderRepository;

        this.recoveryCaseService =
                recoveryCaseService;
    }


    // =========================================================
    // MAIN WEBHOOK PROCESSING METHOD
    // =========================================================

    @Transactional
    public String process(
            byte[] rawBody,
            String eventId
    ) {

        // -----------------------------------------------------
        // 1. VALIDATE EVENT ID
        // -----------------------------------------------------

        if (eventId == null
                || eventId.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay event id is missing"
            );
        }


        // -----------------------------------------------------
        // 2. DUPLICATE WEBHOOK CHECK
        // -----------------------------------------------------

        if (webhookEventRepository
                .existsById(eventId)) {

            System.out.println(
                    "Duplicate webhook ignored: "
                            + eventId
            );

            return "DUPLICATE";
        }


        // -----------------------------------------------------
        // 3. PARSE JSON
        // -----------------------------------------------------

        JsonNode root;

        try {

            root =
                    objectMapper
                            .readTree(rawBody);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid Razorpay webhook JSON",
                    e
            );
        }


        // -----------------------------------------------------
        // 4. READ EVENT TYPE
        // -----------------------------------------------------

        String eventType =
                root.path("event")
                        .asText();


        if (eventType == null
                || eventType.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay event type is missing"
            );
        }


        System.out.println(
                "Processing Razorpay event: "
                        + eventType
        );


        // -----------------------------------------------------
        // 5. CREATE WEBHOOK EVENT
        // -----------------------------------------------------

        WebhookEvent webhookEvent =
                new WebhookEvent();


        webhookEvent.setEventId(
                eventId
        );


        webhookEvent.setEventType(
                eventType
        );


        webhookEvent.setStatus(
                WebhookEventStatus.RECEIVED
        );


        webhookEvent.setReceivedAt(
                LocalDateTime.now()
        );


        /*
         * IMPORTANT FIX:
         *
         * WebhookEvent uses manually assigned eventId.
         *
         * Spring Data may use merge().
         *
         * Therefore always use the entity returned by saveAndFlush().
         */
        webhookEvent =
                webhookEventRepository
                        .saveAndFlush(
                                webhookEvent
                        );


        // -----------------------------------------------------
        // 6. HANDLE EVENT
        // -----------------------------------------------------

        switch (eventType) {


            case "payment.authorized" -> {

                processPayment(
                        root,
                        PaymentStatus.AUTHORIZED
                );
            }


            case "payment.failed" -> {

                processPayment(
                        root,
                        PaymentStatus.FAILED
                );
            }


            case "payment.captured" -> {

                processPayment(
                        root,
                        PaymentStatus.CAPTURED
                );
            }


            default -> {

                webhookEvent.setStatus(
                        WebhookEventStatus.IGNORED
                );


                webhookEvent.setProcessedAt(
                        LocalDateTime.now()
                );


                /*
                 * Explicit save so IGNORED
                 * status is persisted too.
                 */
                webhookEventRepository.save(
                        webhookEvent
                );


                System.out.println(
                        "Webhook ignored: "
                                + eventType
                );


                return "IGNORED";
            }
        }


        // -----------------------------------------------------
        // 7. MARK WEBHOOK PROCESSED
        // -----------------------------------------------------

        webhookEvent.setStatus(
                WebhookEventStatus.PROCESSED
        );


        webhookEvent.setProcessedAt(
                LocalDateTime.now()
        );


        /*
         * IMPORTANT FIX:
         *
         * Explicitly persist the final state.
         */
        webhookEventRepository.save(
                webhookEvent
        );


        System.out.println(
                "Webhook successfully processed: "
                        + eventId
        );


        return "PROCESSED";
    }


    // =========================================================
    // PAYMENT PROCESSING
    // =========================================================

    private void processPayment(
            JsonNode root,
            PaymentStatus incomingStatus
    ) {

        // -----------------------------------------------------
        // RAZORPAY PAYMENT PAYLOAD
        // -----------------------------------------------------

        JsonNode paymentNode =
                root.path("payload")
                        .path("payment")
                        .path("entity");


        // -----------------------------------------------------
        // PAYMENT ID
        // -----------------------------------------------------

        String razorpayPaymentId =
                paymentNode
                        .path("id")
                        .asText();


        // -----------------------------------------------------
        // RAZORPAY ORDER ID
        // -----------------------------------------------------

        String razorpayOrderId =
                paymentNode
                        .path("order_id")
                        .asText();


        if (razorpayPaymentId == null
                || razorpayPaymentId.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay payment id is missing"
            );
        }


        if (razorpayOrderId == null
                || razorpayOrderId.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay order id is missing"
            );
        }


        // -----------------------------------------------------
        // FIND PAYMENT ORDER
        // -----------------------------------------------------

        PaymentOrder paymentOrder =
                paymentOrderRepository
                        .findByRazorpayOrderId(
                                razorpayOrderId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "PaymentOrder not found for Razorpay order id: "
                                                        + razorpayOrderId
                                        )
                        );


        // -----------------------------------------------------
        // FIND EXISTING PAYMENT
        // -----------------------------------------------------

        Optional<Payment> existingPayment =
                paymentRepository
                        .findByRazorpayPaymentId(
                                razorpayPaymentId
                        );


        boolean newPayment =
                existingPayment.isEmpty();


        Payment payment =
                existingPayment
                        .orElseGet(
                                Payment::new
                        );


        // -----------------------------------------------------
        // NEW PAYMENT
        // -----------------------------------------------------

        if (newPayment) {

            payment.setRazorpayPaymentId(
                    razorpayPaymentId
            );


            payment.setPaymentOrder(
                    paymentOrder
            );


            payment.setOrderId(
                    paymentOrder
                            .getMerchantOrderId()
            );


            payment.setCustomerId(
                    paymentOrder
                            .getCustomerId()
            );


            /*
             * Every unique Razorpay payment id
             * represents a new payment attempt.
             */
            Integer attempts =
                    paymentOrder
                            .getAttempts();


            if (attempts == null) {

                attempts = 0;
            }


            paymentOrder.setAttempts(
                    attempts + 1
            );
        }


        // -----------------------------------------------------
        // COMMON PAYMENT INFORMATION
        // -----------------------------------------------------

        payment.setAmount(
                paymentNode
                        .path("amount")
                        .asLong(
                                paymentOrder
                                        .getAmount()
                        )
        );


        payment.setCurrency(
                paymentNode
                        .path("currency")
                        .asText(
                                paymentOrder
                                        .getCurrency()
                        )
                        .toUpperCase(
                                Locale.ROOT
                        )
        );


        payment.setPaymentMethod(
                mapPaymentMethod(
                        paymentNode
                                .path("method")
                                .asText(null)
                )
        );


        // -----------------------------------------------------
        // OUT-OF-ORDER EVENT PROTECTION
        // -----------------------------------------------------

        /*
         * Never allow:
         *
         * CAPTURED
         *    ↓
         * AUTHORIZED / FAILED
         */
        if (payment.getStatus()
                == PaymentStatus.CAPTURED
                &&
                incomingStatus
                        != PaymentStatus.CAPTURED) {

            System.out.println(
                    "Ignoring old event because payment is already CAPTURED"
            );

            return;
        }


        // =====================================================
        // PAYMENT AUTHORIZED
        // =====================================================

        if (incomingStatus
                == PaymentStatus.AUTHORIZED) {


            payment.setStatus(
                    PaymentStatus.AUTHORIZED
            );


            if (paymentOrder.getStatus()
                    != PaymentOrderStatus.PAID) {

                paymentOrder.setStatus(
                        PaymentOrderStatus.ATTEMPTED
                );
            }


            paymentRepository.save(
                    payment
            );


            paymentOrderRepository.save(
                    paymentOrder
            );


            System.out.println(
                    "Payment AUTHORIZED: "
                            + razorpayPaymentId
            );


            return;
        }


        // =====================================================
        // PAYMENT CAPTURED
        // =====================================================

        if (incomingStatus
                == PaymentStatus.CAPTURED) {


            payment.setStatus(
                    PaymentStatus.CAPTURED
            );


            payment.setFailureReason(
                    null
            );


            payment.setFailureDescription(
                    null
            );


            paymentOrder.setStatus(
                    PaymentOrderStatus.PAID
            );


            Payment savedPayment =
                    paymentRepository
                            .save(
                                    payment
                            );


            paymentOrderRepository.save(
                    paymentOrder
            );


            /*
             * If this payment previously had
             * a RecoveryCase, mark it recovered.
             */
            recoveryCaseService
                    .markRecovered(
                            savedPayment
                    );


            System.out.println(
                    "Payment CAPTURED: "
                            + razorpayPaymentId
            );


            return;
        }


        // =====================================================
        // PAYMENT FAILED
        // =====================================================

        if (incomingStatus
                == PaymentStatus.FAILED) {


            payment.setStatus(
                    PaymentStatus.FAILED
            );


            // -------------------------------------------------
            // FAILURE REASON
            // -------------------------------------------------

            String failureReason =
                    getNullableText(
                            paymentNode,
                            "error_reason"
                    );


            /*
             * Some Razorpay failures may not
             * contain error_reason.
             *
             * Use error_code as fallback.
             */
            if (failureReason == null
                    || failureReason.isBlank()) {

                failureReason =
                        getNullableText(
                                paymentNode,
                                "error_code"
                        );
            }


            payment.setFailureReason(
                    failureReason
            );


            // -------------------------------------------------
            // FAILURE DESCRIPTION
            // -------------------------------------------------

            payment.setFailureDescription(
                    getNullableText(
                            paymentNode,
                            "error_description"
                    )
            );


            // -------------------------------------------------
            // ORDER STATUS
            // -------------------------------------------------

            if (paymentOrder.getStatus()
                    != PaymentOrderStatus.PAID) {

                paymentOrder.setStatus(
                        PaymentOrderStatus.ATTEMPTED
                );
            }


            // -------------------------------------------------
            // SAVE PAYMENT
            // -------------------------------------------------

            Payment savedPayment =
                    paymentRepository
                            .save(
                                    payment
                            );


            paymentOrderRepository.save(
                    paymentOrder
            );


            // -------------------------------------------------
            // CREATE RECOVERY CASE
            // -------------------------------------------------

            recoveryCaseService
                    .createForFailedPayment(
                            savedPayment
                    );


            System.out.println(
                    "Payment FAILED: "
                            + razorpayPaymentId
            );


            System.out.println(
                    "Failure reason: "
                            + failureReason
            );


            return;
        }
    }


    // =========================================================
    // PAYMENT METHOD MAPPER
    // =========================================================

    private PaymentMethod mapPaymentMethod(
            String method
    ) {

        if (method == null
                || method.isBlank()) {

            return PaymentMethod.OTHER;
        }


        return switch (
                method.toLowerCase(
                        Locale.ROOT
                )
                ) {


            case "upi" ->
                    PaymentMethod.UPI;


            case "card" ->
                    PaymentMethod.CARD;


            case "netbanking" ->
                    PaymentMethod.NETBANKING;


            case "wallet" ->
                    PaymentMethod.WALLET;


            case "emi" ->
                    PaymentMethod.EMI;


            default ->
                    PaymentMethod.OTHER;
        };
    }


    // =========================================================
    // NULL SAFE JSON FIELD
    // =========================================================

    private String getNullableText(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);


        if (value == null
                || value.isNull()) {

            return null;
        }


        String text =
                value.asText();


        if (text.isBlank()) {

            return null;
        }


        return text;
    }
}