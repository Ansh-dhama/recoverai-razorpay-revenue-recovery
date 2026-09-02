package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.config.RazorpayProperties;
import com.example.razorpay_recover_ai.dto.CreatePaymentOrderRequest;
import com.example.razorpay_recover_ai.dto.PaymentOrderResponse;
import com.example.razorpay_recover_ai.entity.PaymentOrder;
import com.example.razorpay_recover_ai.enums.PaymentOrderStatus;
import com.example.razorpay_recover_ai.exception.InvalidPaymentException;
import com.example.razorpay_recover_ai.exception.PaymentGatewayException;
import com.example.razorpay_recover_ai.exception.ResourceNotFoundException;
import com.example.razorpay_recover_ai.repository.PaymentOrderRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class PaymentOrderService {

    private final PaymentOrderRepository
            paymentOrderRepository;

    private final RazorpayClient
            razorpayClient;

    private final RazorpayProperties
            razorpayProperties;

    public PaymentOrderService(
            PaymentOrderRepository paymentOrderRepository,
            RazorpayClient razorpayClient,
            RazorpayProperties razorpayProperties
    ) {

        this.paymentOrderRepository =
                paymentOrderRepository;

        this.razorpayClient =
                razorpayClient;

        this.razorpayProperties =
                razorpayProperties;
    }

    public PaymentOrderResponse createOrder(
            CreatePaymentOrderRequest request
    ) {

        if (paymentOrderRepository
                .existsByMerchantOrderId(
                        request.merchantOrderId()
                )) {

            throw new InvalidPaymentException(
                    "Order already exists: "
                            + request.merchantOrderId()
            );
        }

        String currency =
                request.currency()
                        .toUpperCase(Locale.ROOT);

        String receipt =
                generateReceipt();

        try {

            JSONObject razorpayRequest =
                    new JSONObject();

            razorpayRequest.put(
                    "amount",
                    request.amount()
            );

            razorpayRequest.put(
                    "currency",
                    currency
            );

            razorpayRequest.put(
                    "receipt",
                    receipt
            );

            JSONObject notes =
                    new JSONObject();

            notes.put(
                    "merchantOrderId",
                    request.merchantOrderId()
            );

            notes.put(
                    "customerId",
                    request.customerId()
            );

            razorpayRequest.put(
                    "notes",
                    notes
            );

            Order razorpayOrder =
                    razorpayClient
                            .orders
                            .create(
                                    razorpayRequest
                            );

            String razorpayOrderId =
                    razorpayOrder.get("id");

            String razorpayStatus =
                    razorpayOrder.get("status");

            Integer attempts =
                    razorpayOrder.get("attempts");

            PaymentOrder paymentOrder =
                    new PaymentOrder();

            paymentOrder.setMerchantOrderId(
                    request.merchantOrderId()
            );

            paymentOrder.setCustomerId(
                    request.customerId()
            );

            paymentOrder.setAmount(
                    request.amount()
            );

            paymentOrder.setCurrency(
                    currency
            );

            paymentOrder.setReceipt(
                    receipt
            );

            paymentOrder.setRazorpayOrderId(
                    razorpayOrderId
            );

            paymentOrder.setStatus(
                    PaymentOrderStatus.valueOf(
                            razorpayStatus
                                    .toUpperCase(
                                            Locale.ROOT
                                    )
                    )
            );

            paymentOrder.setAttempts(
                    attempts
            );

            PaymentOrder saved =
                    paymentOrderRepository
                            .save(paymentOrder);

            return map(saved);

        } catch (RazorpayException e) {

            e.printStackTrace();

            throw new PaymentGatewayException(
                    "Unable to create Razorpay order: "
                            + e.getMessage()
            );
        }
    }

    public PaymentOrderResponse
    getByMerchantOrderId(
            String merchantOrderId
    ) {

        PaymentOrder order =
                paymentOrderRepository
                        .findByMerchantOrderId(
                                merchantOrderId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Payment order not found"
                                        )
                        );

        return map(order);
    }

    private String generateReceipt() {

        return "rcpt_"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24);
    }

    private PaymentOrderResponse map(
            PaymentOrder order
    ) {

        return new PaymentOrderResponse(

                order.getId(),

                order.getMerchantOrderId(),

                order.getRazorpayOrderId(),

                order.getCustomerId(),

                order.getAmount(),

                order.getCurrency(),

                order.getReceipt(),

                order.getStatus(),

                order.getAttempts(),

                order.getCreatedAt()
        );
    }
}