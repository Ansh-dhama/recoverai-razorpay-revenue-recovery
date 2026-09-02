package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.WebhookEventStatus;

import java.time.LocalDateTime;

public record WebhookEventListResponse(

        String eventId,

        String eventType,

        WebhookEventStatus status,

        LocalDateTime receivedAt,

        LocalDateTime processedAt

) {
}