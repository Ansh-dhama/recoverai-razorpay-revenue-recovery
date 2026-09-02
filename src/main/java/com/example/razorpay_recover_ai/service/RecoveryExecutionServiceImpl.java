package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.RecoveryExecutionResponse;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import com.example.razorpay_recover_ai.exception.InvalidRecoveryStateException;
import com.example.razorpay_recover_ai.repository.RecoveryCaseRepository;

import com.example.razorpay_recover_ai.serviceInterface.RecoveryExecutionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecoveryExecutionServiceImpl
        implements RecoveryExecutionService {

    private final RecoveryCaseRepository
            recoveryCaseRepository;


    public RecoveryExecutionServiceImpl(
            RecoveryCaseRepository recoveryCaseRepository
    ) {

        this.recoveryCaseRepository =
                recoveryCaseRepository;
    }


    // =========================================================
    // EXECUTE RECOVERY
    // =========================================================

    @Override
    @Transactional
    public RecoveryExecutionResponse execute(
            Long recoveryCaseId
    ) {

        // -----------------------------------------------------
        // 1. FIND RECOVERY CASE
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // 2. VALIDATE STATUS
        // -----------------------------------------------------

        if (recoveryCase.getStatus()
                != RecoveryStatus.RECOVERY_PLANNED) {

            throw new InvalidRecoveryStateException(
                    "RecoveryCase cannot be executed from status: "
                            + recoveryCase.getStatus()
            );
        }
        if (
                recoveryCase.getRecommendedAction()
                        == RecoveryAction.RETRY_LATER
        ) {

            throw new InvalidRecoveryStateException(
                    "RETRY_LATER recovery must be scheduled, not executed directly"
            );
        }

        // -----------------------------------------------------
        // 3. READ RECOMMENDED ACTION
        // -----------------------------------------------------

        RecoveryAction action =
                recoveryCase
                        .getRecommendedAction();


        if (action == null) {

            throw new InvalidRecoveryStateException(
                    "RecoveryCase has no recommended action"
            );
        }


        // -----------------------------------------------------
        // 4. EXECUTE ACTION
        // -----------------------------------------------------

        return switch (action) {

            case RETRY_NOW ->
                    executeRetryNow(
                            recoveryCase
                    );

            case RETRY_LATER ->
                    executeRetryLater(
                            recoveryCase
                    );

            case ALTERNATIVE_PAYMENT_METHOD ->
                    executeAlternativePayment(
                            recoveryCase
                    );

            case HUMAN_REVIEW ->
                    executeHumanReview(
                            recoveryCase
                    );

            case NO_ACTION ->
                    executeNoAction(
                            recoveryCase
                    );
        };
    }


    // =========================================================
    // RETRY NOW
    // =========================================================

    private RecoveryExecutionResponse executeRetryNow(
            RecoveryCase recoveryCase
    ) {

        incrementAttempt(
                recoveryCase
        );


        recoveryCase.setStatus(
                RecoveryStatus.RECOVERY_IN_PROGRESS
        );


        RecoveryCase saved =
                recoveryCaseRepository
                        .save(recoveryCase);


        return buildResponse(

                saved,

                true,

                "Recovery started. Customer should retry the payment now."
        );
    }


    // =========================================================
    // RETRY LATER
    // =========================================================

    private RecoveryExecutionResponse executeRetryLater(
            RecoveryCase recoveryCase
    ) {

        incrementAttempt(
                recoveryCase
        );


        recoveryCase.setStatus(
                RecoveryStatus.RECOVERY_IN_PROGRESS
        );


        RecoveryCase saved =
                recoveryCaseRepository
                        .save(recoveryCase);


        return buildResponse(

                saved,

                false,

                "Recovery is ready for delayed retry scheduling."
        );
    }


    // =========================================================
    // ALTERNATIVE PAYMENT METHOD
    // =========================================================

    private RecoveryExecutionResponse executeAlternativePayment(
            RecoveryCase recoveryCase
    ) {

        incrementAttempt(
                recoveryCase
        );


        recoveryCase.setStatus(
                RecoveryStatus.RECOVERY_IN_PROGRESS
        );


        RecoveryCase saved =
                recoveryCaseRepository
                        .save(recoveryCase);


        return buildResponse(

                saved,

                true,

                "Customer should continue recovery using an alternative payment method."
        );
    }


    // =========================================================
    // HUMAN REVIEW
    // =========================================================

    private RecoveryExecutionResponse executeHumanReview(
            RecoveryCase recoveryCase
    ) {

        recoveryCase.setStatus(
                RecoveryStatus.HUMAN_REVIEW
        );


        RecoveryCase saved =
                recoveryCaseRepository
                        .save(recoveryCase);


        return buildResponse(

                saved,

                false,

                "Recovery case has been moved to human review."
        );
    }


    // =========================================================
    // NO ACTION
    // =========================================================

    private RecoveryExecutionResponse executeNoAction(
            RecoveryCase recoveryCase
    ) {

        recoveryCase.setStatus(
                RecoveryStatus.CANCELLED
        );


        RecoveryCase saved =
                recoveryCaseRepository
                        .save(recoveryCase);


        return buildResponse(

                saved,

                false,

                "No recovery action will be performed for this case."
        );
    }


    // =========================================================
    // INCREMENT ATTEMPT
    // =========================================================

    private void incrementAttempt(
            RecoveryCase recoveryCase
    ) {

        Integer attempts =
                recoveryCase
                        .getAttemptCount();


        if (attempts == null) {

            attempts = 0;
        }


        recoveryCase.setAttemptCount(
                attempts + 1
        );
    }


    // =========================================================
    // BUILD RESPONSE
    // =========================================================

    private RecoveryExecutionResponse buildResponse(
            RecoveryCase recoveryCase,
            boolean customerActionRequired,
            String message
    ) {

        Long paymentId = null;


        if (recoveryCase.getPayment() != null) {

            paymentId =
                    recoveryCase
                            .getPayment()
                            .getId();
        }


        return new RecoveryExecutionResponse(

                recoveryCase.getId(),

                paymentId,

                recoveryCase
                        .getRecommendedAction(),

                recoveryCase
                        .getStatus(),

                recoveryCase
                        .getAttemptCount(),

                customerActionRequired,

                message
        );
    }
}