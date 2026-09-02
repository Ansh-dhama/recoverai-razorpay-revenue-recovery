package com.example.razorpay_recover_ai.serviceInterface;

import com.example.razorpay_recover_ai.dto.BuildathonSimulationRequest;
import com.example.razorpay_recover_ai.dto.BuildathonSimulationResponse;

public interface BuildathonSimulationService {

    BuildathonSimulationResponse run(
            BuildathonSimulationRequest request
    );
}