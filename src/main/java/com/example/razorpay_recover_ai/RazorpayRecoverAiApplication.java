package com.example.razorpay_recover_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RazorpayRecoverAiApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				RazorpayRecoverAiApplication.class,
				args
		);
	}
}