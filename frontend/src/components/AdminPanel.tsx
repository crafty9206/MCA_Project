import { useState } from 'react'
import type { FormEvent } from 'react'
import { createAdminTask } from '../api'

type AdminPanelProps = { onLogout: () => void | Promise<void> }

export function AdminPanel({ onLogout }: AdminPanelProps) {
  const [taskTitle, setTaskTitle] = useState('')
  const [taskDate, setTaskDate] = useState(new Date().toISOString().slice(0, 10))
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const submitTask = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    try {
      await createAdminTask(token, { title: taskTitle, taskDate })
      setTaskTitle('')
      setMessage('Shared daily task added for all users.')
    } catch {
      setError('Unable to add the care task.')
    }
  }

  return (
    <main className="admin-page">
      <header className="admin-header"><div className="auth-brand"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></div><div><span className="admin-badge">ADMIN WORKSPACE</span><button className="outline-button" type="button" onClick={onLogout}>Sign out</button></div></header>
      <section className="admin-intro"><p className="landing-kicker"><span /> Care coordination</p><h1>Manage the daily rhythm.</h1><p>Add simple daily task names for MaatriCare members. Users manage their own appointments.</p></section>
      {message && <p className="admin-success" role="status">{message}</p>}{error && <p className="form-error" role="alert">{error}</p>}
      <div className="admin-grid"><form className="admin-form" onSubmit={submitTask}><p className="section-label">Daily rhythm</p><h2>Add a shared daily task</h2><label>Task name<input required value={taskTitle} onChange={(event) => setTaskTitle(event.target.value)} placeholder="Take prenatal vitamins" /></label><label>Assign for date<input required type="date" value={taskDate} onChange={(event) => setTaskDate(event.target.value)} /></label><button className="primary-button" type="submit">Add for all users <span>→</span></button></form></div>
    </main>
  )
}
