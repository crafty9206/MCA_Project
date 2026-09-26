import { useEffect, useState } from 'react'
import { AppointmentCard } from './components/AppointmentCard'
import { AuthFlow } from './components/AuthFlow'
import { LandingPage } from './components/LandingPage'
import { AdminPanel } from './components/AdminPanel'
import { ProfileSettings } from './components/ProfileSettings'
import { completeTask, deleteAccount, getAppointments, getMilestones, getProfile, getSymptoms, getTaskHistory, getTasks, getWeeklyGuide, getWellbeing, logout, updateActivity, updateDayPlan, updateMood, updatePrenatalVitamin, updateSleep, updateWater } from './api'
import type { Appointment, CareTask, DailyWellbeing, DashboardProfile, PregnancyMilestone, SymptomEntry, WeeklyGuideData } from './api'
import { CareChecklist } from './components/CareChecklist'
import { ExerciseCard } from './components/ExerciseCard'
import { HydrationCard } from './components/HydrationCard'
import { PrenatalVitaminCard } from './components/PrenatalVitaminCard'
import { ActivityCard } from './components/ActivityCard'
import { SleepCard } from './components/SleepCard'
import { DailyCompletionCard } from './components/DailyCompletionCard'
import { WeeklyCareSummary } from './components/WeeklyCareSummary'
import { JournalCard } from './components/JournalCard'
import { PregnancyStatus } from './components/PregnancyStatus'
import { PregnancyMilestoneCard } from './components/PregnancyMilestoneCard'
import { Topbar } from './components/Topbar'
import { WeeklyGuide } from './components/WeeklyGuide'
import { WelcomeSection } from './components/WelcomeSection'
import { ResourceLibraryCard } from './components/ResourceLibraryCard'
import { AiAssistantCard } from './components/AiAssistantCard'
import { HealthcareReport } from './components/HealthcareReport'
import { SharedReportPage } from './components/SharedReportPage'
import { LabReportGuide } from './components/LabReportGuide'
import { GentleDayPlanner } from './components/GentleDayPlanner'
import { translate } from './i18n'
import type { LanguageCode } from './i18n'
import './App.css'

function App() {
  const sharedReportToken = new URLSearchParams(window.location.search).get('share')
  const [profile, setProfile] = useState<DashboardProfile | null>(null)
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [tasks, setTasks] = useState<CareTask[]>([])
  const [taskHistory, setTaskHistory] = useState<CareTask[]>([])
  const [symptoms, setSymptoms] = useState<SymptomEntry[]>([])
  const [milestones, setMilestones] = useState<PregnancyMilestone[]>([])
  const [wellbeing, setWellbeing] = useState<DailyWellbeing | null>(null)
  const [weeklyGuide, setWeeklyGuide] = useState<WeeklyGuideData | null>(null)
  const [savingWater, setSavingWater] = useState(false)
  const [dashboardError, setDashboardError] = useState('')
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false)
  const [showProfileSettings, setShowProfileSettings] = useState(false)
  const [deletingAccount, setDeletingAccount] = useState(false)
  const [isAdmin, setIsAdmin] = useState(false)
  const [loading, setLoading] = useState(true)
  const language: LanguageCode = 'en'
  const [activeNav, setActiveNav] = useState('Overview')
  const [authScreen, setAuthScreen] = useState<'landing' | 'signup' | 'signin'>(() => {
    const savedScreen = sessionStorage.getItem('maatricare-auth-screen')
    return savedScreen === 'signup' || savedScreen === 'signin' ? savedScreen : 'landing'
  })

  const openAuthScreen = (screen: 'signup' | 'signin') => {
    sessionStorage.setItem('maatricare-auth-screen', screen)
    setAuthScreen(screen)
  }

  const handleLogout = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (token) {
      try { await logout(token) } catch { /* Clear local access even if the server is unavailable. */ }
    }
    sessionStorage.removeItem('maatricare-token')
    setIsAdmin(false)
    setProfile(null)
    setAuthScreen('landing')
  }

  const handleDeleteAccount = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setDeletingAccount(true)
    try {
      await deleteAccount(token)
      sessionStorage.clear()
      setProfile(null)
      setAppointments([])
      setTasks([])
      setTaskHistory([])
      setAuthScreen('landing')
    } catch {
      setDashboardError('Your account could not be deleted. Please try again.')
    } finally {
      setDeletingAccount(false)
      setShowDeleteConfirmation(false)
    }
  }

  const toggleTask = async (id: string, completed: boolean) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setTasks((currentTasks) => currentTasks.map((task) => task.id === id ? { ...task, completed } : task))
    try {
      await completeTask(token, id, completed)
    } catch {
      setTasks((currentTasks) => currentTasks.map((task) => task.id === id ? { ...task, completed: !completed } : task))
      setDashboardError('Your task update could not be saved.')
    }
  }

  const addCustomTask = (task: CareTask) => setTasks((current) => [...current, task])
  const updateCustomTask = (task: CareTask) => setTasks((current) => current.map((item) => item.id === task.id ? task : item))
  const deleteCustomTask = (id: string) => setTasks((current) => current.filter((task) => task.id !== id))

  const addAppointment = (appointment: Appointment) => {
    setAppointments((currentAppointments) => [...currentAppointments, appointment].sort((first, second) => first.startsAt.localeCompare(second.startsAt)))
  }

  const updateAppointmentInList = (appointment: Appointment) => {
    setAppointments((currentAppointments) => currentAppointments.map((item) => item.id === appointment.id ? appointment : item).sort((first, second) => first.startsAt.localeCompare(second.startsAt)))
  }

  const deleteAppointmentFromList = (id: string) => {
    setAppointments((currentAppointments) => currentAppointments.filter((appointment) => appointment.id !== id))
  }

  const changeWater = async (glasses: number) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !wellbeing) return
    const previous = wellbeing
    setWellbeing({ ...wellbeing, waterGlasses: glasses })
    setSavingWater(true)
    try { setWellbeing(await updateWater(token, wellbeing.date, glasses)) }
    catch { setWellbeing(previous); setDashboardError('Your water intake could not be saved.') }
    finally { setSavingWater(false) }
  }

  const changeVitamin = async (taken: boolean) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !wellbeing) return
    const previous = wellbeing
    setWellbeing({ ...wellbeing, prenatalVitaminTaken: taken })
    try { setWellbeing(await updatePrenatalVitamin(token, wellbeing.date, taken)) }
    catch { setWellbeing(previous); setDashboardError('Your vitamin status could not be saved.') }
  }

  const changeActivity = async (minutes: number) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !wellbeing) return
    const previous = wellbeing
    setWellbeing({ ...wellbeing, activityMinutes: minutes })
    try { setWellbeing(await updateActivity(token, wellbeing.date, minutes)) }
    catch { setWellbeing(previous); setDashboardError('Your activity could not be saved.') }
  }

  const changeMood = async (mood: string) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return

    let currentWellbeing = wellbeing
    if (!currentWellbeing) {
      const today = new Date().toISOString().slice(0, 10)
      try {
        currentWellbeing = await getWellbeing(token, today)
        setWellbeing(currentWellbeing)
      } catch {
        setDashboardError('Your wellbeing check-in could not be loaded.')
        return
      }
    }

    const previous = currentWellbeing
    setWellbeing({ ...currentWellbeing, mood })
    try { setWellbeing(await updateMood(token, currentWellbeing.date, mood)) }
    catch { setWellbeing(previous); setDashboardError('Your wellbeing check-in could not be saved.') }
  }

  const changeSleep = async (hours: number) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !wellbeing) return
    const previous = wellbeing
    setWellbeing({ ...wellbeing, sleepHours: hours })
    try { setWellbeing(await updateSleep(token, wellbeing.date, hours)) }
    catch { setWellbeing(previous); setDashboardError('Your sleep record could not be saved.') }
  }

  const saveDayPlan = async (energyLevel: DailyWellbeing['energyLevel'], focusTaskCount: number) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !wellbeing) return
    try { setWellbeing(await updateDayPlan(token, wellbeing.date, energyLevel, focusTaskCount)) }
    catch {
      setDashboardError('Your gentle day plan could not be saved.')
      throw new Error('Unable to save gentle day plan')
    }
  }

  const loadTrackingData = async (token: string) => {
    const todayDate = new Date()
    const historyStartDate = new Date(todayDate)
    const historyEndDate = new Date(todayDate)
    historyStartDate.setDate(historyStartDate.getDate() - 7)
    historyEndDate.setDate(historyEndDate.getDate() - 1)
    const localDate = (date: Date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    const [loadedAppointments, loadedTasks, loadedHistory, loadedWellbeing, loadedSymptoms, loadedMilestones] = await Promise.all([
      getAppointments(token),
      getTasks(token, localDate(todayDate)),
      getTaskHistory(token, localDate(historyStartDate), localDate(historyEndDate)),
      getWellbeing(token, localDate(todayDate)),
      getSymptoms(token),
      getMilestones(token),
    ])
    setAppointments(loadedAppointments)
    setTasks(loadedTasks)
    setTaskHistory(loadedHistory)
    setWellbeing(loadedWellbeing)
    setSymptoms(loadedSymptoms)
    setMilestones(loadedMilestones)
  }

  const completeAuthentication = async (loadedProfile: DashboardProfile) => {
    setProfile(loadedProfile)
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    try { await loadTrackingData(token) } catch { setDashboardError('We could not load your appointments and care tasks.') }
  }

  useEffect(() => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) { setLoading(false); return }
    getProfile(token).then(async (loadedProfile) => {
      setProfile(loadedProfile)
      try {
        await loadTrackingData(token)
      } catch {
        setDashboardError('We could not load your appointments and care tasks.')
      }
    }).catch(() => sessionStorage.removeItem('maatricare-token')).finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !profile?.pregnancy) {
      setWeeklyGuide(null)
      return
    }
    getWeeklyGuide(token).then(setWeeklyGuide).catch(() => setWeeklyGuide(null))
  }, [profile?.pregnancy])

  if (sharedReportToken) return <SharedReportPage token={sharedReportToken} />
  if (loading) return <main className="loading-page">Loading your care plan…</main>
  if (isAdmin) return <AdminPanel onLogout={handleLogout} />
  if (!profile) {
    if (authScreen === 'landing') return <LandingPage onSignUp={() => openAuthScreen('signup')} onSignIn={() => openAuthScreen('signin')} />
    return <AuthFlow initialScreen={authScreen} onComplete={completeAuthentication} onAdmin={() => setIsAdmin(true)} />
  }

  const pageHeading = (label: string, title: string, description: string) => (
    <header className="feature-page-heading">
      <p className="eyebrow">{label}</p>
      <h1>{title}</h1>
      <p>{description}</p>
    </header>
  )

  const activePage = (() => {
    switch (activeNav) {
      case 'Daily Care':
        return <div className="content feature-page" id="daily-care">
          {pageHeading(translate(language, 'page.daily.label'), translate(language, 'page.daily.title'), translate(language, 'page.daily.description'))}
          <div className="dashboard-grid">
            <GentleDayPlanner tasks={tasks} wellbeing={wellbeing} onSave={saveDayPlan} onToggle={toggleTask} />
            <CareChecklist tasks={tasks} history={taskHistory} onToggle={toggleTask} onCreated={addCustomTask} onUpdated={updateCustomTask} onDeleted={deleteCustomTask} language={language} />
            <HydrationCard wellbeing={wellbeing} onChange={changeWater} disabled={savingWater} language={language} />
            <PrenatalVitaminCard wellbeing={wellbeing} onChange={changeVitamin} language={language} />
            <ActivityCard wellbeing={wellbeing} onChange={changeActivity} language={language} />
            <SleepCard wellbeing={wellbeing} onChange={changeSleep} language={language} />
            <ExerciseCard language={language} />
          </div>
        </div>
      case 'Appointments':
        return <div className="content feature-page" id="appointments">
          {pageHeading(translate(language, 'page.appointments.label'), translate(language, 'page.appointments.title'), translate(language, 'page.appointments.description'))}
          <div className="feature-single-column"><AppointmentCard appointments={appointments} onCreated={addAppointment} onUpdated={updateAppointmentInList} onDeleted={deleteAppointmentFromList} language={language} /></div>
        </div>
      case 'Journal':
        return <div className="content feature-page" id="journal">
          {pageHeading(translate(language, 'page.journal.label'), translate(language, 'page.journal.title'), translate(language, 'page.journal.description'))}
          <div className="feature-single-column"><JournalCard selected={wellbeing?.mood ?? null} onSelect={changeMood} entries={symptoms} onCreated={(entry) => setSymptoms((current) => [entry, ...current])} onUpdated={(entry) => setSymptoms((current) => current.map((item) => item.id === entry.id ? entry : item))} onDeleted={(id) => setSymptoms((current) => current.filter((item) => item.id !== id))} language={language} /></div>
        </div>
      case 'Guides':
        return <div className="content feature-page" id="guides">
          {pageHeading(translate(language, 'page.guides.label'), translate(language, 'page.guides.title'), translate(language, 'page.guides.description'))}
          <div className="dashboard-grid">
            <PregnancyMilestoneCard milestones={milestones} language={language} onRefresh={async () => { const token = sessionStorage.getItem('maatricare-token'); if (token) setMilestones(await getMilestones(token)) }} />
            <WeeklyGuide guide={weeklyGuide} language={language} />
          </div>
        </div>
      case 'Resources':
        return <div className="content feature-page" id="resources">
          {pageHeading(translate(language, 'page.resources.label'), translate(language, 'page.resources.title'), translate(language, 'page.resources.description'))}
          <ResourceLibraryCard language={language} />
        </div>
      case 'Reports':
        return <div className="content feature-page" id="reports">
          {pageHeading(translate(language, 'page.reports.label'), translate(language, 'page.reports.title'), translate(language, 'page.reports.description'))}
          <HealthcareReport language={language} />
        </div>
      case 'AI Assistant':
        return <div className="content assistant-page" id="ai-assistant">
          <header className="assistant-page-heading">
            <p className="eyebrow">{translate(language, 'page.ai.label')}</p>
            <h1>{translate(language, 'page.ai.title')}</h1>
            <p>{translate(language, 'page.ai.description')}</p>
          </header>
          <AiAssistantCard language={language} />
        </div>
      default:
        return <div className="content feature-page" id="overview">
          <WelcomeSection name={profile.displayName} language={language} />
          {profile.pregnancy && <PregnancyStatus pregnancy={profile.pregnancy} language={language} />}
          <div className="dashboard-grid overview-summary-grid">
            <DailyCompletionCard tasks={tasks} wellbeing={wellbeing} language={language} />
            <WeeklyCareSummary history={taskHistory} todayTasks={tasks} language={language} />
          </div>
          <LabReportGuide />
        </div>
    }
  })()

  return (
    <main className="app-shell">
      <Topbar activeNav={activeNav} onNavigate={setActiveNav} onLogout={handleLogout} onDeleteAccount={() => setShowDeleteConfirmation(true)} onOpenSettings={() => setShowProfileSettings(true)} displayName={profile.displayName} appointments={appointments} appointmentRemindersEnabled={profile.appointmentRemindersEnabled} browserNotificationsEnabled={profile.browserNotificationsEnabled} tasks={tasks} dailyCareRemindersEnabled={profile.dailyCareRemindersEnabled} dailyCareReminderTime={profile.dailyCareReminderTime} pregnancy={profile.pregnancy} weeklyPregnancyRemindersEnabled={profile.weeklyPregnancyRemindersEnabled} weeklyPregnancyReminderDay={profile.weeklyPregnancyReminderDay} weeklyPregnancyReminderTime={profile.weeklyPregnancyReminderTime} taskHistory={taskHistory} missedTaskRemindersEnabled={profile.missedTaskRemindersEnabled} />
      {activePage}
      {dashboardError && <p className="dashboard-error" role="alert">{dashboardError}</p>}
      <p className="disclaimer">{translate(language, 'disclaimer')}</p>
      {showDeleteConfirmation && <div className="confirm-overlay" role="presentation"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-account-title"><p className="section-label">Permanent action</p><h2 id="delete-account-title">Delete your account?</h2><p>This permanently removes your pregnancy profile, appointments, care-task history, and account information. This cannot be undone.</p><div className="confirm-actions"><button className="outline-button" type="button" disabled={deletingAccount} onClick={() => setShowDeleteConfirmation(false)}>Cancel</button><button className="danger-button" type="button" disabled={deletingAccount} onClick={handleDeleteAccount}>{deletingAccount ? 'Deleting…' : 'Delete account'}</button></div></section></div>}
      {showProfileSettings && <ProfileSettings profile={profile} onClose={() => setShowProfileSettings(false)} onSaved={(updatedProfile) => { setProfile(updatedProfile); setShowProfileSettings(false) }} />}
    </main>
  )
}

export default App
