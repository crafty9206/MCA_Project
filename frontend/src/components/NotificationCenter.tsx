import { useEffect, useState } from 'react'
import { Baby, Bell, CalendarClock, History, ListChecks } from 'lucide-react'
import type { Appointment, CareTask, PregnancySummary } from '../api'

type NotificationCenterProps = {
  appointments: Appointment[]
  tasks: CareTask[]
  appointmentRemindersEnabled: boolean
  browserNotificationsEnabled: boolean
  dailyCareRemindersEnabled: boolean
  dailyCareReminderTime: string
  pregnancy: PregnancySummary | null
  weeklyPregnancyRemindersEnabled: boolean
  weeklyPregnancyReminderDay: number
  weeklyPregnancyReminderTime: string
  taskHistory: CareTask[]
  missedTaskRemindersEnabled: boolean
}

type ReminderItem = {
  key: string
  title: string
  detail: string
  reminderAt: Date
  due: boolean
  type: 'appointment' | 'care' | 'pregnancy' | 'missed'
}

const reminderTimeFormat = new Intl.DateTimeFormat('en-IN', {
  day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit',
})

export function NotificationCenter({ appointments, tasks, appointmentRemindersEnabled, browserNotificationsEnabled, dailyCareRemindersEnabled, dailyCareReminderTime, pregnancy, weeklyPregnancyRemindersEnabled, weeklyPregnancyReminderDay, weeklyPregnancyReminderTime, taskHistory, missedTaskRemindersEnabled }: NotificationCenterProps) {
  const [open, setOpen] = useState(false)
  const [now, setNow] = useState(() => Date.now())
  const [permission, setPermission] = useState<NotificationPermission | 'unsupported'>(() =>
    'Notification' in window ? Notification.permission : 'unsupported')

  const appointmentReminders: ReminderItem[] = (appointmentRemindersEnabled ? appointments : [])
    .filter((appointment) => appointment.reminderMinutesBefore != null && new Date(appointment.startsAt).getTime() > now)
    .map((appointment) => {
      const reminderAt = new Date(new Date(appointment.startsAt).getTime() - appointment.reminderMinutesBefore! * 60_000)
      return {
        key: `appointment:${appointment.id}:${appointment.startsAt}:${appointment.reminderMinutesBefore}`,
        title: appointment.title,
        detail: `Appointment ${reminderTimeFormat.format(new Date(appointment.startsAt))}`,
        reminderAt,
        due: reminderAt.getTime() <= now,
        type: 'appointment' as const,
      }
    })

  const incompleteTasks = tasks.filter((task) => !task.completed)
  const today = new Date()
  const [hours, minutes] = (dailyCareReminderTime || '09:00').split(':').map(Number)
  const careReminderAt = new Date(today.getFullYear(), today.getMonth(), today.getDate(), hours, minutes)
  const careReminders: ReminderItem[] = dailyCareRemindersEnabled && incompleteTasks.length > 0 ? [{
    key: `care:${today.toISOString().slice(0, 10)}:${dailyCareReminderTime}`,
    title: `${incompleteTasks.length} care ${incompleteTasks.length === 1 ? 'task' : 'tasks'} remaining`,
    detail: incompleteTasks.map((task) => task.title).join(', '),
    reminderAt: careReminderAt,
    due: careReminderAt.getTime() <= now,
    type: 'care',
  }] : []

  const weekStart = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const todayIsoDay = today.getDay() === 0 ? 7 : today.getDay()
  const normalizedWeeklyDay = weeklyPregnancyReminderDay >= 1 && weeklyPregnancyReminderDay <= 7
    ? weeklyPregnancyReminderDay : 1
  weekStart.setDate(weekStart.getDate() - todayIsoDay + 1)
  const [weeklyHours, weeklyMinutes] = (weeklyPregnancyReminderTime || '09:00').split(':').map(Number)
  const weeklyReminderAt = new Date(weekStart)
  weeklyReminderAt.setDate(weekStart.getDate() + normalizedWeeklyDay - 1)
  weeklyReminderAt.setHours(weeklyHours, weeklyMinutes, 0, 0)
  const weeklyReminders: ReminderItem[] = weeklyPregnancyRemindersEnabled && pregnancy ? [{
    key: `pregnancy:${weekStart.toISOString().slice(0, 10)}:${normalizedWeeklyDay}:${weeklyPregnancyReminderTime}`,
    title: `Your week ${pregnancy.currentWeek} pregnancy update`,
    detail: `${pregnancy.trimester} · ${pregnancy.weeksRemaining} weeks remaining`,
    reminderAt: weeklyReminderAt,
    due: weeklyReminderAt.getTime() <= now,
    type: 'pregnancy',
  }] : []

  const yesterday = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 1)
  const yesterdayKey = `${yesterday.getFullYear()}-${String(yesterday.getMonth() + 1).padStart(2, '0')}-${String(yesterday.getDate()).padStart(2, '0')}`
  const missedTasks = taskHistory.filter((task) => task.taskDate === yesterdayKey && !task.completed)
  const missedReminders: ReminderItem[] = missedTaskRemindersEnabled && missedTasks.length > 0 ? [{
    key: `missed:${yesterdayKey}`,
    title: `${missedTasks.length} ${missedTasks.length === 1 ? 'task was' : 'tasks were'} missed yesterday`,
    detail: missedTasks.map((task) => task.title).join(', '),
    reminderAt: new Date(today.getFullYear(), today.getMonth(), today.getDate()),
    due: true,
    type: 'missed',
  }] : []

  const reminders = [...appointmentReminders, ...careReminders, ...weeklyReminders, ...missedReminders]
    .sort((first, second) => first.reminderAt.getTime() - second.reminderAt.getTime())
  const dueCount = reminders.filter((reminder) => reminder.due).length

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30_000)
    return () => window.clearInterval(timer)
  }, [])

  useEffect(() => {
    if (!browserNotificationsEnabled || permission !== 'granted') return
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
  }, [browserNotificationsEnabled, permission, reminders])

  const requestPermission = async () => {
    if (!('Notification' in window)) return
    setPermission(await Notification.requestPermission())
    setNow(Date.now())
  }

  const remindersEnabled = appointmentRemindersEnabled || dailyCareRemindersEnabled || weeklyPregnancyRemindersEnabled || missedTaskRemindersEnabled

  return (
    <div className="notification-center">
      <button className="notification" type="button" aria-label="Notifications" aria-expanded={open} onClick={() => setOpen((visible) => !visible)}><Bell aria-hidden="true" strokeWidth={1.8} />{dueCount > 0 && <span className="notification-count">{dueCount}</span>}</button>
      {open && <section className="notification-popover" aria-label="Notification center"><div className="notification-heading"><div><p className="section-label">Reminders</p><h3>Notifications</h3></div><span>{dueCount} due</span></div>{!remindersEnabled ? <p className="notification-note">Reminders are off. Enable them in Profile settings.</p> : <>{browserNotificationsEnabled && permission === 'default' && <button className="enable-notifications" type="button" onClick={requestPermission}>Allow browser notifications</button>}{browserNotificationsEnabled && permission === 'denied' && <p className="notification-note">Browser notifications are blocked. Enable them in your browser site settings.</p>}{browserNotificationsEnabled && permission === 'unsupported' && <p className="notification-note">This browser does not support notifications.</p>}<div className="notification-list">{reminders.length === 0 ? <p className="notification-empty">No upcoming reminders.</p> : reminders.map((reminder) => <article className={reminder.due ? 'notification-item due' : 'notification-item'} key={reminder.key}><span className="notification-mark">{reminder.type === 'care' ? <ListChecks aria-hidden="true" /> : reminder.type === 'pregnancy' ? <Baby aria-hidden="true" /> : reminder.type === 'missed' ? <History aria-hidden="true" /> : <CalendarClock aria-hidden="true" />}</span><div><strong>{reminder.title}</strong><p>{reminder.type === 'missed' ? 'Review your care history' : reminder.due ? 'Reminder due now' : `Reminder ${reminderTimeFormat.format(reminder.reminderAt)}`}</p><small>{reminder.detail}</small></div></article>)}</div><p className="notification-footnote">{browserNotificationsEnabled ? 'Browser alerts work while MaatriCare is open.' : 'Browser alerts are off in Profile settings.'}</p></>}</section>}
    </div>
  )
}
