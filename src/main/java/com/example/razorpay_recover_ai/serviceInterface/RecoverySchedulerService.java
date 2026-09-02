package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.entity.RecoveryCase;

import java.time.LocalDateTime;
import java.util.List;

public interface RecoverySchedulerService {

    RecoveryCase scheduleRetry(
            Long recoveryCaseId,
            LocalDateTime retryAt
    );

    List<RecoveryCase> processDueRetries();
}