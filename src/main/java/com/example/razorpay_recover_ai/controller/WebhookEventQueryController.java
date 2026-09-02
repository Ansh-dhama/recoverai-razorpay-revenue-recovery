package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.WebhookEventListResponse;
import com.example.razorpay_recover_ai.entity.WebhookEvent;
import com.example.razorpay_recover_ai.repository.WebhookEventRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhook-events")
public class WebhookEventQueryController {

    private final WebhookEventRepository repository;


    public WebhookEventQueryController(
            WebhookEventRepository repository
    ) {

        this.repository =
                repository;
    }


    @GetMapping
    public Page<WebhookEventListResponse> getEvents(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return repository
                .findAll(
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "receivedAt"
                                )
                        )
                )
                .map(
                        this::map
                );
    }


    private WebhookEventListResponse map(
            WebhookEvent event
    ) {

        return new WebhookEventListResponse(

                event.getEventId(),

                event.getEventType(),

                event.getStatus(),

                event.getReceivedAt(),

                event.getProcessedAt()
        );
    }
}