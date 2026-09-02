package com.example.razorpay_recover_ai.entity;

import com.example.razorpay_recover_ai.enums.PaymentMethod;
import com.example.razorpay_recover_ai.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "razorpay_payment_id",
            unique = true,
            length = 100
    )
    private String razorpayPaymentId;

    @Column(
            name = "order_id",
            nullable = false,
            length = 100
    )
    private String orderId;

    @Column(
            name = "customer_id",
            nullable = false,
            length = 100
    )
    private String customerId;

    @Column(nullable = false)
    private Long amount;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false
    )
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false
    )
    private PaymentStatus status;

    @Column(
            name = "failure_reason",
            length = 255
    )
    private String failureReason;

    @Column(
            name = "failure_description",
            length = 1000
    )
    private String failureDescription;

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

    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "payment_order_id",
            nullable = false
    )
    private PaymentOrder paymentOrder;


}