package com.example.razorpay_recover_ai.repository;

import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecoveryCaseRepository
        extends JpaRepository<RecoveryCase, Long> {

    boolean existsByPayment_Id(
            Long paymentId
    );

    Optional<RecoveryCase> findByPayment_Id(
            Long paymentId
    );

    List<RecoveryCase> findByStatus(
            RecoveryStatus status
    );

    @Query("""
        SELECT r
        FROM RecoveryCase r
        WHERE r.status = :status
          AND r.nextRetryAt IS NOT NULL
          AND r.nextRetryAt <= CURRENT_TIMESTAMP
        """)
    List<RecoveryCase> findDueRetries(
            @Param("status") RecoveryStatus status
    );long countByStatus(
            RecoveryStatus status
    );
}