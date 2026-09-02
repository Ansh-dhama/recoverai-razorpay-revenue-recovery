package com.example.razorpay_recover_ai.entity;

import com.example.razorpay_recover_ai.enums.AnalysisSource;
import com.example.razorpay_recover_ai.enums.RecoveryAction;
import com.example.razorpay_recover_ai.enums.RecoveryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_cases")
@Getter
@Setter
@NoArgsConstructor
public class RecoveryCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "payment_id",
            nullable = false,
            unique = true
    )
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecoveryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_action")
    private RecoveryAction recommendedAction;

    private Double confidence;

    @Column(length = 1000)
    private String reason;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private Integer attemptCount;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_source")
    private AnalysisSource analysisSource;
    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}