package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.CreatePaymentOrderRequest;
import com.example.razorpay_recover_ai.dto.PaymentOrderListResponse;
import com.example.razorpay_recover_ai.dto.PaymentOrderResponse;
import com.example.razorpay_recover_ai.repository.PaymentOrderRepository;
import com.example.razorpay_recover_ai.service.PaymentOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-orders")
public class PaymentOrderController {

    private final PaymentOrderService
            paymentOrderService;
private  final PaymentOrderRepository paymentOrderRepository;

    public PaymentOrderController(
            PaymentOrderService paymentOrderService,
            PaymentOrderRepository paymentOrderRepository
    ) {

        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentOrderService =
                paymentOrderService;
    }

    @PostMapping
    public ResponseEntity<PaymentOrderResponse>
    createOrder(

            @Valid
            @RequestBody
            CreatePaymentOrderRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        paymentOrderService
                                .createOrder(request)
                );
    }

    @GetMapping(
            "/merchant/{merchantOrderId}"
    )
    public ResponseEntity<PaymentOrderResponse>
    getOrder(

            @PathVariable
            String merchantOrderId
    ) {

        return ResponseEntity.ok(
                paymentOrderService
                        .getByMerchantOrderId(
                                merchantOrderId
                        )
        );
    }
    @GetMapping
    public Page<PaymentOrderListResponse> getOrders(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return paymentOrderRepository
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
                        order -> new PaymentOrderListResponse(

                                order.getId(),

                                order.getMerchantOrderId(),

                                order.getRazorpayOrderId(),

                                order.getCustomerId(),

                                order.getAmount(),

                                order.getCurrency(),

                                order.getReceipt(),

                                order.getStatus(),

                                order.getAttempts()
                        )
                );
    }
}