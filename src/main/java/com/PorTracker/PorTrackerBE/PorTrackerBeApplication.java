package com.PorTracker.PorTrackerBE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PorTrackerBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PorTrackerBeApplication.class, args);
    }
}
