# Review 2 Narration and Recording Checklist

**Target length:** about 6 minutes (within the required 5-7 minutes)  
**Presenter:** Deepak Kumar, Register No. 9224116032  
**Project:** MaatriCare - Pregnancy Support and Care-Coordination Application

## Narration

### 0:00-0:20 | Slides 1-2
Good morning. I am Deepak Kumar, Register Number 9224116032, from the Department of Computer Applications. My project is MaatriCare, a pregnancy support and care-coordination web application. I have left semester, batch, faculty guide, and the official college logo as placeholders for completion.

### 0:20-0:50 | Slides 3-4
Pregnancy involves appointments, daily routines, personal notes, and conversations with healthcare professionals. When information is spread across different places, it can be difficult to stay organized and prepare for a visit. MaatriCare brings these organization tools into one authenticated workspace. Its purpose is to support tracking and communication, not to diagnose or replace professional or emergency care.

### 0:50-1:35 | Slides 5-10
The frontend is built with React and TypeScript. It communicates with a Spring Boot REST API protected by Spring Security and JWT authentication. JPA repositories persist user-owned records in a relational database. The design includes the browser client, application API, and database, along with user interactions and the background reminder flow. The reminder scheduler uses Asia/Kolkata as the current default timezone, stores occurrences to prevent duplicates, and retries failed deliveries. Web Push is opt-in and requires VAPID keys and HTTPS in deployment.

### 1:35-2:00 | Slides 11-12
The implementation uses Java 25, Maven, Node.js, React, Spring Boot, and MySQL for local development, with PostgreSQL configuration and H2-backed tests. The main modules are account and pregnancy profile, daily care, appointments, private journal, milestones, guides and resources, AI-supported visit preparation, reports, emergency contacts, and reminders.

### 2:00-3:30 | Slides 13-15, then live demo
I will demonstrate the application using synthetic demo data. On Overview, the user sees pregnancy progress and a summary of recorded activity. In Daily Care, the user can choose a manageable focus list and track hydration, vitamins, activity, and sleep. In Appointments, the user can review or create a visit, prepare questions, and build a visit brief. The Journal supports private notes, search, severity filtering, and date filtering. Guides, resources, and reports help organize information for conversations with a healthcare professional. The interface keeps private data under user control and does not provide diagnosis or treatment recommendations.

For the live demo, I will navigate from Overview to Daily Care, then Appointments and Journal. I will show the saved synthetic visit, the journal filter controls, and Profile Settings. I will point out that push is unavailable until VAPID keys are configured. I will not use real health data in the recording.

### 3:30-4:05 | Slides 16-18
For the sample input, a user creates an appointment and chooses a reminder interval. The backend calculates the reminder instant using the account timezone and stores a deduplicated occurrence. The output is a generic browser notification. The user can open the reminder center or snooze for one hour. The code and sample database rows on these slides are representative examples, not personal data.

### 4:05-4:45 | Slide 19
Focused tests cover the Asia/Kolkata timezone default and validation, scheduling preferences, deduplication, one-time snooze capabilities, and cleanup of old terminal reminder records. The frontend production build also passes. Live push delivery has not been demonstrated because server VAPID keys and production HTTPS are not configured yet. Broader accessibility, security, and database integration testing remain future work.

### 4:45-5:15 | Slide 20
In conclusion, MaatriCare provides a connected organizer for pregnancy-related records and care conversations. Next steps are to configure and test live push, replace illustrative mockups with clean live screenshots, complete broader verification, and prepare the final project report in the official institutional format. Thank you. I welcome your questions.

**Timing note:** The narration above is approximately five minutes at a measured pace. Use a concise live demo and brief pauses to reach the required 5-7 minute duration; do not rush.

## Recording Checklist

- Replace `[FILL]` with semester, batch, and faculty guide name.
- Add the official college logo supplied by the institution.
- The slide UI panels are illustrative mockups. Replace them with clean screenshots from the running app before final submission.
- Use only the disposable synthetic demo account; do not reveal real health records or passwords.
- Keep the camera on for the full recording and test microphone levels first.
- Record the presentation at 1080p if available; check text legibility and audio before upload.
- Confirm the video runs 5-7 minutes and includes the project demonstration.
- Suggested file name: `R2_[Batch]_MCA_9224116032_Deepak_Kumar.mp4`.
- The college logo, guide details, and batch are not present in the workspace and must be supplied before submission.
