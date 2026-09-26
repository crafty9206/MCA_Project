import { useState } from 'react'
import type { FormEvent } from 'react'
import { createAppointment, createAppointmentQuestion, deleteAppointment, deleteAppointmentQuestion, getAppointmentQuestions, updateAppointment, updateAppointmentQuestion } from '../api'
import type { Appointment, AppointmentQuestion } from '../api'
import { localeFor } from '../i18n'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type AppointmentCardProps = {
  appointments: Appointment[]
  onCreated: (appointment: Appointment) => void
  onUpdated: (appointment: Appointment) => void
  onDeleted: (id: string) => void
  language: LanguageCode
}

const reminderLabels: Record<number, string> = { 30: '30 minutes before', 60: '1 hour before', 1440: '1 day before', 2880: '2 days before', 10080: '1 week before' }

const localDateTimeValue = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

export function AppointmentCard({ appointments, onCreated, onUpdated, onDeleted, language }: AppointmentCardProps) {
  const locale = localeFor(language)
  const dateFormat = new Intl.DateTimeFormat(locale, { day: '2-digit', month: 'short' })
  const timeFormat = new Intl.DateTimeFormat(locale, { hour: 'numeric', minute: '2-digit' })
  const [showForm, setShowForm] = useState(false)
  const [showSchedule, setShowSchedule] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [showQuestions, setShowQuestions] = useState(false)
  const [questions, setQuestions] = useState<AppointmentQuestion[]>([])
  const [questionText, setQuestionText] = useState('')
  const [questionsLoading, setQuestionsLoading] = useState(false)
  const [title, setTitle] = useState('')
  const [startsAt, setStartsAt] = useState('')
  const [endsAt, setEndsAt] = useState('')
  const [providerName, setProviderName] = useState('')
  const [clinicName, setClinicName] = useState('')
  const [notes, setNotes] = useState('')
  const [reminderMinutesBefore, setReminderMinutesBefore] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const appointment = appointments[0]
  const start = appointment ? new Date(appointment.startsAt) : null
  const end = appointment?.endsAt ? new Date(appointment.endsAt) : null

  const closeForm = () => {
    setTitle('')
    setStartsAt('')
    setEndsAt('')
    setProviderName('')
    setClinicName('')
    setNotes('')
    setReminderMinutesBefore('')
    setError('')
    setEditingId(null)
    setShowForm(false)
  }

  const edit = (item: Appointment) => {
    setEditingId(item.id)
    setTitle(item.title)
    setStartsAt(localDateTimeValue(item.startsAt))
    setEndsAt(localDateTimeValue(item.endsAt))
    setProviderName(item.providerName ?? '')
    setClinicName(item.clinicName ?? '')
    setNotes(item.notes ?? '')
    setReminderMinutesBefore(item.reminderMinutesBefore?.toString() ?? '')
    setError('')
    setShowForm(true)
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    if (endsAt && new Date(endsAt) <= new Date(startsAt)) {
      setError('End time must be after the start time.')
      return
    }

    setSaving(true)
    setError('')
    try {
      const payload = {
        title: title.trim(),
        startsAt: new Date(startsAt).toISOString(),
        endsAt: endsAt ? new Date(endsAt).toISOString() : undefined,
        providerName: providerName.trim() || undefined,
        clinicName: clinicName.trim() || undefined,
        notes: notes.trim() || undefined,
        reminderMinutesBefore: reminderMinutesBefore ? Number(reminderMinutesBefore) : undefined,
      }
      if (editingId) onUpdated(await updateAppointment(token, editingId, payload))
      else onCreated(await createAppointment(token, payload))
      closeForm()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Unable to save this appointment.')
    } finally {
      setSaving(false)
    }
  }

  const remove = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !deletingId) return
    try {
      await deleteAppointment(token, deletingId)
      onDeleted(deletingId)
      setDeletingId(null)
    } catch {
      setError('Unable to delete this appointment.')
    }
  }

  const openQuestions = async () => {
    if (!appointment) return
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setShowQuestions(true)
    setQuestionsLoading(true)
    setError('')
    try { setQuestions(await getAppointmentQuestions(token, appointment.id)) }
    catch { setError('Unable to load your questions.') }
    finally { setQuestionsLoading(false) }
  }

  const addQuestion = async (event: FormEvent) => {
    event.preventDefault()
    if (!appointment || !questionText.trim()) return
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    try {
      const created = await createAppointmentQuestion(token, appointment.id, questionText.trim())
      setQuestions((current) => [...current, created])
      setQuestionText('')
    } catch { setError('Unable to save your question.') }
  }

  const toggleQuestion = async (item: AppointmentQuestion) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    const answered = !item.answered
    setQuestions((current) => current.map((question) => question.id === item.id ? { ...question, answered } : question))
    try { await updateAppointmentQuestion(token, item.id, item.question, answered) }
    catch { setQuestions((current) => current.map((question) => question.id === item.id ? item : question)); setError('Unable to update your question.') }
  }

  const removeQuestion = async (id: string) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    try { await deleteAppointmentQuestion(token, id); setQuestions((current) => current.filter((question) => question.id !== id)) }
    catch { setError('Unable to remove your question.') }
  }

  return (
    <section className="panel appointment-panel">
      <div className="panel-heading">
        <div><p className="section-label">{translate(language, 'appointment.label')}</p><h2>{translate(language, 'appointment.title')}</h2></div>
        {appointments.length > 0 && <button className="more-button" type="button" aria-label="View appointment schedule" onClick={() => setShowSchedule((visible) => !visible)}>•••</button>}
      </div>

      {appointment && start ? <div className="appointment-detail"><div className="date-block"><b>{start.getDate()}</b><span>{dateFormat.format(start).split(' ')[1]}</span></div><div><strong>{appointment.title}</strong><span>{start.toLocaleDateString(locale, { weekday: 'long' })} · {timeFormat.format(start)}{end ? ` – ${timeFormat.format(end)}` : ''}</span><span>{[appointment.providerName, appointment.clinicName].filter(Boolean).join(' · ') || '—'}</span>{appointment.reminderMinutesBefore != null && <span className="reminder-label">◇ {reminderLabels[appointment.reminderMinutesBefore] ?? appointment.reminderMinutesBefore}</span>}</div></div> : <p className="panel-description">{translate(language, 'appointment.empty')}</p>}

      {appointment && <div className="appointment-actions"><button className="primary-button" type="button" onClick={openQuestions}>Prepare questions <span>→</span></button><button className="outline-button" type="button" onClick={() => edit(appointment)}>Edit</button><button className="icon-danger-button" type="button" aria-label="Delete appointment" title="Delete appointment" onClick={() => setDeletingId(appointment.id)}>×</button></div>}

      {showSchedule && <div className="appointment-schedule">{appointments.map((item) => <button type="button" key={item.id} onClick={() => edit(item)}><span>{dateFormat.format(new Date(item.startsAt))}</span><strong>{item.title}</strong><small>{timeFormat.format(new Date(item.startsAt))} · {[item.providerName, item.clinicName].filter(Boolean).join(' · ') || 'No provider details'}{item.reminderMinutesBefore != null ? ` · Reminder ${reminderLabels[item.reminderMinutesBefore] ?? `${item.reminderMinutesBefore} min before`}` : ''}</small></button>)}</div>}

      <button className="text-button" type="button" onClick={() => showForm ? closeForm() : setShowForm(true)}>{translate(language, showForm ? 'appointment.close' : 'appointment.add')} <span>{showForm ? '↑' : '→'}</span></button>

      {showForm && <form className="appointment-form" onSubmit={submit}><label>Appointment title<input required maxLength={160} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Mid-pregnancy checkup" /></label><div className="form-row"><label>Start time<input required type="datetime-local" value={startsAt} onChange={(event) => setStartsAt(event.target.value)} /></label><label>End time <span className="optional">Optional</span><input type="datetime-local" value={endsAt} onChange={(event) => setEndsAt(event.target.value)} /></label></div><label>Provider <span className="optional">Optional</span><input maxLength={160} value={providerName} onChange={(event) => setProviderName(event.target.value)} placeholder="Dr. Meera Shah" /></label><label>Clinic <span className="optional">Optional</span><input maxLength={200} value={clinicName} onChange={(event) => setClinicName(event.target.value)} placeholder="Sunrise Women's Clinic" /></label><label>Reminder <span className="optional">Optional</span><select value={reminderMinutesBefore} onChange={(event) => setReminderMinutesBefore(event.target.value)}><option value="">No reminder</option><option value="30">30 minutes before</option><option value="60">1 hour before</option><option value="1440">1 day before</option><option value="2880">2 days before</option><option value="10080">1 week before</option></select></label><label>Notes <span className="optional">Optional</span><textarea maxLength={1000} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="Questions or preparation notes" /></label>{error && <p className="form-error">{error}</p>}<button className="primary-button" disabled={saving} type="submit">{saving ? 'Saving…' : editingId ? 'Update appointment' : 'Save appointment'} <span>→</span></button></form>}

      {showQuestions && <div className="confirm-overlay"><section className="questions-dialog" role="dialog" aria-modal="true" aria-labelledby="appointment-questions-title"><div className="settings-heading"><div><p className="section-label">Prepare together</p><h2 id="appointment-questions-title">Questions for your visit</h2><p>{appointment?.title}</p></div><button className="settings-close" type="button" aria-label="Close questions" onClick={() => setShowQuestions(false)}>×</button></div>{questionsLoading ? <p className="panel-description">Loading questions…</p> : <div className="question-list">{questions.length === 0 ? <p className="panel-description">No questions saved yet.</p> : questions.map((item) => <div className={item.answered ? 'question answered' : 'question'} key={item.id}><button className="question-check" type="button" aria-label={item.answered ? 'Mark unanswered' : 'Mark answered'} onClick={() => toggleQuestion(item)}>{item.answered ? '✓' : ''}</button><span>{item.question}</span><button className="question-delete" type="button" aria-label="Delete question" onClick={() => removeQuestion(item.id)}>×</button></div>)}</div>}<form className="question-form" onSubmit={addQuestion}><label>New question<textarea required maxLength={500} value={questionText} onChange={(event) => setQuestionText(event.target.value)} placeholder="What should I ask my healthcare professional?" /></label><button className="primary-button" type="submit">Add question <span>→</span></button></form>{error && <p className="form-error">{error}</p>}</section></div>}

      {deletingId && <div className="confirm-overlay"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-appointment-title"><p className="section-label">Appointment</p><h2 id="delete-appointment-title">Delete this appointment?</h2><p>This removes it permanently from your schedule.</p><div className="confirm-actions"><button className="outline-button" type="button" onClick={() => setDeletingId(null)}>Cancel</button><button className="danger-button" type="button" onClick={remove}>Delete appointment</button></div></section></div>}
    </section>
  )
}
