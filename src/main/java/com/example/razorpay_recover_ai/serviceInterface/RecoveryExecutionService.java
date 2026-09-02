package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.dto.RecoveryExecutionResponse;

public interface RecoveryExecutionService {

    RecoveryExecutionResponse execute(
            Long recoveryCaseId
    );
}