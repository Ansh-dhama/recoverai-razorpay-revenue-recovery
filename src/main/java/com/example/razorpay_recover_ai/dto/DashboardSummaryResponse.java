package com.example.razorpay_recover_ai.dto;

public record DashboardSummaryResponse(

        long totalOrders,

        long totalPayments,

        long failedPayments,

        long totalRecoveryCases,

        long pendingAnalysis,

        long recoveryPlanned,

        long scheduledRecoveries,

        long recoveredCases,

        long humanReviewCases

) {
}