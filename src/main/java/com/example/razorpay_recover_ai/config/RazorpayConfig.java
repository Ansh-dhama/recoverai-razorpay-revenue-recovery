package com.example.razorpay_recover_ai.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RazorpayProperties.class)
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient(
            RazorpayProperties properties
    ) throws RazorpayException {

        return new RazorpayClient(
                properties.keyId(),
                properties.keySecret()
        );
    }
}