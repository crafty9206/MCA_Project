import type { CareTask } from '../api'

type WeeklyCareSummaryProps = { history: CareTask[]; todayTasks: CareTask[] }

const dateFormat = new Intl.DateTimeFormat('en-IN', { weekday: 'short' })

export function WeeklyCareSummary({ history, todayTasks }: WeeklyCareSummaryProps) {
  const allTasks = [...history, ...todayTasks]
  const grouped = allTasks.reduce<Record<string, CareTask[]>>((days, task) => {
    (days[task.taskDate] ??= []).push(task)
    return days
  }, {})
  const days = Object.entries(grouped).sort(([first], [second]) => first.localeCompare(second))
  const completed = allTasks.filter((task) => task.completed).length
  const total = allTasks.length
  const percentage = total ? Math.round((completed / total) * 100) : 0

  return (
    <section className="panel weekly-summary-panel">
      <div className="panel-heading"><div><p className="section-label">Your week in care</p><h2>Weekly care summary</h2></div><span className="summary-score">{percentage}%</span></div>
      <p className="summary-intro">{total ? `${completed} of ${total} care tasks completed across the recorded week.` : 'Complete a care task to start your weekly summary.'}</p>
      <div className="summary-bars">{days.length === 0 ? <p className="panel-description">No care history recorded yet.</p> : days.map(([date, dayTasks]) => { const dayCompleted = dayTasks.filter((task) => task.completed).length; const dayProgress = Math.round((dayCompleted / dayTasks.length) * 100); return <div className="summary-day" key={date}><span>{dateFormat.format(new Date(`${date}T00:00:00`))}</span><div className="summary-bar"><i style={{ width: `${dayProgress}%` }} /></div><b>{dayCompleted}/{dayTasks.length}</b></div> })}</div>
    </section>
  )
}
