package com.maatricare.notifications;

import java.util.UUID;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.Urgency;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final String publicKey;
    private final String apiBaseUrl;
    private final nl.martijndwars.webpush.PushService pushService;

    public PushNotificationService(PushSubscriptionRepository subscriptionRepository, ObjectMapper objectMapper,
            @Value("${notifications.web-push.vapid-public-key:}") String publicKey,
            @Value("${notifications.web-push.vapid-private-key:}") String privateKey,
            @Value("${notifications.web-push.subject:mailto:support@maatricare.local}") String subject,
            @Value("${notifications.web-push.api-base-url:http://localhost:8080/api}") String apiBaseUrl) {
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
        this.publicKey = publicKey;
        this.apiBaseUrl = apiBaseUrl.replaceAll("/+$", "");
        nl.martijndwars.webpush.PushService configuredService = null;
        if (!publicKey.isBlank() && !privateKey.isBlank()) {
            try {
                configuredService = new nl.martijndwars.webpush.PushService(publicKey, privateKey, subject);
            } catch (Exception exception) {
                logger.error("Web Push keys are invalid; server-side push is disabled.");
            }
        }
        this.pushService = configuredService;
    }

    public boolean isConfigured() { return pushService != null; }
    public String getPublicKey() { return isConfigured() ? publicKey : ""; }

    public boolean send(PushSubscription subscription, String reminderKey, String title, String body, String snoozeToken) {
        if (!isConfigured()) return false;
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "body", body,
                    "key", reminderKey,
                    "snoozeToken", snoozeToken,
                    "snoozeUrl", apiBaseUrl + "/push/snooze-action"));
            var keys = new nl.martijndwars.webpush.Subscription.Keys(
                    subscription.getP256dhKey(), subscription.getAuthKey());
            var browserSubscription = new nl.martijndwars.webpush.Subscription(subscription.getEndpoint(), keys);
            var notification = new Notification(browserSubscription, payload, Urgency.NORMAL);
            var response = pushService.send(notification, Encoding.AES128GCM);
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consumeQuietly(response.getEntity());
            if (status == 404 || status == 410) subscriptionRepository.delete(subscription);
            if (status < 200 || status >= 300) logger.warn("Web Push delivery returned HTTP status {}.", status);
            return status >= 200 && status < 300;
        } catch (Exception exception) {
            logger.warn("Web Push delivery failed for a configured subscription.");
            return false;
        }
    }

    public boolean sendToUser(UUID userId, String reminderKey, String title, String body, String snoozeToken) {
        var subscriptions = subscriptionRepository.findByUserId(userId);
        if (subscriptions.isEmpty()) return false;
        boolean allDelivered = true;
        for (PushSubscription subscription : subscriptions) {
            allDelivered &= send(subscription, reminderKey, title, body, snoozeToken);
        }
        return allDelivered;
    }
}