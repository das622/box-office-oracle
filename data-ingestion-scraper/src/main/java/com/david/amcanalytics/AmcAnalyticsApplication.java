package com.david.amcanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// We removed the exclusions! The app is now fully allowed to connect to Postgres.
@SpringBootApplication
@EnableScheduling
public class AmcAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmcAnalyticsApplication.class, args);
	}

}