import { useState } from 'react'
import type { FormEvent } from 'react'
import { Pencil, Trash2 } from 'lucide-react'
import { createTask, deleteTask, updateTask } from '../api'
import type { CareTask } from '../api'
import { localeFor, translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type CareChecklistProps = {
  tasks: CareTask[]
  history: CareTask[]
  onToggle: (id: string, completed: boolean) => void
  onCreated: (task: CareTask) => void
  onUpdated: (task: CareTask) => void
  onDeleted: (id: string) => void
  language: LanguageCode
}

export function CareChecklist({ tasks, history, onToggle, onCreated, onUpdated, onDeleted, language }: CareChecklistProps) {
  const [showHistory, setShowHistory] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [title, setTitle] = useState('')
  const [recurrence, setRecurrence] = useState<'NONE' | 'DAILY' | 'WEEKLY'>('NONE')
  const [error, setError] = useState('')
  const [editingTask, setEditingTask] = useState<CareTask | null>(null)
  const [editingTitle, setEditingTitle] = useState('')
  const [deletingTask, setDeletingTask] = useState<CareTask | null>(null)
  const historyDateFormat = new Intl.DateTimeFormat(localeFor(language), { day: 'numeric', month: 'short' })
  const completedCount = tasks.filter((task) => task.completed).length
  const historyByDate = history.reduce<Record<string, CareTask[]>>((days, task) => {
    (days[task.taskDate] ??= []).push(task)
    return days
  }, {})
  const addTask = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !title.trim()) return
    try {
      onCreated(await createTask(token, title.trim(), new Date().toISOString().slice(0, 10), recurrence))
      setTitle(''); setRecurrence('NONE'); setError(''); setShowForm(false)
    } catch { setError('Unable to add your custom task.') }
  }
  return (
    <section className="panel checklist-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'care.label')}</p><h2>{translate(language, 'care.title')}</h2></div><span className="task-count">{completedCount} / {tasks.length} {translate(language, 'care.complete')}</span></div>
      <div className="task-list">{tasks.length === 0 ? <p className="panel-description">{translate(language, 'care.empty')}</p> : tasks.map((task) => <div className={`task ${task.completed ? 'done' : ''}`} key={task.id}><button className="task-main" type="button" onClick={() => onToggle(task.id, !task.completed)}><span className="check">{task.completed ? '✓' : ''}</span><span className="task-copy"><b>{task.title}</b><small>{task.completed ? translate(language, 'care.completed') : translate(language, 'care.planned')}</small></span></button>{!task.shared && <span className="task-actions"><button type="button" aria-label={`Edit ${task.title}`} onClick={() => { setEditingTask(task); setEditingTitle(task.title) }}><Pencil aria-hidden="true" /></button><button type="button" aria-label={`Delete ${task.title}`} onClick={() => setDeletingTask(task)}><Trash2 aria-hidden="true" /></button></span>}</div>)}</div>
      <button className="text-button" type="button" onClick={() => setShowHistory((visible) => !visible)}>{translate(language, showHistory ? 'care.hideHistory' : 'care.history')} <span>{showHistory ? '↑' : '→'}</span></button>
      <button className="text-button" type="button" onClick={() => setShowForm((visible) => !visible)}>{translate(language, showForm ? 'care.close' : 'care.add')} <span>{showForm ? '↑' : '→'}</span></button>
      {showForm && <form className="custom-task-form" onSubmit={addTask}><label>Task name<input required maxLength={160} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Call my midwife" /></label><label>Repeat<select value={recurrence} onChange={(event) => setRecurrence(event.target.value as 'NONE' | 'DAILY' | 'WEEKLY')}><option value="NONE">One time</option><option value="DAILY">Every day</option><option value="WEEKLY">Every week</option></select></label>{error && <p className="form-error">{error}</p>}<button className="primary-button" type="submit">Add task <span>→</span></button></form>}
      {editingTask && <div className="confirm-overlay"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="edit-task-title"><p className="section-label">Custom task</p><h2 id="edit-task-title">Edit task</h2><form className="custom-task-form" onSubmit={async (event) => { event.preventDefault(); const token = sessionStorage.getItem('maatricare-token'); if (!token) return; try { onUpdated(await updateTask(token, editingTask.id, editingTitle.trim())); setEditingTask(null) } catch { setError('Unable to update this task.') } }}><input required maxLength={160} value={editingTitle} onChange={(event) => setEditingTitle(event.target.value)} /><div className="confirm-actions"><button className="outline-button" type="button" onClick={() => setEditingTask(null)}>Cancel</button><button className="primary-button" type="submit">Save</button></div></form></section></div>}
      {deletingTask && <div className="confirm-overlay"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-task-title"><p className="section-label">Custom task</p><h2 id="delete-task-title">Delete this task?</h2><p>This removes it from today’s care list.</p><div className="confirm-actions"><button className="outline-button" type="button" onClick={() => setDeletingTask(null)}>Cancel</button><button className="danger-button" type="button" onClick={async () => { const token = sessionStorage.getItem('maatricare-token'); if (!token) return; try { await deleteTask(token, deletingTask.id); onDeleted(deletingTask.id); setDeletingTask(null) } catch { setError('Unable to delete this task.') } }}>Delete task</button></div></section></div>}
      {showHistory && <div className="task-history">{Object.keys(historyByDate).length === 0 ? <p className="panel-description">No activity recorded in the last seven days.</p> : Object.entries(historyByDate).reverse().map(([date, dayTasks]) => <div className="history-day" key={date}><div><strong>{historyDateFormat.format(new Date(`${date}T00:00:00`))}</strong><span>{dayTasks.filter((task) => task.completed).length} of {dayTasks.length} completed</span></div><ul>{dayTasks.map((task) => <li className={task.completed ? 'complete' : ''} key={task.id}><span>{task.completed ? '✓' : '○'}</span>{task.title}</li>)}</ul></div>)}</div>}
    </section>
  )
}
