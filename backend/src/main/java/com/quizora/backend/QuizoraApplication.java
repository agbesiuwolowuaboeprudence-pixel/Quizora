package com.quizora.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // daily sweep expires subscriptions that have passed their end date
public class QuizoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizoraApplication.class, args);
    }
}
