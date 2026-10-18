package com.maatricare.notifications;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notifications.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PushReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PushReminderScheduler.class);
    private final PushReminderSchedulingService schedulingService;
    private final PushNotificationService pushService;

    public PushReminderScheduler(PushReminderSchedulingService schedulingService, PushNotificationService pushService) {
        this.schedulingService = schedulingService;
        this.pushService = pushService;
    }

    @Scheduled(fixedDelayString = "${notifications.scheduler.interval-ms:30000}")
    public void dispatchDueReminders() {
        if (!pushService.isConfigured()) return;
        try {
            for (UUID reminderId : schedulingService.synchronizeAndFindDue()) {
                PushReminderSchedulingService.Delivery delivery = schedulingService.claim(reminderId);
                if (delivery == null) continue;
                boolean sent = pushService.sendToUser(delivery.userId(), delivery.key(), delivery.title(), delivery.body(), delivery.snoozeToken());
                schedulingService.finish(reminderId, sent);
            }
        } catch (Exception exception) {
            logger.error("Scheduled push reminder processing failed.", exception);
        }
    }
}