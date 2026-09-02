package com.example.razorpay_recover_ai.service;

import com.example.razorpay_recover_ai.dto.*;
import com.example.razorpay_recover_ai.entity.Payment;
import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.enums.*;

import com.example.razorpay_recover_ai.serviceInterface.BuildathonSimulationService;
import com.example.razorpay_recover_ai.serviceInterface.HybridRecoveryDecisionService;
import com.example.razorpay_recover_ai.serviceInterface.RuleBasedRecoveryDecisionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class BuildathonSimulationServiceImpl
        implements BuildathonSimulationService {


    private final HybridRecoveryDecisionService
            hybridRecoveryDecisionService;


    private final RuleBasedRecoveryDecisionService
            ruleBasedRecoveryDecisionService;


    public BuildathonSimulationServiceImpl(

            HybridRecoveryDecisionService
                    hybridRecoveryDecisionService,

            RuleBasedRecoveryDecisionService
                    ruleBasedRecoveryDecisionService
    ) {

        this.hybridRecoveryDecisionService =
                hybridRecoveryDecisionService;

        this.ruleBasedRecoveryDecisionService =
                ruleBasedRecoveryDecisionService;
    }



    @Override
    public BuildathonSimulationResponse run(
            BuildathonSimulationRequest request
    ) {


        int size =
                request.size() == null
                        ? 100
                        : request.size();


        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Simulation size must be between 1 and 100"
            );
        }


        long seed =
                request.seed() == null
                        ? 42L
                        : request.seed();


        SimulationMode mode =
                request.mode() == null
                        ? SimulationMode.FAST_SIMULATION
                        : request.mode();


        /*
         * LIVE_AI is deliberately capped.
         *
         * We don't want a dashboard button
         * accidentally sending 100 Gemini requests.
         */
        if (
                mode == SimulationMode.LIVE_AI
                &&
                size > 20
        ) {

            throw new IllegalArgumentException(
                    "LIVE_AI simulation is limited to 20 cases"
            );
        }


        Random random =
                new Random(seed);


        long revenueAtRisk = 0;

        long revenueRecovered = 0;


        int aiDecisions = 0;

        int ruleFallbacks = 0;

        int humanReviews = 0;

        int retriesStopped = 0;

        int recoveredCases = 0;


        List<SimulationCaseResult> samples =
                new ArrayList<>();



        for (
                int i = 1;
                i <= size;
                i++
        ) {


            SyntheticFailure scenario =
                    generateScenario(
                            random
                    );


            revenueAtRisk +=
                    scenario.amount();


            Payment payment =
                    buildPayment(
                            scenario
                    );


            RecoveryCase recoveryCase =
                    buildRecoveryCase(
                            scenario
                    );



            RecoveryDecision decision;

            AnalysisSource source;



            if (
                    mode ==
                    SimulationMode.LIVE_AI
            ) {


                HybridRecoveryDecisionResult
                        result =
                        hybridRecoveryDecisionService
                                .decideWithSource(
                                        payment,
                                        recoveryCase
                                );


                decision =
                        result.decision();


                source =
                        result.source();


            } else {


                /*
                 * FAST_SIMULATION:
                 *
                 * We use the deterministic rule engine.
                 *
                 * This is intentionally NOT labelled
                 * as a Gemini decision.
                 */
                decision =
                        ruleBasedRecoveryDecisionService
                                .decide(
                                        payment,
                                        recoveryCase
                                );


                source =
                        AnalysisSource.RULE_FALLBACK;
            }



            if (
                    source ==
                    AnalysisSource.AI
            ) {

                aiDecisions++;

            } else {

                ruleFallbacks++;
            }



            if (
                    decision.action() ==
                    RecoveryAction.HUMAN_REVIEW
            ) {

                humanReviews++;
            }



            boolean retryStopped =
                    scenario.previousAttempts()
                    >= 3;


            if (retryStopped) {

                retriesStopped++;
            }



            boolean recovered =
                    simulateRecoveryOutcome(

                            decision.action(),

                            scenario.previousAttempts(),

                            random
                    );



            if (recovered) {

                recoveredCases++;

                revenueRecovered +=
                        scenario.amount();
            }



            /*
             * Return only first 10 cases.
             *
             * Dashboard does not need all 100
             * individual objects.
             */
            if (samples.size() < 10) {

                samples.add(

                        new SimulationCaseResult(

                                i,

                                scenario.amount(),

                                scenario.paymentMethod(),

                                scenario.failureReason(),

                                scenario.previousAttempts(),

                                decision.action(),

                                decision.confidence(),

                                source,

                                recovered,

                                retryStopped
                        )
                );
            }
        }



        double recoveryRate =

                revenueAtRisk == 0

                        ? 0.0

                        : (
                                (double)
                                revenueRecovered
                                /
                                revenueAtRisk
                          )
                          * 100.0;



        return new BuildathonSimulationResponse(

                size,

                seed,

                mode,

                revenueAtRisk,

                revenueRecovered,

                Math.round(
                        recoveryRate
                        * 100.0
                )
                / 100.0,

                aiDecisions,

                ruleFallbacks,

                humanReviews,

                retriesStopped,

                recoveredCases,

                samples
        );
    }



    // ========================================================
    // SYNTHETIC PAYMENT GENERATION
    // ========================================================

    private SyntheticFailure generateScenario(
            Random random
    ) {


        long[] amounts = {

                19900,

                49900,

                99900,

                149900,

                249900,

                499900
        };


        int type =
                random.nextInt(6);


        long amount =
                amounts[
                        random.nextInt(
                                amounts.length
                        )
                ];


        int attempts =
                random.nextInt(4);



        return switch (type) {


            case 0 ->
                    new SyntheticFailure(

                            amount,

                            PaymentMethod.UPI,

                            "payment_timeout",

                            "Bank response timed out",

                            attempts
                    );


            case 1 ->
                    new SyntheticFailure(

                            amount,

                            PaymentMethod.CARD,

                            "card_declined",

                            "Card was declined by issuer",

                            attempts
                    );


            case 2 ->
                    new SyntheticFailure(

                            amount,

                            PaymentMethod.UPI,

                            "insufficient_funds",

                            "Customer has insufficient funds",

                            attempts
                    );


            case 3 ->
                    new SyntheticFailure(

                            amount,

                            PaymentMethod.NETBANKING,

                            "gateway_error",

                            "Temporary payment gateway error",

                            attempts
                    );


            case 4 ->
                    new SyntheticFailure(

                            amount,

                            PaymentMethod.CARD,

                            "authentication_failed",

                            "Customer authentication failed",

                            attempts
                    );


            default ->
                    new SyntheticFailure(

                            amount,

                            PaymentMethod.UPI,

                            "network_error",

                            "Temporary network connection failure",

                            attempts
                    );
        };
    }



    // ========================================================
    // BUILD TRANSIENT PAYMENT
    // ========================================================

    private Payment buildPayment(
            SyntheticFailure scenario
    ) {


        Payment payment =
                new Payment();


        payment.setAmount(
                scenario.amount()
        );


        payment.setCurrency(
                "INR"
        );


        payment.setPaymentMethod(
                scenario.paymentMethod()
        );


        payment.setStatus(
                PaymentStatus.FAILED
        );


        payment.setFailureReason(
                scenario.failureReason()
        );


        payment.setFailureDescription(
                scenario.failureDescription()
        );


        return payment;
    }



    // ========================================================
    // BUILD TRANSIENT RECOVERY CASE
    // ========================================================

    private RecoveryCase buildRecoveryCase(
            SyntheticFailure scenario
    ) {


        RecoveryCase recoveryCase =
                new RecoveryCase();


        recoveryCase.setAttemptCount(
                scenario.previousAttempts()
        );


        recoveryCase.setStatus(
                RecoveryStatus.PENDING_ANALYSIS
        );


        return recoveryCase;
    }



    // ========================================================
    // SIMULATED OUTCOME
    // ========================================================

    private boolean simulateRecoveryOutcome(

            RecoveryAction action,

            int previousAttempts,

            Random random
    ) {


        /*
         * These are SYNTHETIC DEMO probabilities.
         *
         * They are not Razorpay benchmarks
         * or real production recovery rates.
         */


        double probability =
                switch (action) {


                    case RETRY_NOW ->
                            0.68;


                    case RETRY_LATER ->
                            0.58;


                    case ALTERNATIVE_PAYMENT_METHOD ->
                            0.72;


                    case HUMAN_REVIEW ->
                            0.30;


                    case NO_ACTION ->
                            0.0;
                };


        /*
         * Each previous failure lowers
         * the probability slightly.
         */

        probability -=
                previousAttempts
                * 0.08;


        probability =
                Math.max(
                        0.0,
                        probability
                );


        return random.nextDouble()
               < probability;
    }



    // ========================================================
    // INTERNAL SYNTHETIC OBJECT
    // ========================================================

    private record SyntheticFailure(

            long amount,

            PaymentMethod paymentMethod,

            String failureReason,

            String failureDescription,

            int previousAttempts

    ) {
    }
}