package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.DashboardSummaryResponse;
import com.example.razorpay_recover_ai.enums.PaymentStatus;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import com.example.razorpay_recover_ai.repository.PaymentOrderRepository;
import com.example.razorpay_recover_ai.repository.PaymentRepository;
import com.example.razorpay_recover_ai.repository.RecoveryCaseRepository;

import com.example.razorpay_recover_ai.serviceInterface.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl
        implements DashboardService {

    private final PaymentOrderRepository paymentOrderRepository;

    private final PaymentRepository paymentRepository;

    private final RecoveryCaseRepository recoveryCaseRepository;


    public DashboardServiceImpl(
            PaymentOrderRepository paymentOrderRepository,
            PaymentRepository paymentRepository,
            RecoveryCaseRepository recoveryCaseRepository
    ) {

        this.paymentOrderRepository =
                paymentOrderRepository;

        this.paymentRepository =
                paymentRepository;

        this.recoveryCaseRepository =
                recoveryCaseRepository;
    }


    @Override
    public DashboardSummaryResponse getSummary() {

        return new DashboardSummaryResponse(

                paymentOrderRepository.count(),

                paymentRepository.count(),

                paymentRepository.countByStatus(
                        PaymentStatus.FAILED
                ),

                recoveryCaseRepository.count(),

                recoveryCaseRepository.countByStatus(
                        RecoveryStatus.PENDING_ANALYSIS
                ),

                recoveryCaseRepository.countByStatus(
                        RecoveryStatus.RECOVERY_PLANNED
                ),

                recoveryCaseRepository.countByStatus(
                        RecoveryStatus.RECOVERY_SCHEDULED
                ),

                recoveryCaseRepository.countByStatus(
                        RecoveryStatus.RECOVERED
                ),

                recoveryCaseRepository.countByStatus(
                        RecoveryStatus.HUMAN_REVIEW
                )
        );
    }
}