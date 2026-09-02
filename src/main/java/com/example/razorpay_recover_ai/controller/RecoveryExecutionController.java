package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.RecoveryExecutionResponse;
import com.example.razorpay_recover_ai.serviceInterface.RecoveryExecutionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/v1/recovery-cases"
)
public class RecoveryExecutionController {

    private final RecoveryExecutionService
            recoveryExecutionService;


    public RecoveryExecutionController(
            RecoveryExecutionService recoveryExecutionService
    ) {

        this.recoveryExecutionService =
                recoveryExecutionService;
    }


    @PostMapping("/{id}/execute")
    public ResponseEntity<RecoveryExecutionResponse>
    executeRecovery(
            @PathVariable Long id
    ) {

        RecoveryExecutionResponse response =
                recoveryExecutionService
                        .execute(id);


        return ResponseEntity.ok(
                response
        );
    }
}