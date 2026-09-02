package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.BuildathonSimulationRequest;
import com.example.razorpay_recover_ai.dto.BuildathonSimulationResponse;
import com.example.razorpay_recover_ai.serviceInterface.BuildathonSimulationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(
        "/api/v1/buildathon"
)
public class BuildathonSimulationController {


    private final BuildathonSimulationService
            simulationService;


    public BuildathonSimulationController(
            BuildathonSimulationService simulationService
    ) {

        this.simulationService =
                simulationService;
    }



    @PostMapping(
            "/simulate"
    )
    public ResponseEntity<BuildathonSimulationResponse>
    simulate(

            @RequestBody
            BuildathonSimulationRequest request
    ) {


        return ResponseEntity.ok(

                simulationService.run(
                        request
                )
        );
    }
}