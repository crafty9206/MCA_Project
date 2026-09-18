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

## Current scope

This initial backend provides the application foundation, database configuration, actuator health checks, and a health API. Authentication, profiles, pregnancy tracking, appointments, journaling, reminders, and AI support will be added in later phases.
