# Review 2 Presentation Narration and Recording Guide

**Target duration:** 5-7 minutes; this script is paced for about 6 minutes.  
**Presenter:** Deepak Kumar | **Register No.:** 9224116032  
**Project:** MaatriCare - Pregnancy Support and Care Coordination

Speak naturally; this is a guide, not something to read mechanically. Keep the camera on throughout the recording.

## Before You Start

1. Open the final deck and start the slide show on slide 1.
2. Sign in to the running MaatriCare application using a demo account with synthetic data.
3. Close unrelated tabs and hide notifications. Do not show real health records, credentials, or tokens.
4. Test microphone volume, camera framing, and screen readability.

## Script

### 0:00-0:25 | Slides 1-2: Welcome and Title

Good morning. I am Deepak Kumar, Register Number 9224116032, from the Master of Computer Applications programme, Semester IV, Batch January 2024. My faculty advisor is Mr. M. Govindaraj. My project is MaatriCare, a pregnancy support and care-coordination application.

### 0:25-1:00 | Slides 3-4: Abstract and Introduction

Pregnancy involves appointments, daily routines, personal records, and conversations with healthcare professionals. When these details are spread across different tools, it can be difficult to stay organized and prepare for a visit. MaatriCare brings the main organization activities into one authenticated workspace. Its purpose is to support record keeping and communication; it does not diagnose or replace professional or emergency care.

### 1:00-2:10 | Slides 5-9: System Design, Requirements, and Modules

The system design includes an ER model and UML use-case and class views. The user interacts with the React and TypeScript frontend, which communicates with a Spring Boot REST API. The backend uses JWT authentication, validation, and user-ownership checks. Data is stored in a relational database. MySQL is used for local development, with PostgreSQL configuration and H2-backed tests.

The main modules are pregnancy profile and milestones, daily care, appointments and visit questions, private journal, guides and resources, reports and sharing, emergency contacts, and reminders.

### 2:10-3:40 | Slides 10-12: Module Walkthrough and Live Demo

Daily Care includes a capacity-aware focus planner and wellbeing controls. Appointments supports upcoming, past, and all visits, search, saved questions, calendar export, and a visit brief. The Journal records private symptoms and notes, with date, severity, and text filters. Guides and resources provide general education.

I will now demonstrate these workflows in the running application using synthetic data. First, I will show the Daily Care planner and one tracking control. Next, I will open Appointments and show the visit list and question preparation. Finally, I will open the Journal and show its private-entry filters. MaatriCare does not diagnose symptoms or recommend treatment.

### 3:40-4:40 | Slides 13-16: Input/Output, Code, and Data

The sample shows an appointment reminder configured one day before the visit and scheduled in Asia/Kolkata time. The result is a generic browser notification, and snoozing moves delivery by one hour. The following slides show appointment-history UI code, scheduled Spring Boot dispatch code, and synthetic database fields. These code and data examples are representative; they contain no real user data.

### 4:40-5:20 | Slide 17: Testing

Four focused backend tests passed. They cover the Delhi timezone default and validation, local-time scheduling and reminder preferences, repeated-run deduplication, one-time snooze actions, and cleanup of old reminder records. The frontend production build also passes. Broader accessibility, security, database integration, and live push tests remain future work. Live push requires server VAPID keys and HTTPS.

### 5:20-6:00 | Slides 18-20: Conclusion, References, and Thanks

MaatriCare provides an authenticated workspace for pregnancy records, care routines, appointments, private journaling, educational resources, reports, and reminders. Next steps are to configure and test live push, replace the illustrative interface panels with screenshots from the application, expand testing, and complete the institutional final report. Thank you to my guide, the CDOE, and the review committee. Thank you, and I welcome your questions.

## Recording Checklist

- The deck contains 20 slides, within the required 15-20 slide range.
- Replace illustrative UI panels on slides 10-12 with clear screenshots from the running application.
- Demonstrate the live app for about 1-2 minutes using synthetic data only.
- Keep the camera on throughout the 5-7 minute recording.
- Save as `R2_Jan2024_MCA_9224116032_Deepak_Kumar.mp4` (adjust the batch label if the LMS specifies another format).
- Check audio, camera, slide readability, and video duration before uploading.
