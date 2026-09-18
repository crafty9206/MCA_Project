import { useEffect, useState } from 'react'
import { AppointmentCard } from './components/AppointmentCard'
import { AuthFlow } from './components/AuthFlow'
import { getProfile } from './api'
import type { DashboardProfile } from './api'
import { CareChecklist } from './components/CareChecklist'
import { ExerciseCard } from './components/ExerciseCard'
import { JournalCard } from './components/JournalCard'
import { PregnancyStatus } from './components/PregnancyStatus'
import { Topbar } from './components/Topbar'
import { WeeklyGuide } from './components/WeeklyGuide'
import { WelcomeSection } from './components/WelcomeSection'
import './App.css'

function App() {
  const [profile, setProfile] = useState<DashboardProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [language, setLanguage] = useState('English')
  const [completed, setCompleted] = useState([true, false, false])
  const [selectedSymptom, setSelectedSymptom] = useState<string | null>(null)
  const [activeNav, setActiveNav] = useState('Overview')

  const toggleTask = (index: number) => {
    setCompleted((tasks) => tasks.map((task, taskIndex) => taskIndex === index ? !task : task))
  }

  useEffect(() => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) { setLoading(false); return }
    getProfile(token).then(setProfile).catch(() => sessionStorage.removeItem('maatricare-token')).finally(() => setLoading(false))
  }, [])

  if (loading) return <main className="loading-page">Loading your care plan…</main>
  if (!profile) return <AuthFlow onComplete={setProfile} />

  return (
    <main className="app-shell">
      <Topbar language={language} onLanguageChange={setLanguage} activeNav={activeNav} onNavigate={setActiveNav} />
      <div className="content" id="overview">
        <WelcomeSection name={profile.displayName} />
        {profile.pregnancy && <PregnancyStatus pregnancy={profile.pregnancy} />}
        <div className="dashboard-grid">
          <CareChecklist completed={completed} onToggle={toggleTask} />
          <AppointmentCard />
          <JournalCard selected={selectedSymptom} onSelect={setSelectedSymptom} />
          <ExerciseCard />
          <WeeklyGuide />
        </div>
        <p className="disclaimer">MaatriCare provides organization and educational support. It does not provide medical diagnosis or replace advice from your healthcare professional.</p>
      </div>
    </main>
  )
}

export default App
