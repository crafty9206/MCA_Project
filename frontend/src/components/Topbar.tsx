import { useState } from 'react'
import { Languages, Menu, PanelLeftClose, PanelLeftOpen, X } from 'lucide-react'
import type { Appointment, CareTask, PregnancySummary } from '../api'
import { NotificationCenter } from './NotificationCenter'
import { EmergencyContacts } from './EmergencyContacts'
import { Sidebar } from './Sidebar'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type TopbarProps = {
  sidebarCollapsed: boolean
  onToggleSidebar: () => void
  activeNav: string
  onNavigate: (item: string) => void
  onLogout: () => void
  onDeleteAccount: () => void
  onOpenSettings: () => void
  displayName: string
  appointments: Appointment[]
  appointmentRemindersEnabled: boolean
  browserNotificationsEnabled: boolean
  pushNotificationsEnabled: boolean
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

export function Topbar({ sidebarCollapsed, onToggleSidebar, activeNav, onNavigate, onLogout, onDeleteAccount, onOpenSettings, displayName, appointments, appointmentRemindersEnabled, browserNotificationsEnabled, pushNotificationsEnabled, tasks, dailyCareRemindersEnabled, dailyCareReminderTime, pregnancy, weeklyPregnancyRemindersEnabled, weeklyPregnancyReminderDay, weeklyPregnancyReminderTime, taskHistory, missedTaskRemindersEnabled }: TopbarProps) {
  const [menuOpen, setMenuOpen] = useState(false)
  const [translateOpen, setTranslateOpen] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const language: LanguageCode = 'en'
  const initials = displayName.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase()

  return (
    <>
    <header className="topbar">
      <button className="sidebar-toggle desktop-sidebar-toggle" type="button" aria-label={sidebarCollapsed ? 'Expand navigation' : 'Collapse navigation'} title={sidebarCollapsed ? 'Expand navigation' : 'Collapse navigation'} aria-expanded={!sidebarCollapsed} aria-controls="desktop-module-sidebar" onClick={onToggleSidebar}>{sidebarCollapsed ? <PanelLeftOpen /> : <PanelLeftClose />}</button>
      <button className="sidebar-toggle mobile-sidebar-toggle" type="button" aria-label="Open navigation" title="Open navigation" aria-expanded={mobileOpen} aria-controls="mobile-module-sidebar" onClick={() => setMobileOpen(true)}><Menu /></button>
      <a className="brand" href="#overview" aria-label="MaatriCare home" onClick={() => onNavigate('Overview')}><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></a>
      <div className="top-actions">
        <EmergencyContacts />
        <div className="browser-translate">
          <button className="browser-translate-button" type="button" aria-label="Translate this page with Chrome" aria-expanded={translateOpen} onClick={() => setTranslateOpen((open) => !open)}><Languages /><span>Translate</span></button>
          {translateOpen && <section className="browser-translate-popover" role="dialog" aria-label="Chrome translation help"><button className="browser-translate-close" type="button" aria-label="Close" onClick={() => setTranslateOpen(false)}><X /></button><strong>Use Chrome Translate</strong><p><b>Desktop:</b> Right-click anywhere on this page and choose <b>Translate to…</b>.</p><p><b>Mobile:</b> Open Chrome’s menu and tap <b>Translate</b>.</p><small>Chrome remembers your preferred language and translates the full page.</small></section>}
        </div>
        <NotificationCenter language={language} appointments={appointments} appointmentRemindersEnabled={appointmentRemindersEnabled} browserNotificationsEnabled={browserNotificationsEnabled} pushNotificationsEnabled={pushNotificationsEnabled} tasks={tasks} dailyCareRemindersEnabled={dailyCareRemindersEnabled} dailyCareReminderTime={dailyCareReminderTime} pregnancy={pregnancy} weeklyPregnancyRemindersEnabled={weeklyPregnancyRemindersEnabled} weeklyPregnancyReminderDay={weeklyPregnancyReminderDay} weeklyPregnancyReminderTime={weeklyPregnancyReminderTime} taskHistory={taskHistory} missedTaskRemindersEnabled={missedTaskRemindersEnabled} />
        <div className="profile-menu">
          <button className="avatar" type="button" aria-label="Open profile menu" aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>{initials}</button>
          {menuOpen && <div className="profile-menu-popover"><strong>{displayName}</strong><button type="button" onClick={() => { setMenuOpen(false); onOpenSettings() }}>{translate(language, 'settings')}</button><button type="button" onClick={onLogout}>{translate(language, 'signOut')}</button><button className="delete-account-action" type="button" onClick={onDeleteAccount}>{translate(language, 'deleteAccount')}</button></div>}
        </div>
      </div>
    </header>
    <Sidebar activeNav={activeNav} onNavigate={onNavigate} mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} />
    </>
  )
}
