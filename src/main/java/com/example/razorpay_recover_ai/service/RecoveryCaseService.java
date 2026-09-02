package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.RecoveryCaseResponse;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import com.example.razorpay_recover_ai.exception.ResourceNotFoundException;
import com.example.razorpay_recover_ai.repository.RecoveryCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecoveryCaseService {

    private final RecoveryCaseRepository recoveryCaseRepository;

    public RecoveryCaseService(
            RecoveryCaseRepository recoveryCaseRepository
    ) {
        this.recoveryCaseRepository =
                recoveryCaseRepository;
    }

    @Transactional
    public void createForFailedPayment(Payment payment) {

        if (recoveryCaseRepository
                .existsByPayment_Id(payment.getId())) {

            return;
        }

        RecoveryCase recoveryCase =
                new RecoveryCase();

        recoveryCase.setPayment(payment);

        recoveryCase.setStatus(
                RecoveryStatus.PENDING_ANALYSIS
        );

        recoveryCase.setRecommendedAction(null);

        recoveryCase.setConfidence(null);

        recoveryCase.setReason(null);

        recoveryCase.setAttemptCount(0);

        recoveryCaseRepository.save(recoveryCase);
    }

    @Transactional(readOnly = true)
    public List<RecoveryCaseResponse> getAllRecoveries() {

        return recoveryCaseRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecoveryCaseResponse getRecovery(Long id) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Recovery case not found: " + id
                                        )
                        );

        return mapToResponse(recoveryCase);
    }

    @Transactional(readOnly = true)
    public RecoveryCaseResponse getByPaymentId(
            Long paymentId
    ) {

        RecoveryCase recoveryCase =
                recoveryCaseRepository
                        .findByPayment_Id(paymentId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Recovery case not found for payment: "
                                                        + paymentId
                                        )
                        );

        return mapToResponse(recoveryCase);
    }

    private RecoveryCaseResponse mapToResponse(
            RecoveryCase recoveryCase
    ) {

        return new RecoveryCaseResponse(

                recoveryCase.getId(),

                recoveryCase
                        .getPayment()
                        .getId(),

                recoveryCase.getStatus(),

                recoveryCase.getRecommendedAction(),

                recoveryCase.getConfidence(),

                recoveryCase.getReason(),

                recoveryCase.getAttemptCount(),

                recoveryCase.getCreatedAt(),

                recoveryCase.getUpdatedAt()
        );
    }

    @Transactional
    public void markRecovered(Payment payment) {

        recoveryCaseRepository
                .findByPayment_Id(payment.getId())
                .ifPresent(recoveryCase -> {

                    recoveryCase.setStatus(
                            RecoveryStatus.RECOVERED
                    );

                    recoveryCaseRepository.save(
                            recoveryCase
                    );
                });
    }
}