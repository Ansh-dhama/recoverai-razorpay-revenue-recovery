package com.example.razorpay_recover_ai.entity;

import com.example.razorpay_recover_ai.enums.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_orders",
        indexes = {

                @Index(
                        name = "idx_payment_order_customer",
                        columnList = "customer_id"
                ),

                @Index(
                        name = "idx_payment_order_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "merchant_order_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String merchantOrderId;

    @Column(
            name = "razorpay_order_id",
            nullable = false,
            unique = true,
            length = 100
    )
    private String razorpayOrderId;

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

    @Column(
            nullable = false,
            unique = true,
            length = 40
    )
    private String receipt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentOrderStatus status;

    @Column(nullable = false)
    private Integer attempts;

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
    public void createTimestamp() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (attempts == null) {
            attempts = 0;
        }
    }

    @PreUpdate
    public void updateTimestamp() {

        updatedAt =
                LocalDateTime.now();
    }
}