import { useEffect, useState } from 'react'
import { Baby, Bell, CalendarClock, History, ListChecks, Clock3 } from 'lucide-react'
import type { Appointment, CareTask, PregnancySummary } from '../api'
import { snoozePushReminder } from '../api'
import { localeFor } from '../i18n'
import type { LanguageCode } from '../i18n'

type NotificationCenterProps = {
  appointments: Appointment[]
  tasks: CareTask[]
  appointmentRemindersEnabled: boolean
  browserNotificationsEnabled: boolean
  pushNotificationsEnabled: boolean
  dailyCareRemindersEnabled: boolean
  dailyCareReminderTime: string
  pregnancy: PregnancySummary | null
  weeklyPregnancyRemindersEnabled: boolean
  weeklyPregnancyReminderDay: number
  weeklyPregnancyReminderTime: string
  taskHistory: CareTask[]
  missedTaskRemindersEnabled: boolean
  language: LanguageCode
}

const reminderTimezone = 'Asia/Kolkata'
const dateKeyInReminderTimezone = (date: Date) => {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: reminderTimezone, year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(date)
  const part = (type: Intl.DateTimeFormatPartTypes) => parts.find((item) => item.type === type)?.value ?? ''
  return `${part('year')}-${part('month')}-${part('day')}`
}
const addDaysToReminderDate = (dateKey: string, days: number) => {
  const date = new Date(`${dateKey}T00:00:00Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}
const reminderDateTime = (dateKey: string, time: string) => new Date(`${dateKey}T${time.slice(0, 5)}:00+05:30`)

type ReminderItem = {
  key: string
  title: string
  detail: string
  reminderAt: Date
  due: boolean
  type: 'appointment' | 'care' | 'pregnancy' | 'missed'
}

export function NotificationCenter({ appointments, tasks, appointmentRemindersEnabled, browserNotificationsEnabled, pushNotificationsEnabled, dailyCareRemindersEnabled, dailyCareReminderTime, pregnancy, weeklyPregnancyRemindersEnabled, weeklyPregnancyReminderDay, weeklyPregnancyReminderTime, taskHistory, missedTaskRemindersEnabled, language }: NotificationCenterProps) {
  const reminderTimeFormat = new Intl.DateTimeFormat(localeFor(language), { timeZone: reminderTimezone, day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' })
  const snoozesKey = 'maatricare-reminder-snoozes'
  const [open, setOpen] = useState(false)
  const [now, setNow] = useState(() => Date.now())
  const [snoozes, setSnoozes] = useState<Record<string, number>>(() => {
    try { return JSON.parse(localStorage.getItem(snoozesKey) ?? '{}') as Record<string, number> }
    catch { return {} }
  })
  const [permission, setPermission] = useState<NotificationPermission | 'unsupported'>(() =>
    'Notification' in window ? Notification.permission : 'unsupported')
  const [pushSnoozeError, setPushSnoozeError] = useState('')

  const appointmentReminders: ReminderItem[] = (appointmentRemindersEnabled ? appointments : [])
    .filter((appointment) => appointment.reminderMinutesBefore != null && new Date(appointment.startsAt).getTime() > now)
    .map((appointment) => {
      const reminderAt = new Date(new Date(appointment.startsAt).getTime() - appointment.reminderMinutesBefore! * 60_000)
      return {
        key: `appointment:${appointment.id}:${new Date(appointment.startsAt).getTime()}:${appointment.reminderMinutesBefore}`,
        title: appointment.title,
        detail: `Appointment ${reminderTimeFormat.format(new Date(appointment.startsAt))}`,
        reminderAt,
        due: reminderAt.getTime() <= now,
        type: 'appointment' as const,
      }
    })

  const incompleteTasks = tasks.filter((task) => !task.completed)
  const today = new Date()
  const todayKey = dateKeyInReminderTimezone(today)
  const [hours, minutes] = (dailyCareReminderTime || '09:00').split(':').map(Number)
  const normalizedDailyTime = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`
  const careReminderAt = reminderDateTime(todayKey, normalizedDailyTime)
  const careReminders: ReminderItem[] = dailyCareRemindersEnabled && incompleteTasks.length > 0 ? [{
    key: `care:${todayKey}:${normalizedDailyTime}`,
    title: `${incompleteTasks.length} care ${incompleteTasks.length === 1 ? 'task' : 'tasks'} remaining`,
    detail: incompleteTasks.map((task) => task.title).join(', '),
    reminderAt: careReminderAt,
    due: careReminderAt.getTime() <= now,
    type: 'care',
  }] : []

  const utcToday = new Date(`${todayKey}T00:00:00Z`)
  const utcWeekday = utcToday.getUTCDay()
  const todayIsoDay = utcWeekday === 0 ? 7 : utcWeekday
  const normalizedWeeklyDay = weeklyPregnancyReminderDay >= 1 && weeklyPregnancyReminderDay <= 7
    ? weeklyPregnancyReminderDay : 1
  const weekStartKey = addDaysToReminderDate(todayKey, 1 - todayIsoDay)
  const [weeklyHours, weeklyMinutes] = (weeklyPregnancyReminderTime || '09:00').split(':').map(Number)
  const normalizedWeeklyTime = `${String(weeklyHours).padStart(2, '0')}:${String(weeklyMinutes).padStart(2, '0')}`
  const weeklyReminderDate = addDaysToReminderDate(weekStartKey, normalizedWeeklyDay - 1)
  const weeklyReminderAt = reminderDateTime(weeklyReminderDate, normalizedWeeklyTime)
  const weeklyReminders: ReminderItem[] = weeklyPregnancyRemindersEnabled && pregnancy ? [{
    key: `pregnancy:${weeklyReminderDate}:${normalizedWeeklyDay}:${normalizedWeeklyTime}`,
    title: `Your week ${pregnancy.currentWeek} pregnancy update`,
    detail: `${pregnancy.trimester} · ${pregnancy.weeksRemaining} weeks remaining`,
    reminderAt: weeklyReminderAt,
    due: weeklyReminderAt.getTime() <= now,
    type: 'pregnancy',
  }] : []

  const yesterdayKey = addDaysToReminderDate(todayKey, -1)
  const missedTasks = taskHistory.filter((task) => task.taskDate === yesterdayKey && !task.completed)
  const missedReminders: ReminderItem[] = missedTaskRemindersEnabled && missedTasks.length > 0 ? [{
    key: `missed:${yesterdayKey}`,
    title: `${missedTasks.length} ${missedTasks.length === 1 ? 'task was' : 'tasks were'} missed yesterday`,
    detail: missedTasks.map((task) => task.title).join(', '),
    reminderAt: reminderDateTime(todayKey, '09:00'),
    due: true,
    type: 'missed',
  }] : []

  const reminders = [...appointmentReminders, ...careReminders, ...weeklyReminders, ...missedReminders]
    .map((reminder) => {
      const snoozedUntil = snoozes[reminder.key]
      return snoozedUntil && snoozedUntil > now
        ? { ...reminder, reminderAt: new Date(snoozedUntil), due: false }
        : reminder
    })
    .sort((first, second) => first.reminderAt.getTime() - second.reminderAt.getTime())
  const dueCount = reminders.filter((reminder) => reminder.due).length

  const applyLocalSnooze = (key: string) => {
    const nextSnoozes = { ...snoozes, [key]: Date.now() + 60 * 60 * 1000 }
    localStorage.removeItem(`maatricare-notified:${key}`)
    localStorage.setItem(snoozesKey, JSON.stringify(nextSnoozes))
    setSnoozes(nextSnoozes)
    setNow(Date.now())
  }

  const snoozeReminder = (key: string) => {
    applyLocalSnooze(key)
    const token = sessionStorage.getItem('maatricare-token')
    if (pushNotificationsEnabled && token) {
      setPushSnoozeError('')
      void snoozePushReminder(token, key).catch(() => setPushSnoozeError('This reminder could not be snoozed on the server.'))
    }
  }

  useEffect(() => {
    const url = new URL(window.location.href)
    if (!url.searchParams.has('reminder')) return
    setOpen(true)
    url.searchParams.delete('reminder')
    window.history.replaceState(window.history.state, '', `${url.pathname}${url.search}${url.hash}`)
  }, [])

  useEffect(() => {
    if (!('serviceWorker' in navigator)) return
    const onServiceWorkerMessage = (event: MessageEvent<{ type?: string; key?: string }>) => {
      if (event.data?.type === 'reminder-snoozed' && event.data.key) applyLocalSnooze(event.data.key)
    }
    navigator.serviceWorker.addEventListener('message', onServiceWorkerMessage)
    return () => navigator.serviceWorker.removeEventListener('message', onServiceWorkerMessage)
  }, [snoozes])

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30_000)
    return () => window.clearInterval(timer)
  }, [])

  useEffect(() => {
    const hasExpiredSnoozes = Object.values(snoozes).some((until) => until <= now)
    if (!hasExpiredSnoozes) return
    const activeSnoozes = Object.fromEntries(Object.entries(snoozes).filter(([, until]) => until > now))
    localStorage.setItem(snoozesKey, JSON.stringify(activeSnoozes))
    setSnoozes(activeSnoozes)
  }, [now, snoozes])

  useEffect(() => {
    if (!browserNotificationsEnabled || pushNotificationsEnabled || permission !== 'granted') return
    reminders.filter((reminder) => reminder.due).forEach((reminder) => {
      const storageKey = `maatricare-notified:${reminder.key}`
      if (localStorage.getItem(storageKey)) return
      const notificationTitle = reminder.type === 'care' ? 'Today’s care reminder' : reminder.type === 'pregnancy' ? 'Weekly pregnancy update' : reminder.type === 'missed' ? 'Missed care tasks' : 'Upcoming appointment'
      new Notification(notificationTitle, {
        body: reminder.type === 'care' ? reminder.title : `${reminder.title}. ${reminder.detail}`,
        tag: reminder.key,
      })
      localStorage.setItem(storageKey, new Date().toISOString())
    })
  }, [browserNotificationsEnabled, permission, pushNotificationsEnabled, reminders])

  const requestPermission = async () => {
    if (!('Notification' in window)) return
    setPermission(await Notification.requestPermission())
    setNow(Date.now())
  }

  const remindersEnabled = appointmentRemindersEnabled || dailyCareRemindersEnabled || weeklyPregnancyRemindersEnabled || missedTaskRemindersEnabled

  return (
    <div className="notification-center">
      <button className="notification" type="button" aria-label="Notifications" aria-expanded={open} onClick={() => setOpen((visible) => !visible)}><Bell aria-hidden="true" strokeWidth={1.8} />{dueCount > 0 && <span className="notification-count">{dueCount}</span>}</button>
      {open && <section className="notification-popover" aria-label="Notification center"><div className="notification-heading"><div><p className="section-label">Reminders</p><h3>Notifications</h3></div><span>{dueCount} due</span></div>{!remindersEnabled ? <p className="notification-note">Reminders are off. Enable them in Profile settings.</p> : <>{browserNotificationsEnabled && permission === 'default' && <button className="enable-notifications" type="button" onClick={requestPermission}>Allow browser notifications</button>}{browserNotificationsEnabled && permission === 'denied' && <p className="notification-note">Browser notifications are blocked. Enable them in your browser site settings.</p>}{browserNotificationsEnabled && permission === 'unsupported' && <p className="notification-note">This browser does not support notifications.</p>}{pushSnoozeError && <p className="notification-note" role="alert">{pushSnoozeError}</p>}<div className="notification-list">{reminders.length === 0 ? <p className="notification-empty">No upcoming reminders.</p> : reminders.map((reminder) => <article className={reminder.due ? 'notification-item due' : 'notification-item'} key={reminder.key}><span className="notification-mark">{reminder.type === 'care' ? <ListChecks aria-hidden="true" /> : reminder.type === 'pregnancy' ? <Baby aria-hidden="true" /> : reminder.type === 'missed' ? <History aria-hidden="true" /> : <CalendarClock aria-hidden="true" />}</span><div><strong>{reminder.title}</strong><p>{reminder.type === 'missed' ? 'Review your care history' : reminder.due ? 'Reminder due now' : `Reminder ${reminderTimeFormat.format(reminder.reminderAt)}`}</p><small>{reminder.detail}</small>{reminder.due && <button className="snooze-reminder" type="button" onClick={() => snoozeReminder(reminder.key)}><Clock3 aria-hidden="true" /> Remind me in 1 hour</button>}</div></article>)}</div><p className="notification-footnote">{pushNotificationsEnabled ? 'Push reminders can arrive while MaatriCare is closed.' : browserNotificationsEnabled ? 'Browser alerts work while MaatriCare is open.' : 'Browser alerts are off in Profile settings.'} Snoozed reminders return in one hour.</p></>}</section>}
    </div>
  )
}
