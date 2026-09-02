package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.RecoveryScheduleResponse;
import com.example.razorpay_recover_ai.dto.ScheduleRecoveryRequest;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.serviceInterface.RecoverySchedulerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recovery-cases")
public class RecoverySchedulerController {

    private final RecoverySchedulerService
            recoverySchedulerService;

    public RecoverySchedulerController(
            RecoverySchedulerService recoverySchedulerService
    ) {

        this.recoverySchedulerService =
                recoverySchedulerService;
    }


    // =========================================================
    // SCHEDULE ONE RECOVERY
    // =========================================================

    @PostMapping("/{id}/schedule")
    public ResponseEntity<RecoveryScheduleResponse>
    schedule(
            @PathVariable Long id,
            @RequestBody ScheduleRecoveryRequest request
    ) {

        RecoveryCase recoveryCase =
                recoverySchedulerService
                        .scheduleRetry(
                                id,
                                request.retryAt()
                        );


        RecoveryScheduleResponse response =
                mapToResponse(
                        recoveryCase
                );


        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // MANUALLY PROCESS DUE RECOVERIES
    // =========================================================

    @PostMapping("/process-due")
    public ResponseEntity<List<RecoveryScheduleResponse>>
    processDue() {

        List<RecoveryCase> cases =
                recoverySchedulerService
                        .processDueRetries();


        List<RecoveryScheduleResponse> response =
                cases.stream()
                        .map(this::mapToResponse)
                        .toList();


        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private RecoveryScheduleResponse mapToResponse(
            RecoveryCase recoveryCase
    ) {

        return new RecoveryScheduleResponse(

                recoveryCase.getId(),

                recoveryCase.getStatus(),

                recoveryCase.getRecommendedAction(),

                recoveryCase.getAttemptCount(),

                recoveryCase.getNextRetryAt(),

                recoveryCase.getLastAttemptAt()
        );
    }
}