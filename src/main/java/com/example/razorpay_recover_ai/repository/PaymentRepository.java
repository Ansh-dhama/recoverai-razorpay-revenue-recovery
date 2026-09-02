package com.example.razorpay_recover_ai.repository;

import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayPaymentId(
            String razorpayPaymentId
    );

    boolean existsByRazorpayPaymentId(
            String razorpayPaymentId
    );

    List<Payment> findByStatus(
            PaymentStatus status
    );
    long countByStatus(
            PaymentStatus status
    );
}