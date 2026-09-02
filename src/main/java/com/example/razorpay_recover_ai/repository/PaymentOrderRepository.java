package com.example.razorpay_recover_ai.repository;

import com.example.razorpay_recover_ai.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder>
    findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<PaymentOrder>
    findByMerchantOrderId(
            String merchantOrderId
    );

    boolean existsByMerchantOrderId(
            String merchantOrderId
    );

}