package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.CreatePaymentRequest;
import com.example.razorpay_recover_ai.dto.PaymentResponse;
import com.example.razorpay_recover_ai.dto.UpdatePaymentStatusRequest;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.enums.PaymentStatus;
import com.example.razorpay_recover_ai.exception.InvalidPaymentException;
import com.example.razorpay_recover_ai.exception.ResourceNotFoundException;
import com.example.razorpay_recover_ai.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final RecoveryCaseService recoveryCaseService;

    public PaymentService(
            PaymentRepository paymentRepository,
            RecoveryCaseService recoveryCaseService
    ) {
        this.paymentRepository =
                paymentRepository;

        this.recoveryCaseService =
                recoveryCaseService;
    }

    @Transactional
    public PaymentResponse createPayment(
            CreatePaymentRequest request
    ) {

        validatePayment(request);

        if (StringUtils.hasText(
                request.razorpayPaymentId()
        )) {

            boolean exists =
                    paymentRepository
                            .existsByRazorpayPaymentId(
                                    request.razorpayPaymentId()
                            );

            if (exists) {
                throw new InvalidPaymentException(
                        "Payment with Razorpay payment id already exists"
                );
            }
        }

        Payment payment = new Payment();

        payment.setRazorpayPaymentId(
                request.razorpayPaymentId()
        );

        payment.setOrderId(
                request.orderId()
        );

        payment.setCustomerId(
                request.customerId()
        );

        payment.setAmount(
                request.amount()
        );

        payment.setCurrency(
                request.currency()
                        .toUpperCase(Locale.ROOT)
        );

        payment.setPaymentMethod(
                request.paymentMethod()
        );

        payment.setStatus(
                request.status()
        );

        payment.setFailureReason(
                request.failureReason()
        );

        payment.setFailureDescription(
                request.failureDescription()
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        /*
         * Main Phase-1 business workflow
         */
        if (savedPayment.getStatus()
                == PaymentStatus.FAILED) {

            recoveryCaseService
                    .createForFailedPayment(
                            savedPayment
                    );
        }

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {

        Payment payment =
                getPaymentEntity(id);

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getFailedPayments() {

        return paymentRepository
                .findByStatus(
                        PaymentStatus.FAILED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(
            Long id,
            UpdatePaymentStatusRequest request
    ) {

        Payment payment =
                getPaymentEntity(id);

        if (request.status()
                == PaymentStatus.FAILED) {

            if (!StringUtils.hasText(
                    request.failureReason()
            )) {

                throw new InvalidPaymentException(
                        "failureReason is required when payment status is FAILED"
                );
            }

            payment.setFailureReason(
                    request.failureReason()
            );

            payment.setFailureDescription(
                    request.failureDescription()
            );
        }

        payment.setStatus(
                request.status()
        );

        Payment updatedPayment =
                paymentRepository.save(payment);

        /*
         * A CREATED/PENDING payment may later fail.
         *
         * Ensure a recovery case gets created.
         */
        if (updatedPayment.getStatus()
                == PaymentStatus.FAILED) {

            recoveryCaseService
                    .createForFailedPayment(
                            updatedPayment
                    );
        }

        return mapToResponse(updatedPayment);
    }

    private Payment getPaymentEntity(Long id) {

        return paymentRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Payment not found: " + id
                                )
                );
    }

    private void validatePayment(
            CreatePaymentRequest request
    ) {

        if (request.status()
                == PaymentStatus.FAILED
                &&
                !StringUtils.hasText(
                        request.failureReason()
                )) {

            throw new InvalidPaymentException(
                    "failureReason is required for FAILED payments"
            );
        }
    }

    private PaymentResponse mapToResponse(
            Payment payment
    ) {

        return new PaymentResponse(

                payment.getId(),

                payment.getRazorpayPaymentId(),

                payment.getOrderId(),

                payment.getCustomerId(),

                payment.getAmount(),

                payment.getCurrency(),

                payment.getPaymentMethod(),

                payment.getStatus(),

                payment.getFailureReason(),

                payment.getFailureDescription(),

                payment.getCreatedAt(),

                payment.getUpdatedAt()
        );
    }
}