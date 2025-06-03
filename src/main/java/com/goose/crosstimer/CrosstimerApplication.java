package com.goose.crosstimer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrosstimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrosstimerApplication.class, args);
    }

}
