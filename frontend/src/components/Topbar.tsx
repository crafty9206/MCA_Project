import { useState } from 'react'
import type { Appointment, CareTask, PregnancySummary } from '../api'
import { NotificationCenter } from './NotificationCenter'

type TopbarProps = {
  language: string
  onLanguageChange: (language: string) => void
  activeNav: string
  onNavigate: (item: string) => void
  onLogout: () => void
  onDeleteAccount: () => void
  onOpenSettings: () => void
  displayName: string
  appointments: Appointment[]
  appointmentRemindersEnabled: boolean
  browserNotificationsEnabled: boolean
  tasks: CareTask[]
  dailyCareRemindersEnabled: boolean
  dailyCareReminderTime: string
  pregnancy: PregnancySummary | null
  weeklyPregnancyRemindersEnabled: boolean
  weeklyPregnancyReminderDay: number
  weeklyPregnancyReminderTime: string
  taskHistory: CareTask[]
  missedTaskRemindersEnabled: boolean
}

const navigation = ['Overview', 'My journal', 'Resources']

export function Topbar({ language, onLanguageChange, activeNav, onNavigate, onLogout, onDeleteAccount, onOpenSettings, displayName, appointments, appointmentRemindersEnabled, browserNotificationsEnabled, tasks, dailyCareRemindersEnabled, dailyCareReminderTime, pregnancy, weeklyPregnancyRemindersEnabled, weeklyPregnancyReminderDay, weeklyPregnancyReminderTime, taskHistory, missedTaskRemindersEnabled }: TopbarProps) {
  const [menuOpen, setMenuOpen] = useState(false)
  const initials = displayName.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase()
  return (
    <header className="topbar">
      <a className="brand" href="#overview" aria-label="MaatriCare home"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></a>
      <nav className="nav-links" aria-label="Main navigation">{navigation.map((item) => <a className={activeNav === item ? 'active' : ''} href={item === 'Overview' ? '#overview' : item === 'My journal' ? '#journal' : '#resources'} key={item} onClick={() => onNavigate(item)}>{item}</a>)}</nav>
      <div className="top-actions"><label className="language-select"><span className="globe">◎</span><span className="sr-only">Language</span><select value={language} onChange={(event) => onLanguageChange(event.target.value)}><option>English</option><option>Hindi</option><option>Tamil</option></select></label><NotificationCenter appointments={appointments} appointmentRemindersEnabled={appointmentRemindersEnabled} browserNotificationsEnabled={browserNotificationsEnabled} tasks={tasks} dailyCareRemindersEnabled={dailyCareRemindersEnabled} dailyCareReminderTime={dailyCareReminderTime} pregnancy={pregnancy} weeklyPregnancyRemindersEnabled={weeklyPregnancyRemindersEnabled} weeklyPregnancyReminderDay={weeklyPregnancyReminderDay} weeklyPregnancyReminderTime={weeklyPregnancyReminderTime} taskHistory={taskHistory} missedTaskRemindersEnabled={missedTaskRemindersEnabled} /><div className="profile-menu"><button className="avatar" type="button" aria-label="Open profile menu" aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>{initials}</button>{menuOpen && <div className="profile-menu-popover"><strong>{displayName}</strong><button type="button" onClick={() => { setMenuOpen(false); onOpenSettings() }}>Profile settings</button><button type="button" onClick={onLogout}>Sign out</button><button className="delete-account-action" type="button" onClick={onDeleteAccount}>Delete account</button></div>}</div></div>
    </header>
  )
}
