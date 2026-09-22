const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

export type PregnancySummary = { lastMenstrualPeriod: string; dueDate: string; currentWeek: number; trimester: string; weeksRemaining: number; ageYears: number; heightCm?: number; prePregnancyWeightKg?: number; bloodPressure?: string; bloodGroup?: string }
export type DashboardProfile = { email: string; displayName: string; preferredLanguage: string; appointmentRemindersEnabled: boolean; browserNotificationsEnabled: boolean; dailyCareRemindersEnabled: boolean; dailyCareReminderTime: string; weeklyPregnancyRemindersEnabled: boolean; weeklyPregnancyReminderDay: number; weeklyPregnancyReminderTime: string; missedTaskRemindersEnabled: boolean; pregnancy: PregnancySummary | null }
export type AuthResponse = { token: string; email: string; displayName: string; role: 'USER' | 'ADMIN' }
export type SignupPayload = { displayName: string; email: string; password: string }
export type PregnancyPayload = { lastMenstrualPeriod: string; ageYears: number; heightCm?: number; prePregnancyWeightKg?: number; bloodPressure?: string; bloodGroup?: string }
export type Appointment = { id: string; title: string; startsAt: string; endsAt?: string; providerName?: string; clinicName?: string; notes?: string; reminderMinutesBefore?: number }
export type AppointmentQuestion = { id: string; appointmentId: string; question: string; answered: boolean }
export type SymptomEntry = { id: string; symptom: string; severity: 'MILD' | 'MODERATE' | 'SEVERE'; occurredAt: string; notes?: string }
export type CareTask = { id: string; taskDetailId: string; title: string; taskDate: string; completed: boolean; shared: boolean }
export type PregnancyMilestone = { id: string; title: string; description?: string; weekNumber: number; type: 'STANDARD' | 'CUSTOM'; completed: boolean }
export type DailyWellbeing = { date: string; waterGlasses: number; waterGoal: number; prenatalVitaminTaken: boolean; activityMinutes: number; activityGoal: number; mood?: string; sleepHours: number; sleepGoal: number }

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } })
  if (!response.ok) throw new Error((await response.text()) || 'Something went wrong. Please try again.')
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const register = (payload: SignupPayload) => request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(payload) })
export const login = (email: string, password: string) => request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
export const forgotPassword = (email: string) => request<{ message: string; resetToken?: string }>('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) })
export const resetPassword = (token: string, password: string) => request<void>('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, password }) })
export const logout = (token: string) => request<void>('/auth/logout', { method: 'POST' }, token)
export const deleteAccount = (token: string) => request<void>('/auth/account', { method: 'DELETE' }, token)
export const getProfile = (token: string) => request<DashboardProfile>('/profile', {}, token)
export const updateProfile = (token: string, payload: { displayName: string; appointmentRemindersEnabled: boolean; browserNotificationsEnabled: boolean; dailyCareRemindersEnabled: boolean; dailyCareReminderTime: string; weeklyPregnancyRemindersEnabled: boolean; weeklyPregnancyReminderDay: number; weeklyPregnancyReminderTime: string; missedTaskRemindersEnabled: boolean }) => request<DashboardProfile>('/profile', { method: 'PATCH', body: JSON.stringify(payload) }, token)
export const createPregnancyProfile = (token: string, payload: PregnancyPayload) => request<DashboardProfile>('/profile/pregnancy', { method: 'POST', body: JSON.stringify(payload) }, token)
export const updatePregnancyProfile = (token: string, payload: PregnancyPayload) => request<DashboardProfile>('/profile/pregnancy', { method: 'PATCH', body: JSON.stringify(payload) }, token)
export const getAppointments = (token: string) => request<Appointment[]>('/appointments', {}, token)
export const createAppointment = (token: string, payload: { title: string; startsAt: string; endsAt?: string; providerName?: string; clinicName?: string; notes?: string; reminderMinutesBefore?: number }) => request<Appointment>('/appointments', { method: 'POST', body: JSON.stringify(payload) }, token)
export const updateAppointment = (token: string, id: string, payload: { title: string; startsAt: string; endsAt?: string; providerName?: string; clinicName?: string; notes?: string; reminderMinutesBefore?: number }) => request<Appointment>(`/appointments/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }, token)
export const deleteAppointment = (token: string, id: string) => request<void>(`/appointments/${id}`, { method: 'DELETE' }, token)
export const getAppointmentQuestions = (token: string, appointmentId: string) => request<AppointmentQuestion[]>(`/appointments/${appointmentId}/questions`, {}, token)
export const createAppointmentQuestion = (token: string, appointmentId: string, question: string) => request<AppointmentQuestion>(`/appointments/${appointmentId}/questions`, { method: 'POST', body: JSON.stringify({ question }) }, token)
export const updateAppointmentQuestion = (token: string, id: string, question: string, answered: boolean) => request<AppointmentQuestion>(`/appointment-questions/${id}`, { method: 'PATCH', body: JSON.stringify({ question, answered }) }, token)
export const deleteAppointmentQuestion = (token: string, id: string) => request<void>(`/appointment-questions/${id}`, { method: 'DELETE' }, token)
export const getTasks = (token: string, date: string) => request<CareTask[]>(`/tasks?date=${date}`, {}, token)
export const getSymptoms = (token: string) => request<SymptomEntry[]>('/symptoms', {}, token)
export const createSymptom = (token: string, payload: { symptom: string; severity: SymptomEntry['severity']; occurredAt: string; notes?: string }) => request<SymptomEntry>('/symptoms', { method: 'POST', body: JSON.stringify(payload) }, token)
export const updateSymptom = (token: string, id: string, payload: { symptom: string; severity: SymptomEntry['severity']; occurredAt: string; notes?: string }) => request<SymptomEntry>(`/symptoms/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }, token)
export const deleteSymptom = (token: string, id: string) => request<void>(`/symptoms/${id}`, { method: 'DELETE' }, token)
export const getTaskHistory = (token: string, from: string, to: string) => request<CareTask[]>(`/tasks?from=${from}&to=${to}`, {}, token)
export const getMilestones = (token: string) => request<PregnancyMilestone[]>('/pregnancy-milestones', {}, token)
export const completeMilestone = (token: string, id: string, completed: boolean) => request<PregnancyMilestone>(`/pregnancy-milestones/${id}/complete`, { method: 'PATCH', body: JSON.stringify({ completed }) }, token)
export const completeTask = (token: string, id: string, completed: boolean) => request<CareTask>(`/tasks/${id}/complete`, { method: 'PATCH', body: JSON.stringify({ completed }) }, token)
export const createTask = (token: string, title: string, taskDate: string, recurrence: 'NONE' | 'DAILY' | 'WEEKLY') => request<CareTask>('/tasks', { method: 'POST', body: JSON.stringify({ title, taskDate, recurrence }) }, token)
export const updateTask = (token: string, id: string, title: string) => request<CareTask>(`/tasks/${id}`, { method: 'PATCH', body: JSON.stringify({ title }) }, token)
export const deleteTask = (token: string, id: string) => request<void>(`/tasks/${id}`, { method: 'DELETE' }, token)
export const getWellbeing = (token: string, date: string) => request<DailyWellbeing>(`/wellbeing?date=${date}`, {}, token)
export const updateWater = (token: string, date: string, glasses: number) => request<DailyWellbeing>('/wellbeing/water', { method: 'PATCH', body: JSON.stringify({ date, glasses }) }, token)
export const updatePrenatalVitamin = (token: string, date: string, taken: boolean) => request<DailyWellbeing>('/wellbeing/prenatal-vitamin', { method: 'PATCH', body: JSON.stringify({ date, taken }) }, token)
export const updateActivity = (token: string, date: string, minutes: number) => request<DailyWellbeing>('/wellbeing/activity', { method: 'PATCH', body: JSON.stringify({ date, minutes }) }, token)
export const updateMood = (token: string, date: string, mood: string) => request<DailyWellbeing>('/wellbeing/mood', { method: 'PATCH', body: JSON.stringify({ date, mood }) }, token)
export const updateSleep = (token: string, date: string, hours: number) => request<DailyWellbeing>('/wellbeing/sleep', { method: 'PATCH', body: JSON.stringify({ date, hours }) }, token)
export type AdminUser = { email: string; displayName: string; role: string }
export const getAdminUsers = (token: string) => request<AdminUser[]>('/admin/users', {}, token)
export const createAdminTask = (token: string, payload: { title: string; taskDate: string }) => request('/admin/tasks', { method: 'POST', body: JSON.stringify(payload) }, token)
export const createAdminAppointment = (token: string, payload: { userEmail: string; title: string; startsAt: string; providerName?: string; clinicName?: string; notes?: string; reminderMinutesBefore?: number }) => request('/admin/appointments', { method: 'POST', body: JSON.stringify(payload) }, token)
