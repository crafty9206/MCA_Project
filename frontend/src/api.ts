const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

export type PregnancySummary = { lastMenstrualPeriod: string; dueDate: string; currentWeek: number; trimester: string; weeksRemaining: number; ageYears: number; heightCm?: number; prePregnancyWeightKg?: number; bloodPressure?: string; bloodGroup?: string }
export type DashboardProfile = { email: string; displayName: string; preferredLanguage: string; pregnancy: PregnancySummary | null }
type AuthResponse = { token: string; email: string; displayName: string }
export type SignupPayload = { displayName: string; email: string; password: string }
export type PregnancyPayload = { lastMenstrualPeriod: string; ageYears: number; heightCm?: number; prePregnancyWeightKg?: number; bloodPressure?: string; bloodGroup?: string }

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } })
  if (!response.ok) throw new Error((await response.text()) || 'Something went wrong. Please try again.')
  return response.json() as Promise<T>
}

export const register = (payload: SignupPayload) => request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(payload) })
export const login = (email: string, password: string) => request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
export const getProfile = (token: string) => request<DashboardProfile>('/profile', {}, token)
export const createPregnancyProfile = (token: string, payload: PregnancyPayload) => request<DashboardProfile>('/profile/pregnancy', { method: 'POST', body: JSON.stringify(payload) }, token)
