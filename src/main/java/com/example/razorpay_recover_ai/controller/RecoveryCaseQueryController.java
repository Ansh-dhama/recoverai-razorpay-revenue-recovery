package com.example.razorpay_recover_ai.controller;

import com.example.razorpay_recover_ai.dto.RecoveryCaseListResponse;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.repository.RecoveryCaseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recovery-cases")
public class RecoveryCaseQueryController {

    private final RecoveryCaseRepository repository;


    public RecoveryCaseQueryController(
            RecoveryCaseRepository repository
    ) {

        this.repository =
                repository;
    }


    @GetMapping
    public Page<RecoveryCaseListResponse> getCases(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return repository
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


    private RecoveryCaseListResponse map(
            RecoveryCase recoveryCase
    ) {

        return new RecoveryCaseListResponse(

                recoveryCase.getId(),

                recoveryCase
                        .getPayment()
                        .getId(),

                recoveryCase.getStatus(),

                recoveryCase.getRecommendedAction(),

                recoveryCase.getConfidence(),

                recoveryCase.getReason(),

                recoveryCase.getAttemptCount(),

                recoveryCase.getNextRetryAt(),

                recoveryCase.getLastAttemptAt()
        );
    }
}