# MaatriCare Local - Project Todo

## Project Goal

Build a pregnancy support and care-coordination application using React, Spring Boot, and MySQL or PostgreSQL. The application provides organization, reminders, educational information, symptom journaling, and healthcare communication support. It must not diagnose conditions or replace professional medical advice.
 
 ok
## Phase 1: Frontend Foundation

- [x] Create React TypeScript frontend with Vite
- [x] Install frontend dependencies
- [x] Create responsive MaatriCare dashboard layout
- [x] Add pregnancy progress card
- [x] Add current pregnancy week and trimester display
- [x] Add estimated due date display
- [x] Add daily care checklist
- [x] Add interactive checklist completion state
- [x] Add upcoming appointment card
- [x] Add symptom logging entry point
- [x] Add weekly pregnancy guide card
- [x] Add language selector UI
- [x] Add navigation for overview, journal, and resources
- [x] Add medical information disclaimer
- [x] Validate the frontend production build
- [x] Split dashboard into reusable React components
- [x] Add modern responsive visual styling and interaction states

## Phase 2: Spring Boot Backend

- [x] Create Spring Boot backend project
- [x] Configure Maven build
- [x] Add Spring Web dependency
- [x] Add Spring Data JPA dependency
- [x] Create User entity
- [x] Create PregnancyProfile entity
- [x] Create Appointment entity
- [ ] Create SymptomEntry entity
- [x] Create CareTask entity
- [ ] Create WeeklyGuide entity
- [ ] Create UserLanguagePreference entity
- [x] Add entity relationships and constraints
- [x] Add database migrations with Flyway
- [ ] Add indexes for user and date-based queries
- [ ] Add seed data for local development

## Phase 4: Authentication and Profiles

- [x] Create user registration endpoint
- [x] Create login endpoint
- [x] Generate and validate JWT access tokens
- [x] Add password hashing
- [x] Add authenticated route protection
- [ ] Add logout or token invalidation strategy
- [x] Add user profile retrieval endpoint
- [x] Add profile update endpoint
- [x] Add pregnancy profile creation flow
- [x] Store last menstrual period or due date securely
- [x] Calculate current pregnancy week
- [x] Calculate trimester and weeks remaining
- [ ] Add account deletion capability

## Phase 5: Pregnancy Tracking

- [ ] Connect dashboard to real pregnancy profile data
- [x] Display current pregnancy week from backend data
- [ ] Display progress percentage
- [ ] Display estimated due date
- [ ] Add pregnancy milestone tracking
- [x] Add daily care task creation
- [x] Add daily care task completion endpoint
- [x] Add task history
- [ ] Add hydration tracking
- [ ] Add prenatal vitamin tracking
- [ ] Add activity or walking tracking
- [ ] Add weekly pregnancy update view

## Phase 6: Symptoms and Journal

- [ ] Create symptom entry form
- [ ] Add common symptom options
- [ ] Allow free-text journal notes
- [ ] Add symptom severity
- [ ] Add symptom date and time
- [ ] Add symptom history list
- [ ] Add symptom filtering by date
- [ ] Add edit and delete symptom entries
- [ ] Add private journal access controls
- [ ] Display a reminder to discuss concerning symptoms with a healthcare professional
- [ ] Ensure the app does not provide diagnosis or treatment recommendations

## Phase 7: Appointments and Reminders

 [x] Create appointment form
 [x] Add appointment date and time
 [x] Add provider name
 [x] Add clinic name and location
 [x] Add appointment notes
 [x] Add appointment list view
 [x] Add daily care task creation
 [x] Add daily care task completion endpoint
 [x] Handle timezone correctly

## Phase 8: AI Weekly Support

- [ ] Decide between a free AI API and a local AI model
- [ ] Define the AI request and response format
- [ ] Create weekly pregnancy summary endpoint
- [ ] Generate summaries from user-approved tracking data
- [ ] Create doctor appointment question preparation endpoint
- [ ] Add clear AI-generated content labeling
- [ ] Add safety instructions and medical disclaimer to AI responses
- [ ] Prevent diagnosis, treatment, or emergency medical advice
- [ ] Avoid sending unnecessary personal health information to external AI services
- [ ] Add rate limiting and usage limits
- [ ] Add fallback behavior when the AI service is unavailable
- [ ] Test AI responses for unsafe or misleading content

## Phase 9: Multilingual Support

- [ ] Select supported languages for the first release
- [ ] Add frontend internationalization framework
- [ ] Translate navigation and interface text
- [ ] Translate checklist and reminder labels
- [ ] Translate disclaimers and safety messaging
- [ ] Add backend language preference support
- [ ] Add localized date and time formatting
- [ ] Decide whether AI responses should follow the selected language
- [ ] Test layout with longer translated text

## Phase 10: Security and Privacy

- [ ] Store secrets only in environment variables
- [ ] Configure CORS for the frontend origin
- [ ] Validate and sanitize all user input
- [ ] Add authorization checks for every user-owned resource
- [ ] Prevent users from accessing another user's health data
- [ ] Add secure password policy
- [ ] Configure HTTPS for deployed environments
- [ ] Add security headers
- [ ] Avoid logging sensitive health information
- [ ] Add audit logging for important account actions
- [ ] Document data retention and deletion behavior
- [ ] Review applicable health-data privacy requirements

## Phase 11: Testing and Validation

- [ ] Add backend unit tests
- [ ] Add controller/API integration tests
- [ ] Add repository and database tests
- [ ] Add JWT authentication tests
- [ ] Add authorization tests
- [ ] Add frontend component tests
- [ ] Add checklist interaction tests
- [ ] Add symptom form validation tests
- [ ] Add appointment workflow tests
- [ ] Add responsive mobile tests
- [ ] Add end-to-end tests for registration and login
- [ ] Add end-to-end tests for pregnancy profile creation
- [ ] Add end-to-end tests for tracking and reminders
- [ ] Add end-to-end tests for AI question preparation
- [ ] Run accessibility checks
- [ ] Run dependency vulnerability scans
- [ ] Run production frontend build
- [ ] Run backend build and test suite

## Phase 12: Deployment

- [ ] Create production frontend environment configuration
- [ ] Create backend production configuration
- [ ] Create Dockerfile for the frontend
- [ ] Create Dockerfile for the backend
- [ ] Create Docker Compose setup for local development
- [ ] Configure managed PostgreSQL or MySQL
- [ ] Configure deployment platform
- [ ] Configure frontend hosting
- [ ] Configure backend hosting
- [ ] Configure database backups
- [ ] Configure health checks
- [ ] Configure application logging
- [ ] Configure monitoring and error reporting
- [ ] Configure CI/CD pipeline
- [ ] Add staging environment
- [ ] Perform production smoke test
- [ ] Document deployment and rollback steps

## Release Checklist

- [ ] Authentication works securely
- [ ] User data is isolated correctly
- [ ] Pregnancy calculations are tested
- [ ] Appointments and reminders work
- [ ] Symptom journaling works
- [ ] AI output includes safety boundaries
- [ ] Supported languages work
- [ ] Mobile layout is usable
- [ ] Accessibility review is complete
- [ ] Security review is complete
- [ ] Privacy documentation is complete
- [ ] Frontend and backend production builds pass
- [ ] Backup and monitoring are configured
- [ ] Medical disclaimer is visible throughout relevant workflows
