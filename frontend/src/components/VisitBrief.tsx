import { useEffect, useState } from 'react'
import { ClipboardList, Printer } from 'lucide-react'
import { getAppointmentQuestions } from '../api'
import type { Appointment, AppointmentQuestion, SymptomEntry } from '../api'

type Props = { appointments: Appointment[]; selectedAppointmentId: string | null; onSelectedAppointmentChange: (id: string | null) => void; symptoms: SymptomEntry[]; displayName: string }

const dateTimeFormat = new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' })

export function VisitBrief({ appointments, selectedAppointmentId, onSelectedAppointmentChange, symptoms, displayName }: Props) {
  const [questions, setQuestions] = useState<AppointmentQuestion[]>([])
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>([])
  const [includeNotes, setIncludeNotes] = useState(false)
  const [includeAnswered, setIncludeAnswered] = useState(false)
  const [includeAnswers, setIncludeAnswers] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [preview, setPreview] = useState(false)

  const appointmentId = selectedAppointmentId ?? ''
  const appointment = appointments.find((item) => item.id === appointmentId)
  const journalEntries = symptoms.filter((entry) => selectedSymptoms.includes(entry.id))
  const briefQuestions = questions.filter((item) => includeAnswered || !item.answered)

  useEffect(() => {
    const token = sessionStorage.getItem('maatricare-token')
    setQuestions([])
    setError('')
    setPreview(false)
    setSelectedSymptoms([])
    setIncludeNotes(false)
    setIncludeAnswers(false)
    if (!appointmentId || !token) return
    let active = true
    setLoading(true)
    getAppointmentQuestions(token, appointmentId)
      .then((items) => { if (active) setQuestions(items) })
      .catch(() => { if (active) setError('Could not load appointment questions. Try selecting the appointment again.') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [appointmentId])

  const toggleSymptom = (id: string) => {
    setSelectedSymptoms((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id])
    setIncludeNotes(false)
    setPreview(false)
  }

  return (
    <section className="panel visit-brief-panel" aria-labelledby="visit-brief-title">
      <div className="panel-heading"><div><p className="section-label">For your next conversation</p><h2 id="visit-brief-title">Visit brief</h2></div><ClipboardList className="visit-brief-mark" aria-hidden="true" /></div>
      <p className="panel-description">Prepare a printable summary to discuss with your care team. Nothing is shared automatically.</p>
      <label className="visit-brief-field">Appointment
        <select value={appointmentId} onChange={(event) => onSelectedAppointmentChange(event.target.value || null)}>
          <option value="">Choose an appointment</option>
          {appointments.map((item) => <option key={item.id} value={item.id}>{item.title} · {dateTimeFormat.format(new Date(item.startsAt))}</option>)}
        </select>
      </label>
      {appointment && <>
        <div className="visit-brief-choices">
          <div><strong>Questions to discuss</strong><span>{loading ? 'Loading…' : `${briefQuestions.length} saved ${briefQuestions.length === 1 ? 'question' : 'questions'}`}</span></div>
          <label className="visit-brief-option"><input type="checkbox" checked={includeAnswered} onChange={(event) => { setIncludeAnswered(event.target.checked); setPreview(false) }} /> Include questions already marked answered</label>
          <label className="visit-brief-option"><input type="checkbox" checked={includeAnswers} onChange={(event) => { setIncludeAnswers(event.target.checked); setPreview(false) }} /> Include recorded answers for the questions shown</label>
          {!loading && !error && questions.length === 0 && <p>Add questions in the appointment card above to include them here.</p>}
          {error && <p className="form-error" role="alert">{error}</p>}
        </div>
        <div className="visit-brief-choices">
          <div><strong>Journal entries</strong><span>{selectedSymptoms.length} selected</span></div>
          <p>Private entries are excluded unless you select them here.</p>
          {symptoms.length === 0 ? <p>No journal entries recorded.</p> : <div className="visit-brief-entry-list">{symptoms.map((entry) => <label className="visit-brief-option" key={entry.id}><input type="checkbox" checked={selectedSymptoms.includes(entry.id)} onChange={() => toggleSymptom(entry.id)} /><span>{entry.symptom} · {dateTimeFormat.format(new Date(entry.occurredAt))}</span></label>)}</div>}
          {selectedSymptoms.length > 0 && <label className="visit-brief-option"><input type="checkbox" checked={includeNotes} onChange={(event) => { setIncludeNotes(event.target.checked); setPreview(false) }} /> Include notes from selected entries</label>}
        </div>
        <div className="visit-brief-actions"><button className="primary-button" type="button" disabled={loading || Boolean(error)} onClick={() => setPreview(true)}>Preview brief</button>{preview && <button className="outline-button" type="button" onClick={() => window.print()}><Printer aria-hidden="true" /> Print / PDF</button>}</div>
      </>}
      {preview && appointment && <article className="visit-brief-document" id="visit-brief">
        <header><p>MaatriCare · Visit brief</p><h3>{appointment.title}</h3><span>{displayName} · {dateTimeFormat.format(new Date(appointment.startsAt))}</span>{appointment.providerName && <span>{appointment.providerName}{appointment.clinicName ? ` · ${appointment.clinicName}` : ''}</span>}</header>
        <section><h4>Questions to discuss</h4>{briefQuestions.length ? <ul>{briefQuestions.map((item) => <li key={item.id}>{item.question}{includeAnswers && item.answer && <p className="visit-recorded-answer"><strong>Your recorded answer: </strong>{item.answer}</p>}</li>)}</ul> : <p>No questions selected.</p>}</section>
        {journalEntries.length > 0 && <section><h4>Journal entries shared for this visit</h4>{journalEntries.map((entry) => <div className="visit-brief-entry" key={entry.id}><strong>{entry.symptom} · {entry.severity.toLowerCase()}</strong><span>{dateTimeFormat.format(new Date(entry.occurredAt))}</span>{includeNotes && entry.notes && <p>{entry.notes}</p>}</div>)}</section>}
        <footer>User-recorded information for discussion only. This is not a diagnosis or a medical record.</footer>
      </article>}
    </section>
  )
}