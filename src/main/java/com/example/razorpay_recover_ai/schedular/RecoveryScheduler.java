package com.example.razorpay_recover_ai.schedular;

import com.example.razorpay_recover_ai.entity.RecoveryCase;
import com.example.razorpay_recover_ai.serviceInterface.RecoverySchedulerService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecoveryScheduler {

    private final RecoverySchedulerService
            recoverySchedulerService;


    public RecoveryScheduler(
            RecoverySchedulerService recoverySchedulerService
    ) {

        this.recoverySchedulerService =
                recoverySchedulerService;
    }


    @Scheduled(fixedDelay = 60000)
    public void processScheduledRecoveries() {

        List<RecoveryCase> processed =
                recoverySchedulerService
                        .processDueRetries();


        if (!processed.isEmpty()) {

            System.out.println(
                    "Processed scheduled recovery cases: "
                            + processed.size()
            );
        }
    }
}