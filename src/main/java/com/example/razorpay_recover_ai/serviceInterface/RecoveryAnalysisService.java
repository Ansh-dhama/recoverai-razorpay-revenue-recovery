package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.entity.RecoveryCase;

import java.util.List;

public interface RecoveryAnalysisService {

    RecoveryCase analyzeCase(
            Long recoveryCaseId
    );

    List<RecoveryCase> analyzePendingCases();
}