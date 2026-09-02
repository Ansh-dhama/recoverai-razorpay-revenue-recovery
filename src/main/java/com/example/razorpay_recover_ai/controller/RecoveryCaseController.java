package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.RecoveryCaseResponse;
import com.example.razorpay_recover_ai.service.RecoveryCaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recoveries")
public class RecoveryCaseController {

    private final RecoveryCaseService recoveryCaseService;

    public RecoveryCaseController(
            RecoveryCaseService recoveryCaseService
    ) {
        this.recoveryCaseService =
                recoveryCaseService;
    }

    @GetMapping
    public ResponseEntity<List<RecoveryCaseResponse>>
    getAllRecoveries() {

        return ResponseEntity.ok(
                recoveryCaseService
                        .getAllRecoveries()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecoveryCaseResponse>
    getRecovery(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                recoveryCaseService
                        .getRecovery(id)
        );
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<RecoveryCaseResponse>
    getRecoveryByPayment(

            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                recoveryCaseService
                        .getByPaymentId(
                                paymentId
                        )
        );
    }
}