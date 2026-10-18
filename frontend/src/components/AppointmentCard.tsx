import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { CalendarPlus, LoaderCircle, Search, Sparkles } from 'lucide-react'
import { createAppointment, createAppointmentQuestion, deleteAppointment, deleteAppointmentQuestion, getAppointmentQuestions, suggestAppointmentQuestions, updateAppointment, updateAppointmentQuestion } from '../api'
import type { Appointment, AppointmentQuestion } from '../api'
import { localeFor } from '../i18n'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'
import { QuestionAnswer } from './QuestionAnswer'

type AppointmentCardProps = {
  appointments: Appointment[]
  selectedAppointmentId: string | null
  onSelectedAppointmentChange: (id: string | null) => void
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

const escapeCalendarText = (value: string) => value
  .replace(/\\/g, '\\\\')
  .replace(/\r?\n/g, '\\n')
  .replace(/,/g, '\\,')
  .replace(/;/g, '\\;')

const calendarDateTime = (value: Date) => value.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, 'Z')

const calendarTrigger = (minutes: number) => minutes % 1440 === 0
  ? `-P${minutes / 1440}D`
  : minutes % 60 === 0 ? `-PT${minutes / 60}H` : `-PT${minutes}M`

export function AppointmentCard({ appointments, selectedAppointmentId, onSelectedAppointmentChange, onCreated, onUpdated, onDeleted, language }: AppointmentCardProps) {
  const locale = localeFor(language)
  const dateFormat = new Intl.DateTimeFormat(locale, { day: '2-digit', month: 'short' })
  const timeFormat = new Intl.DateTimeFormat(locale, { hour: 'numeric', minute: '2-digit' })
  const [showForm, setShowForm] = useState(false)
  const [visitView, setVisitView] = useState<'UPCOMING' | 'PAST' | 'ALL'>('UPCOMING')
  const [visitSearch, setVisitSearch] = useState('')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [showQuestions, setShowQuestions] = useState(false)
  const [questions, setQuestions] = useState<AppointmentQuestion[]>([])
  const [suggestedQuestions, setSuggestedQuestions] = useState<string[]>([])
  const [suggestionSource, setSuggestionSource] = useState<'LOCAL_OLLAMA' | 'GUIDED_SUPPORT' | null>(null)
  const [suggestionsLoading, setSuggestionsLoading] = useState(false)
  const [questionText, setQuestionText] = useState('')
  const [questionsLoading, setQuestionsLoading] = useState(false)
  const [questionSaving, setQuestionSaving] = useState(false)
  const [questionSaveError, setQuestionSaveError] = useState('')
  const [questionSaveStatus, setQuestionSaveStatus] = useState('')
  const [title, setTitle] = useState('')
  const [startsAt, setStartsAt] = useState('')
  const [endsAt, setEndsAt] = useState('')
  const [providerName, setProviderName] = useState('')
  const [clinicName, setClinicName] = useState('')
  const [notes, setNotes] = useState('')
  const [reminderMinutesBefore, setReminderMinutesBefore] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const now = Date.now()
  const query = visitSearch.trim().toLowerCase()
  const visibleVisits = appointments.filter((item) => {
    const upcoming = new Date(item.endsAt ?? item.startsAt).getTime() >= now
    return (visitView === 'ALL' || (visitView === 'UPCOMING' ? upcoming : !upcoming))
      && [item.title, item.providerName, item.clinicName].some((value) => value?.toLowerCase().includes(query))
  }).sort((first, second) => visitView === 'PAST'
    ? new Date(second.startsAt).getTime() - new Date(first.startsAt).getTime()
    : new Date(first.startsAt).getTime() - new Date(second.startsAt).getTime())
  const appointment = visibleVisits.find((item) => item.id === selectedAppointmentId) ?? visibleVisits[0]
  const start = appointment ? new Date(appointment.startsAt) : null
  const end = appointment?.endsAt ? new Date(appointment.endsAt) : null

  useEffect(() => {
    const selectedAppointment = appointments.find((item) => item.id === selectedAppointmentId)
    if (selectedAppointment && !visibleVisits.some((item) => item.id === selectedAppointmentId)) {
      const upcoming = new Date(selectedAppointment.endsAt ?? selectedAppointment.startsAt).getTime() >= now
      if (query) setVisitSearch('')
      setVisitView(upcoming ? 'UPCOMING' : 'PAST')
      return
    }
    if (appointment?.id !== selectedAppointmentId) onSelectedAppointmentChange(appointment?.id ?? null)
  }, [appointment?.id, appointments, now, onSelectedAppointmentChange, query, selectedAppointmentId, visibleVisits])

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

  const addToCalendar = () => {
    if (!appointment) return
    const startsAt = new Date(appointment.startsAt)
    const endsAt = appointment.endsAt ? new Date(appointment.endsAt) : new Date(startsAt.getTime() + 30 * 60_000)
    const description = [appointment.providerName && `Provider: ${appointment.providerName}`, appointment.notes]
      .filter(Boolean).join('\n')
    const lines = [
      'BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//MaatriCare//Appointment//EN', 'CALSCALE:GREGORIAN',
      'BEGIN:VEVENT', `UID:${appointment.id}@maatricare`, `DTSTAMP:${calendarDateTime(new Date())}`,
      `DTSTART:${calendarDateTime(startsAt)}`, `DTEND:${calendarDateTime(endsAt)}`,
      `SUMMARY:${escapeCalendarText(appointment.title)}`,
      ...(description ? [`DESCRIPTION:${escapeCalendarText(description)}`] : []),
      ...(appointment.clinicName ? [`LOCATION:${escapeCalendarText(appointment.clinicName)}`] : []),
      ...(appointment.reminderMinutesBefore != null ? [
        'BEGIN:VALARM', `TRIGGER:${calendarTrigger(appointment.reminderMinutesBefore)}`,
        'ACTION:DISPLAY', `DESCRIPTION:${escapeCalendarText(appointment.title)}`, 'END:VALARM',
      ] : []),
      'END:VEVENT', 'END:VCALENDAR', ''
    ]
    const file = new Blob([lines.join('\r\n')], { type: 'text/calendar;charset=utf-8' })
    const url = URL.createObjectURL(file)
    const link = document.createElement('a')
    link.href = url
    link.download = `${appointment.title.trim().replace(/[^a-z0-9]+/gi, '-').replace(/^-|-$/g, '') || 'appointment'}.ics`
    document.body.append(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(url), 1000)
  }

  const openQuestions = async () => {
    if (!appointment) return
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setShowQuestions(true)
    setQuestionsLoading(true)
    setQuestionSaveError('')
    setQuestionSaveStatus('')
    setSuggestedQuestions([])
    setSuggestionSource(null)
    setError('')
    try { setQuestions(await getAppointmentQuestions(token, appointment.id)) }
    catch { setError('Unable to load your questions.') }
    finally { setQuestionsLoading(false) }
  }

  const generateQuestionSuggestions = async () => {
    if (!appointment) return
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setSuggestionsLoading(true)
    setError('')
    try {
      const result = await suggestAppointmentQuestions(token, appointment.id)
      setSuggestedQuestions(result.questions)
      setSuggestionSource(result.source)
    } catch {
      setError('Unable to suggest questions right now.')
    } finally {
      setSuggestionsLoading(false)
    }
  }

  const addSuggestedQuestion = async (suggestion: string) => {
    if (!appointment || questionSaving) return
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setQuestionSaving(true)
    setQuestionSaveError('')
    setQuestionSaveStatus('')
    try {
      const created = await createAppointmentQuestion(token, appointment.id, suggestion.trim())
      setQuestions((current) => [...current, created])
      setSuggestedQuestions((current) => current.filter((item) => item !== suggestion))
      setQuestionSaveStatus('Question added to your saved questions below.')
    } catch {
      setQuestionSaveError('This question was not saved. Please try again; if it fails again, check that the backend is running and sign in again.')
    } finally {
      setQuestionSaving(false)
    }
  }

  const addQuestion = async (event: FormEvent) => {
    event.preventDefault()
    if (!appointment || !questionText.trim() || questionSaving) return
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setQuestionSaving(true)
    setQuestionSaveError('')
    setQuestionSaveStatus('')
    try {
      const created = await createAppointmentQuestion(token, appointment.id, questionText.trim())
      setQuestions((current) => [...current, created])
      setQuestionText('')
      setQuestionSaveStatus('Question added to your saved questions below.')
    } catch { setQuestionSaveError('Your question was not saved. Please try again.') }
    finally { setQuestionSaving(false) }
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
        <div><p className="section-label">Your visits</p><h2>Appointment history</h2></div>
        <span className="task-count">{visibleVisits.length} of {appointments.length}</span>
      </div>

      <div className="visit-browser">
        <div className="visit-view-options" role="group" aria-label="Appointment period">{([['UPCOMING', 'Upcoming'], ['PAST', 'Past'], ['ALL', 'All visits']] as const).map(([value, label]) => <button type="button" key={value} aria-pressed={visitView === value} onClick={() => { setVisitView(value); onSelectedAppointmentChange(null); closeForm() }}>{label}</button>)}</div>
        <label className="visit-search"><Search aria-hidden="true" /><span className="sr-only">Search appointments</span><input type="search" value={visitSearch} onChange={(event) => { setVisitSearch(event.target.value); onSelectedAppointmentChange(null); closeForm() }} placeholder="Search title, doctor, or clinic" /></label>
        {visibleVisits.length > 0 ? <div className="visit-browser-list" aria-label="Choose a visit">{visibleVisits.map((item) => <button type="button" key={item.id} aria-pressed={appointment?.id === item.id} onClick={() => { onSelectedAppointmentChange(item.id); closeForm(); setQuestions([]); setSuggestedQuestions([]) }}><strong>{item.title}</strong><time dateTime={item.startsAt}>{new Date(item.startsAt).toLocaleString(locale, { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })}</time><span>{[item.providerName, item.clinicName].filter(Boolean).join(' · ')}</span></button>)}</div> : <p className="panel-description">{appointments.length === 0 ? 'No appointments recorded yet.' : query ? 'No visits match your search.' : `No ${visitView === 'UPCOMING' ? 'upcoming' : 'past'} visits.`}</p>}
      </div>

      {appointment && start && <div className="appointment-detail"><div className="date-block"><b>{start.getDate()}</b><span>{dateFormat.format(start).split(' ')[1]}</span></div><div><strong>{appointment.title}</strong><span>{start.toLocaleDateString(locale, { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' })} · {timeFormat.format(start)}{end ? ` – ${timeFormat.format(end)}` : ''}</span><span>{[appointment.providerName, appointment.clinicName].filter(Boolean).join(' · ') || '—'}</span>{appointment.reminderMinutesBefore != null && <span className="reminder-label">◇ {reminderLabels[appointment.reminderMinutesBefore] ?? appointment.reminderMinutesBefore}</span>}</div></div>}

      {appointment && <div className="appointment-actions"><button className="primary-button" type="button" onClick={openQuestions}>Prepare questions <span>→</span></button><button className="outline-button" type="button" onClick={() => edit(appointment)}>Edit</button><button className="icon-danger-button" type="button" aria-label="Delete appointment" title="Delete appointment" onClick={() => setDeletingId(appointment.id)}>×</button></div>}
      {appointment && <div className="calendar-export-row"><button className="outline-button calendar-export-button" type="button" onClick={addToCalendar}><CalendarPlus aria-hidden="true" /> Add to calendar</button><small>Downloads an event file you can import into your calendar app.</small></div>}

      <button className="text-button" type="button" onClick={() => showForm ? closeForm() : setShowForm(true)}>{translate(language, showForm ? 'appointment.close' : 'appointment.add')} <span>{showForm ? '↑' : '→'}</span></button>

      {showForm && <form className="appointment-form" onSubmit={submit}><label>Appointment title<input required maxLength={160} value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Mid-pregnancy checkup" /></label><div className="form-row"><label>Start time<input required type="datetime-local" value={startsAt} onChange={(event) => setStartsAt(event.target.value)} /></label><label>End time <span className="optional">Optional</span><input type="datetime-local" value={endsAt} onChange={(event) => setEndsAt(event.target.value)} /></label></div><label>Provider <span className="optional">Optional</span><input maxLength={160} value={providerName} onChange={(event) => setProviderName(event.target.value)} placeholder="Dr. Meera Shah" /></label><label>Clinic <span className="optional">Optional</span><input maxLength={200} value={clinicName} onChange={(event) => setClinicName(event.target.value)} placeholder="Sunrise Women's Clinic" /></label><label>Reminder <span className="optional">Optional</span><select value={reminderMinutesBefore} onChange={(event) => setReminderMinutesBefore(event.target.value)}><option value="">No reminder</option><option value="30">30 minutes before</option><option value="60">1 hour before</option><option value="1440">1 day before</option><option value="2880">2 days before</option><option value="10080">1 week before</option></select></label><label>Notes <span className="optional">Optional</span><textarea maxLength={1000} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="Questions or preparation notes" /></label>{error && <p className="form-error">{error}</p>}<button className="primary-button" disabled={saving} type="submit">{saving ? 'Saving…' : editingId ? 'Update appointment' : 'Save appointment'} <span>→</span></button></form>}

      {showQuestions && <div className="confirm-overlay"><section className="questions-dialog" role="dialog" aria-modal="true" aria-labelledby="appointment-questions-title"><div className="settings-heading"><div><p className="section-label">Prepare together</p><h2 id="appointment-questions-title">Questions for your visit</h2><p>{appointment?.title}</p></div><button className="settings-close" type="button" aria-label="Close questions" onClick={() => setShowQuestions(false)}>×</button></div>
        <div className="question-suggestions"><div><div><strong>Question ideas</strong><small>Based only on the appointment title. Review before saving.</small></div><button className="outline-button" type="button" disabled={suggestionsLoading || questionSaving} onClick={generateQuestionSuggestions}>{suggestionsLoading ? <LoaderCircle className="assistant-spinner" /> : <Sparkles />} {suggestionsLoading ? 'Thinking…' : 'Suggest with AI'}</button></div>{suggestionSource && <p>{suggestionSource === 'LOCAL_OLLAMA' ? 'Generated by your local Ollama model.' : 'Guided question suggestions (local AI unavailable).'}</p>}{suggestedQuestions.map((suggestion) => <article key={suggestion}><span>{suggestion}</span><button type="button" disabled={questionSaving || questionsLoading} onClick={() => addSuggestedQuestion(suggestion)}>{questionSaving ? 'Saving…' : 'Save question'}</button></article>)}</div>
        {questionSaveError && <p className="form-error" role="alert">{questionSaveError}</p>}
        {questionSaveStatus && <p className="form-success" role="status">{questionSaveStatus}</p>}
        <p className="section-label">Saved questions ({questions.length})</p>
        {questionsLoading ? <p className="panel-description">Loading questions…</p> : <div className="question-list">{questions.length === 0 ? <p className="panel-description">No questions saved yet.</p> : questions.map((item) => <div key={item.id} className="saved-question-block"><div className={item.answered ? 'question answered' : 'question'}><button className="question-check" type="button" aria-label={item.answered ? 'Mark unanswered' : 'Mark answered'} onClick={() => toggleQuestion(item)}>{item.answered ? '✓' : ''}</button><span>{item.question}</span><button className="question-delete" type="button" aria-label="Delete question" onClick={() => removeQuestion(item.id)}>×</button></div><QuestionAnswer question={item} onSaved={(updated) => setQuestions((current) => current.map((question) => question.id === updated.id ? updated : question))} /></div>)}</div>}<form className="question-form" onSubmit={addQuestion}><label>New question<textarea required maxLength={500} value={questionText} onChange={(event) => setQuestionText(event.target.value)} placeholder="What should I ask my healthcare professional?" /></label><button className="primary-button" type="submit">Add question <span>→</span></button></form>{error && <p className="form-error">{error}</p>}</section></div>}

      {deletingId && <div className="confirm-overlay"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-appointment-title"><p className="section-label">Appointment</p><h2 id="delete-appointment-title">Delete this appointment?</h2><p>This removes it permanently from your schedule.</p><div className="confirm-actions"><button className="outline-button" type="button" onClick={() => setDeletingId(null)}>Cancel</button><button className="danger-button" type="button" onClick={remove}>Delete appointment</button></div></section></div>}
    </section>
  )
}
