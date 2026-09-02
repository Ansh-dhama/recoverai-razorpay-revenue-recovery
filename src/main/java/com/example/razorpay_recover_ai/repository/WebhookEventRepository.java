package com.example.razorpay_recover_ai.repository;

import com.example.razorpay_recover_ai.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository
        extends JpaRepository<WebhookEvent, String> {
}