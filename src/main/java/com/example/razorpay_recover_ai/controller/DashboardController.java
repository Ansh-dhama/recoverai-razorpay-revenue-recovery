package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.DashboardSummaryResponse;

import com.example.razorpay_recover_ai.serviceInterface.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final   DashboardService dashboardService;


    public DashboardController(
            DashboardService dashboardService
    ) {

        this.dashboardService =
                dashboardService;
    }


    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse>
    getSummary() {

        return ResponseEntity.ok(
                dashboardService.getSummary()
        );
    }
}