package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.SimulationMode;

import java.util.List;

public record BuildathonSimulationResponse(

        int batchSize,

        long seed,

        SimulationMode mode,

        long revenueAtRisk,

        long revenueRecovered,

        double recoveryRate,

        int aiDecisions,

        int ruleFallbacks,

        int humanReviews,

        int retriesStopped,

        int recoveredCases,

        List<SimulationCaseResult> sampleCases

) {
}