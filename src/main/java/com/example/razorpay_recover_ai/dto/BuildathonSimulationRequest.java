package com.example.razorpay_recover_ai.dto;

import com.example.razorpay_recover_ai.enums.SimulationMode;

public record BuildathonSimulationRequest(

        Integer size,

        Long seed,

        SimulationMode mode

) {
}