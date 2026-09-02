package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import com.example.razorpay_recover_ai.exception.InvalidRecoveryStateException;
import com.example.razorpay_recover_ai.repository.RecoveryCaseRepository;

import com.example.razorpay_recover_ai.serviceInterface.RecoverySchedulerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecoverySchedulerServiceImpl
        implements RecoverySchedulerService {

    private static final int MAX_RECOVERY_ATTEMPTS = 3;

    private final RecoveryCaseRepository
            recoveryCaseRepository;


    public RecoverySchedulerServiceImpl(
            RecoveryCaseRepository recoveryCaseRepository
    ) {

        this.recoveryCaseRepository =
                recoveryCaseRepository;
    }


    // =========================================================
    // SCHEDULE ONE RETRY
    // =========================================================

    @Override
    @Transactional
    public RecoveryCase scheduleRetry(
            Long recoveryCaseId,
            LocalDateTime retryAt
    ) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository
                        .findById(recoveryCaseId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "RecoveryCase not found: "
                                                        + recoveryCaseId
                                        )
                        );


        if (recoveryCase.getRecommendedAction()
                != RecoveryAction.RETRY_LATER) {

            throw new InvalidRecoveryStateException(
                    "Only RETRY_LATER cases can be scheduled"
            );
        }


        if (recoveryCase.getStatus()
                != RecoveryStatus.RECOVERY_PLANNED
                &&
                recoveryCase.getStatus()
                        != RecoveryStatus.RECOVERY_IN_PROGRESS) {

            throw new InvalidRecoveryStateException(
                    "RecoveryCase cannot be scheduled from status: "
                            + recoveryCase.getStatus()
            );
        }


        if (retryAt == null
                || !retryAt.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Retry time must be in the future"
            );
        }


        recoveryCase.setNextRetryAt(
                retryAt
        );


        recoveryCase.setStatus(
                RecoveryStatus.RECOVERY_SCHEDULED
        );


        return recoveryCaseRepository.save(
                recoveryCase
        );
    }


    // =========================================================
    // PROCESS ALL DUE RETRIES
    // =========================================================

    @Override
    @Transactional
    public List<RecoveryCase> processDueRetries() {

        LocalDateTime now =
                LocalDateTime.now();


        List<RecoveryCase> dueCases =
                recoveryCaseRepository
                        .findDueRetries(
                                RecoveryStatus.RECOVERY_SCHEDULED

                        );


        List<RecoveryCase> processed =
                new ArrayList<>();


        for (RecoveryCase recoveryCase
                : dueCases) {


            Integer attempts =
                    recoveryCase
                            .getAttemptCount();


            if (attempts == null) {

                attempts = 0;
            }


            // =============================================
            // MAX RETRY PROTECTION
            // =============================================

            if (attempts >= MAX_RECOVERY_ATTEMPTS) {

                recoveryCase.setStatus(
                        RecoveryStatus.HUMAN_REVIEW
                );


                recoveryCase.setNextRetryAt(
                        null
                );


                processed.add(
                        recoveryCaseRepository
                                .save(recoveryCase)
                );


                continue;
            }


            // =============================================
            // RETRY IS NOW DUE
            // =============================================

            recoveryCase.setStatus(
                    RecoveryStatus.RECOVERY_IN_PROGRESS
            );


            recoveryCase.setLastAttemptAt(
                    now
            );


            recoveryCase.setNextRetryAt(
                    null
            );


            recoveryCase.setAttemptCount(
                    attempts + 1
            );


            processed.add(
                    recoveryCaseRepository
                            .save(recoveryCase)
            );
        }


        return processed;
    }
}