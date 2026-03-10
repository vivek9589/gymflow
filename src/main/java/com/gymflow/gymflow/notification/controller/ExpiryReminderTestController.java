package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.notification.scheduler.ExpiryReminderScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/test")
public class ExpiryReminderTestController {

    private final ExpiryReminderScheduler scheduler;

    public ExpiryReminderTestController(ExpiryReminderScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @GetMapping("/trigger-expiry-reminder")
    public void trigger() {
        scheduler.sendExpiryReminders();
    }

}
