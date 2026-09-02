package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.HybridRecoveryDecisionResult;
import com.example.razorpay_recover_ai.dto.RecoveryDecision;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.AnalysisSource;
import com.example.razorpay_recover_ai.serviceInterface.AiRecoveryAnalysisService;
import com.example.razorpay_recover_ai.serviceInterface.HybridRecoveryDecisionService;
import com.example.razorpay_recover_ai.serviceInterface.RuleBasedRecoveryDecisionService;
import org.springframework.stereotype.Service;

@Service
public class HybridRecoveryDecisionServiceImpl
        implements HybridRecoveryDecisionService {

    private final AiRecoveryAnalysisService aiRecoveryAnalysisService;

    private final AiRecoveryDecisionValidator validator;

    private final RuleBasedRecoveryDecisionService
            ruleBasedRecoveryDecisionService;


    public HybridRecoveryDecisionServiceImpl(
            AiRecoveryAnalysisService aiRecoveryAnalysisService,
            AiRecoveryDecisionValidator validator,
            RuleBasedRecoveryDecisionService ruleBasedRecoveryDecisionService
    ) {

        this.aiRecoveryAnalysisService =
                aiRecoveryAnalysisService;

        this.validator =
                validator;

        this.ruleBasedRecoveryDecisionService =
                ruleBasedRecoveryDecisionService;
    }


    @Override
    public RecoveryDecision decide(
            Payment payment,
            RecoveryCase recoveryCase
    ) {

        return decideWithSource(
                payment,
                recoveryCase
        ).decision();
    }


    @Override
    public HybridRecoveryDecisionResult decideWithSource(
            Payment payment,
            RecoveryCase recoveryCase
    ) {

        try {

            var aiDecision =
                    aiRecoveryAnalysisService.analyze(
                            payment,
                            recoveryCase
                    );


            validator.validate(
                    aiDecision
            );


            RecoveryDecision decision =
                    new RecoveryDecision(
                            aiDecision.action(),
                            aiDecision.confidence(),
                            aiDecision.reason()
                    );


            return new HybridRecoveryDecisionResult(
                    decision,
                    AnalysisSource.AI
            );


        } catch (Exception exception) {

            RecoveryDecision fallback =
                    ruleBasedRecoveryDecisionService.decide(
                            payment,
                            recoveryCase
                    );


            return new HybridRecoveryDecisionResult(
                    fallback,
                    AnalysisSource.RULE_FALLBACK
            );
        }
    }
}