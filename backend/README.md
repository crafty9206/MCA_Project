# MaatriCare Backend

Spring Boot backend for the MaatriCare pregnancy support application.

## Requirements

- Java 25
- Maven 3.9+
- PostgreSQL 14+

## Run locally

Create a PostgreSQL database named `maatricare`, then run:

```powershell
mvn spring-boot:run
```

The health endpoint is available at `http://localhost:8080/api/health`.

## Scheduled Push Reminders

Push delivery is opt-in per account and uses the existing appointment, daily-care, missed-task, and weekly-pregnancy reminder preferences. Reminder times use `Asia/Kolkata` by default. The backend reconciles schedules every 30 seconds, retries transient delivery failures, and keeps reminder text generic on device lock screens.

Generate a VAPID key pair with `npx --yes web-push generate-vapid-keys`, then provide the values through environment variables; do not commit the private key:

```powershell
$env:VAPID_PUBLIC_KEY = "<public-key>"
$env:VAPID_PRIVATE_KEY = "<private-key>"
$env:VAPID_SUBJECT = "mailto:your-operations-email@example.com"
$env:NOTIFICATION_API_BASE_URL = "http://localhost:8080/api"
$env:NOTIFICATION_SCHEDULER_ENABLED = "true"
```

Browsers require a secure context for service workers and push (localhost is allowed for development; deployed sites must use HTTPS). In production, set `NOTIFICATION_API_BASE_URL` to the publicly reachable backend URL and configure `CORS_ALLOWED_ORIGINS` for the frontend origin. Push remains unavailable until both VAPID keys are configured. Each registered browser/device can be removed from Profile Settings by turning push reminders off.

## Current scope

This initial backend provides the application foundation, database configuration, actuator health checks, and a health API. Authentication, profiles, pregnancy tracking, appointments, journaling, reminders, and AI support will be added in later phases.
