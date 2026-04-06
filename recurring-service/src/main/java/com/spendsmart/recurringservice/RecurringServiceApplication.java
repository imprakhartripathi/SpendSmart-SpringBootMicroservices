package com.spendsmart.recurringservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecurringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecurringServiceApplication.class, args);
    }

}
