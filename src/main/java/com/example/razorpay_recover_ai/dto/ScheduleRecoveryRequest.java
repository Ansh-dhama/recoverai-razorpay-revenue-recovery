package com.example.razorpay_recover_ai.dto;

import java.time.LocalDateTime;

public record ScheduleRecoveryRequest(

        LocalDateTime retryAt

) {
}