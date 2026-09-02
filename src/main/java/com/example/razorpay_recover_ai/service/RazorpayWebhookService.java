package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.config.RazorpayProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class RazorpayWebhookService {

    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;

    public RazorpayWebhookService(
            RazorpayProperties razorpayProperties,
            ObjectMapper objectMapper
    ) {
        this.razorpayProperties = razorpayProperties;
        this.objectMapper = objectMapper;
    }

    public boolean verifySignature(
            byte[] rawBody,
            String receivedSignature
    ) {

        try {

            if (receivedSignature == null
                    || receivedSignature.isBlank()) {
                return false;
            }

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            razorpayProperties
                                    .webhookSecret()
                                    .getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(secretKey);

            byte[] expectedSignature =
                    mac.doFinal(rawBody);

            byte[] receivedSignatureBytes =
                    HexFormat.of()
                            .parseHex(receivedSignature);

            return MessageDigest.isEqual(
                    expectedSignature,
                    receivedSignatureBytes
            );

        } catch (Exception e) {

            System.out.println(
                    "Webhook signature verification error: "
                            + e.getClass().getSimpleName()
            );

            return false;
        }
    }

    public String getEventType(
            byte[] rawBody
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(rawBody);

            String event =
                    root.path("event")
                            .asText();

            if (event == null
                    || event.isBlank()) {

                throw new IllegalArgumentException(
                        "Webhook event is missing"
                );
            }

            return event;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid Razorpay webhook payload",
                    e
            );
        }
    }
}