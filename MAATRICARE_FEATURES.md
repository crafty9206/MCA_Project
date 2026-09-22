# MaatriCare Feature Roadmap

This document tracks potential product features separately from the implementation TODO list.

Legend:
- [ ] Not started
- [~] In progress or partially implemented
- [x] Completed

## Current Foundation

- [x] React + TypeScript + Vite frontend
- [x] Spring Boot REST backend
- [x] MySQL local development profile
- [x] PostgreSQL default database configuration
- [x] User registration and login
- [x] JWT authentication
- [x] Pregnancy profile creation
- [x] Pregnancy week and trimester calculation
- [x] Pregnancy profile update endpoint
- [x] Dashboard landing page
- [x] Public marketing landing page
- [x] Appointment creation and listing APIs
- [x] Appointment update and deletion APIs
- [x] Care task creation and completion APIs
- [x] Care task history by date range
- [x] Medical support disclaimer

## Priority 1: Core Product

### Dashboard Data

- [x] Connect appointment card to the backend
- [x] Connect care checklist to backend tasks
- [x] Persist checklist completion
- [x] Store shared task definitions separately from user completion records
- [x] Assign admin-created daily tasks to all users
- [x] Assign active shared tasks to users who register later
- [x] Load task history into the dashboard
- [x] Display live pregnancy profile data everywhere
- [x] Display real estimated due date
- [x] Add dashboard loading and empty states
- [x] Add dashboard error states

### Account Management

- [x] Add logout action
- [x] Clear session data on logout
- [x] Add account deletion endpoint
- [x] Add account deletion confirmation
- [x] Add profile settings page
- [x] Allow display-name updates
- [x] Allow pregnancy profile updates in the frontend
- [x] Add forgot-password flow
- [x] Add password reset flow
- [x] Add password strength feedback

### Appointment Experience

- [x] Add appointment creation form in the frontend
- [x] Add appointment edit form
- [x] Add appointment deletion confirmation
- [x] Add appointment empty state
- [x] Add appointment validation messages
- [x] Add calendar view
- [x] Add provider and clinic contact details
- [x] Add appointment preparation questions
- [x] Add appointment reminder settings

## Priority 2: Tracking and Wellbeing

### Daily Care

- [x] Track water intake
- [x] Track prenatal vitamins
- [x] Track walking or activity
- [x] Track sleep
- [x] Track mood
- [x] Add daily completion percentage
- [x] Add weekly care summary
- [x] Add custom care tasks
- [x] Add task edit and deletion
- [x] Add recurring tasks

### Symptoms and Journal

- [x] Create SymptomEntry entity
- [x] Create symptom database migration
- [x] Create symptom repository
- [x] Create symptom API
- [x] Create symptom entry form
- [x] Add common symptom options
- [x] Add symptom severity
- [x] Add symptom date and time
- [x] Add free-text journal notes
- [x] Add symptom and journal history
- [x] Add date filtering
- [x] Add symptom editing
- [x] Add symptom deletion
- [x] Add private journal access controls
- [x] Add concerning-symptom safety reminder
- [x] Confirm that symptom features do not diagnose conditions

### Pregnancy Milestones

- [x] Add pregnancy milestone model
- [x] Add milestone database migration
- [x] Add standard pregnancy milestone timeline
- [x] Add custom milestones
- [x] Add milestone completion state
- [x] Add trimester milestones

## Priority 3: Education and Communication

### Weekly Pregnancy Guide

- [ ] Decide whether guide content is static or admin-managed
- [ ] Create WeeklyGuide entity
- [ ] Create weekly-guide migration
- [ ] Connect guide to current pregnancy week
- [ ] Add nutrition guidance
- [ ] Add safe activity guidance
- [ ] Add common body-change information
- [ ] Add questions to discuss with a healthcare professional
- [ ] Add content versioning
- [ ] Add content review date

### Resource Library

- [ ] Add resource categories
- [ ] Add searchable resources
- [ ] Add nutrition resources
- [ ] Add exercise resources
- [ ] Add mental wellbeing resources
- [ ] Add postpartum resources
- [ ] Add newborn preparation resources
- [ ] Add hospital and emergency resources
- [ ] Add medically reviewed content labels

### Healthcare Sharing

- [ ] Export pregnancy summary
- [ ] Export appointment history
- [ ] Export symptom and journal history
- [ ] Export tracking summaries
- [ ] Generate PDF report
- [ ] Add secure time-limited sharing link
- [ ] Allow users to revoke shared access
- [ ] Record sharing activity in audit logs

## Priority 4: Notifications and Personalization

### Notifications

- [x] Add notification preferences
- [x] Add in-app notification center
- [x] Add appointment reminders
- [x] Add daily care reminders
- [x] Add weekly pregnancy reminders
- [x] Add missed-task reminders
- [x] Add browser notifications
- [ ] Add email notifications
- [ ] Add SMS notifications only after provider and privacy review

### Language and Regional Support

- [ ] Select initial supported languages
- [ ] Add frontend internationalization
- [ ] Translate navigation
- [ ] Translate checklist labels
- [ ] Translate disclaimers
- [ ] Add backend language preference update endpoint
- [ ] Add localized date formatting
- [ ] Add localized time formatting
- [ ] Test longer translated text on mobile
- [ ] Decide whether educational and AI content follows language preference

## Priority 5: Security and Privacy

- [x] Add token logout or invalidation strategy
- [ ] Add account deletion capability
- [ ] Add authorization tests for every user-owned resource
- [ ] Test cross-user appointment access
- [ ] Test cross-user task access
- [ ] Test cross-user profile access
- [ ] Externalize production CORS origins
- [ ] Store secrets only in environment variables
- [ ] Add secure password policy
- [ ] Add login rate limiting
- [ ] Add failed-login protection
- [ ] Add security headers
- [ ] Avoid sensitive health data in logs
- [ ] Add audit logging
- [ ] Document data retention
- [ ] Document data deletion behavior
- [ ] Review applicable health-data privacy requirements
- [ ] Configure HTTPS for deployed environments

## Priority 6: AI Support

AI features require a separate safety and privacy decision before implementation.

- [ ] Choose a local model or external provider
- [ ] Define approved AI use cases
- [ ] Define request and response formats
- [ ] Create weekly summary endpoint
- [ ] Create appointment-question preparation endpoint
- [ ] Use only user-approved data
- [ ] Minimize health data sent externally
- [ ] Add AI-generated content labeling
- [ ] Include medical disclaimers in AI responses
- [ ] Prevent diagnosis and treatment recommendations
- [ ] Prevent emergency-care advice from replacing emergency services
- [ ] Add rate limits
- [ ] Add usage limits
- [ ] Add unsafe-output testing
- [ ] Add provider outage fallback
- [ ] Add prompt and response audit strategy without storing unnecessary health data

## Priority 7: Testing and Quality

### Backend

- [ ] Add controller unit tests
- [ ] Add service unit tests
- [ ] Add repository tests
- [ ] Add MySQL integration tests
- [ ] Add JWT tests
- [ ] Add logout tests
- [ ] Add authorization tests
- [ ] Add validation tests
- [ ] Add migration tests

### Frontend

- [ ] Add component tests
- [ ] Test sign-up flow
- [ ] Test sign-in flow
- [ ] Test landing-page actions
- [ ] Test refresh-state persistence
- [ ] Test pregnancy profile form
- [ ] Test checklist interaction
- [ ] Test appointment workflows
- [ ] Test symptom form validation
- [ ] Test mobile layout
- [ ] Add accessibility checks
- [ ] Add end-to-end registration test
- [ ] Add end-to-end dashboard test

### Quality Gates

- [ ] Run frontend production build
- [ ] Run backend clean test
- [ ] Run dependency vulnerability scan
- [ ] Run accessibility audit
- [ ] Review sensitive data handling
- [ ] Review error messages
- [ ] Review medical disclaimers

## Priority 8: Deployment and Operations

- [ ] Add frontend Dockerfile if deployment requires it
- [ ] Add backend Dockerfile
- [ ] Configure production frontend environment
- [ ] Configure production backend environment
- [ ] Configure managed database
- [ ] Add CI pipeline
- [ ] Add deployment pipeline
- [ ] Add staging environment
- [ ] Add database backups
- [ ] Add health checks
- [ ] Add structured application logging
- [ ] Add monitoring
- [ ] Add error reporting
- [ ] Document deployment steps
- [ ] Document rollback steps
- [ ] Perform production smoke test

## Suggested Implementation Order

1. Connect the dashboard to real backend data.
2. Add logout, account settings, and account deletion.
3. Complete appointment management in the frontend.
4. Add symptom and journal tracking with privacy controls.
5. Add daily-care metrics and notifications.
6. Add medically reviewed weekly guides.
7. Add multilingual support.
8. Complete security, privacy, and testing work.
9. Decide whether AI support is appropriate and implement it only with safety controls.
10. Prepare deployment and operations.

## Product Safety Rules

- MaatriCare organizes information and supports communication with healthcare professionals.
- MaatriCare must not diagnose medical conditions.
- MaatriCare must not replace professional medical advice.
- MaatriCare must not replace emergency services.
- Educational and AI content should be medically reviewed where appropriate.
- Users should control what personal health information is stored or shared.
