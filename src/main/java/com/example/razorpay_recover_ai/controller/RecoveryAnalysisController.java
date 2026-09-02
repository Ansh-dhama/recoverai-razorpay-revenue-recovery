package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.RecoveryAnalysisResponse;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.serviceInterface.RecoveryAnalysisService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/recovery-cases"
)
public class RecoveryAnalysisController {

    private final RecoveryAnalysisService
            recoveryAnalysisService;


    public RecoveryAnalysisController(
            RecoveryAnalysisService recoveryAnalysisService
    ) {

        this.recoveryAnalysisService =
                recoveryAnalysisService;
    }


    // =========================================================
    // ANALYZE ONE CASE
    // =========================================================

    @PostMapping("/{id}/analyze")
    public ResponseEntity<RecoveryAnalysisResponse> analyzeCase(
            @PathVariable Long id
    ) {

        RecoveryCase recoveryCase =
                recoveryAnalysisService.analyzeCase(id);

        RecoveryAnalysisResponse response =
                new RecoveryAnalysisResponse(
                        recoveryCase.getId(),
                        recoveryCase.getStatus(),
                        recoveryCase.getRecommendedAction(),
                        recoveryCase.getConfidence(),
                        recoveryCase.getReason(),
                        recoveryCase.getAttemptCount()
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // ANALYZE ALL PENDING CASES
    // =========================================================

    @PostMapping("/analyze-pending")
    public ResponseEntity<List<RecoveryCase>>
    analyzePendingCases() {

        List<RecoveryCase> cases =
                recoveryAnalysisService
                        .analyzePendingCases();


        return ResponseEntity.ok(
                cases
        );
    }
}