package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.config.RazorpayProperties;
import com.example.razorpay_recover_ai.dto.RazorpayPublicConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/config")
public class PublicConfigController {

    private final RazorpayProperties razorpayProperties;

    public PublicConfigController(
            RazorpayProperties razorpayProperties
    ) {
        this.razorpayProperties = razorpayProperties;
    }

    @GetMapping("/razorpay")
    public ResponseEntity<RazorpayPublicConfigResponse>
    getRazorpayConfig() {

        return ResponseEntity.ok(
                new RazorpayPublicConfigResponse(
                        razorpayProperties.keyId()
                )
        );
    }
}