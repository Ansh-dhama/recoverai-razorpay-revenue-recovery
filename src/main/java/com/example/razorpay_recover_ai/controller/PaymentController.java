package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.PaymentListResponse;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.repository.PaymentRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;


    public PaymentController(
            PaymentRepository paymentRepository
    ) {

        this.paymentRepository =
                paymentRepository;
    }


    @GetMapping
    public Page<PaymentListResponse> getPayments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return paymentRepository
                .findAll(
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "id"
                                )
                        )
                )
                .map(
                        this::map
                );
    }


    private PaymentListResponse map(
            Payment payment
    ) {

        return new PaymentListResponse(

                payment.getId(),

                payment.getRazorpayPaymentId(),

                payment.getOrderId(),

                payment.getCustomerId(),

                payment.getAmount(),

                payment.getCurrency(),

                payment.getPaymentMethod(),

                payment.getStatus(),

                payment.getFailureReason(),

                payment.getFailureDescription()
        );
    }
}