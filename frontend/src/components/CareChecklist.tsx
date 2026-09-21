import { useState } from 'react'
import type { CareTask } from '../api'

type CareChecklistProps = {
  tasks: CareTask[]
  history: CareTask[]
  onToggle: (id: string, completed: boolean) => void
}

const historyDateFormat = new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short' })

export function CareChecklist({ tasks, history, onToggle }: CareChecklistProps) {
  const [showHistory, setShowHistory] = useState(false)
  const completedCount = tasks.filter((task) => task.completed).length
  const historyByDate = history.reduce<Record<string, CareTask[]>>((days, task) => {
    (days[task.taskDate] ??= []).push(task)
    return days
  }, {})
  return (
    <section className="panel checklist-panel">
      <div className="panel-heading"><div><p className="section-label">Your daily rhythm</p><h2>Today’s care</h2></div><span className="task-count">{completedCount} of {tasks.length} complete</span></div>
      <div className="task-list">{tasks.length === 0 ? <p className="panel-description">No care tasks planned for today.</p> : tasks.map((task) => <button className={`task ${task.completed ? 'done' : ''}`} key={task.id} type="button" onClick={() => onToggle(task.id, !task.completed)}><span className="check">{task.completed ? '✓' : ''}</span><span className="task-copy"><b>{task.title}</b><small>{task.completed ? 'Completed' : 'Planned for today'}</small></span><span className="task-arrow">›</span></button>)}</div>
      <button className="text-button" type="button" onClick={() => setShowHistory((visible) => !visible)}>{showHistory ? 'Hide recent activity' : 'View recent activity'} <span>{showHistory ? '↑' : '→'}</span></button>
      {showHistory && <div className="task-history">{Object.keys(historyByDate).length === 0 ? <p className="panel-description">No activity recorded in the last seven days.</p> : Object.entries(historyByDate).reverse().map(([date, dayTasks]) => <div className="history-day" key={date}><div><strong>{historyDateFormat.format(new Date(`${date}T00:00:00`))}</strong><span>{dayTasks.filter((task) => task.completed).length} of {dayTasks.length} completed</span></div><ul>{dayTasks.map((task) => <li className={task.completed ? 'complete' : ''} key={task.id}><span>{task.completed ? '✓' : '○'}</span>{task.title}</li>)}</ul></div>)}</div>}
    </section>
  )
}
