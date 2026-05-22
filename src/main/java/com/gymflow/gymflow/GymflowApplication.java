package com.gymflow.gymflow;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class GymflowApplication {

    private static final Logger log = LoggerFactory.getLogger(GymflowApplication.class);

    @PostConstruct
    public void init() {
        // 🚀 Set JVM default timezone globally to Asia/Kolkata (IST)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        log.info("Spring Boot JVM initialized successfully in IST timezone: {}", TimeZone.getDefault().getID());
    }

    public static void main(String[] args) {
        SpringApplication.run(GymflowApplication.class, args);
    }
}