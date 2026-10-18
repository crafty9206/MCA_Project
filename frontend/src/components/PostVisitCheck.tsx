import { useState } from 'react'
import { Check, MessageCircleQuestion, Save } from 'lucide-react'
import { createAppointmentQuestion, reviewVisit } from '../api'
import type { Appointment, PostVisitReview } from '../api'

export function PostVisitCheck({ appointments, selectedAppointmentId, onSelectedAppointmentChange }: { appointments: Appointment[]; selectedAppointmentId: string | null; onSelectedAppointmentChange: (id: string | null) => void }) {
  const appointmentId = selectedAppointmentId ?? ''
  const [notes, setNotes] = useState('')
  const [useAi, setUseAi] = useState(false)
  const [result, setResult] = useState<PostVisitReview | null>(null)
  const [choices, setChoices] = useState<Record<number, 'understood' | 'unclear'>>({})
  const [drafts, setDrafts] = useState<Record<number, string>>({})
  const [saved, setSaved] = useState<number[]>([])
  const [busy, setBusy] = useState(false)
  const [saving, setSaving] = useState<number | null>(null)
  const [error, setError] = useState('')

  const reset = () => { setResult(null); setChoices({}); setDrafts({}); setSaved([]); setError('') }
  const analyze = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    reset()
    setBusy(true)
    try {
      const response = await reviewVisit(token, appointmentId, notes, useAi)
      setResult(response)
      setDrafts(Object.fromEntries(response.points.map((point, index) => [index, `Could you clarify this note: "${point.slice(0, 400)}"?`])))
    } catch { setError('The review could not be prepared. Please try again.') }
    finally { setBusy(false) }
  }
  const saveQuestion = async (index: number) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !drafts[index]?.trim()) return
    setSaving(index)
    setError('')
    try {
      await createAppointmentQuestion(token, appointmentId, drafts[index].trim())
      setSaved((current) => [...current, index])
    } catch { setError('Question was not saved. Your draft is still available to retry.') }
    finally { setSaving(null) }
  }

  return <section className="panel post-visit-panel" aria-labelledby="post-visit-title">
    <p className="section-label">After your appointment</p><h2 id="post-visit-title">What did we discuss?</h2>
    <p className="panel-description">Notes and review choices are temporary. Only questions you choose to save are stored.</p>
    <form className="post-visit-form" onSubmit={(event) => { event.preventDefault(); void analyze() }}>
      <label>Visit<select required disabled={busy || saving !== null} value={appointmentId} onChange={(event) => { onSelectedAppointmentChange(event.target.value || null); setNotes(''); reset() }}><option value="">Choose a visit</option>{appointments.map((item) => <option key={item.id} value={item.id}>{item.title} · {new Date(item.startsAt).toLocaleDateString()}</option>)}</select></label>
      <label>Your original notes<textarea required maxLength={5000} rows={5} disabled={busy || saving !== null} value={notes} onChange={(event) => { setNotes(event.target.value); reset() }} placeholder="One point per line, exactly as you remember it" /></label>
      <label className="post-visit-consent"><input type="checkbox" disabled={busy} checked={useAi} onChange={(event) => { setUseAi(event.target.checked); reset() }} /> Use AI to draft a recap from these notes</label>
      <button className="primary-button" disabled={busy || saving !== null || !appointmentId || !notes.trim()}>{busy ? 'Preparing review…' : 'Review my notes'}</button>
    </form>
    {error && <p className="form-error" role="alert">{error}</p>}
    {result && <div className="post-visit-review">
      <section className="post-visit-recap"><h3>{result.source === 'LOCAL_OLLAMA' ? 'AI-generated draft — check against your notes' : result.source === 'LOCAL_MODEL_UNAVAILABLE' ? 'AI unavailable — original-note review' : 'Original-note review'}</h3><p>{result.recap}</p></section>
      {result.points.map((point, index) => <section className="post-visit-point" key={index}>
        <h3>Original note {index + 1}</h3><blockquote>{point}</blockquote>
        <div className="post-visit-status" role="group" aria-label={`Understanding of note ${index + 1}`}><button type="button" aria-pressed={choices[index] === 'understood'} onClick={() => setChoices((current) => ({ ...current, [index]: 'understood' }))}><Check /> Understood</button><button type="button" aria-pressed={choices[index] === 'unclear'} onClick={() => setChoices((current) => ({ ...current, [index]: 'unclear' }))}><MessageCircleQuestion /> Need clarification</button></div>
        {choices[index] === 'unclear' && <div className="post-visit-question"><label>Question for your care team<textarea maxLength={500} rows={3} disabled={saved.includes(index) || saving === index} value={drafts[index] ?? ''} onChange={(event) => setDrafts((current) => ({ ...current, [index]: event.target.value }))} /></label><button type="button" disabled={saving !== null || saved.includes(index) || !drafts[index]?.trim()} onClick={() => void saveQuestion(index)}><Save /> {saved.includes(index) ? 'Saved to visit questions' : saving === index ? 'Saving…' : 'Save question'}</button></div>}
      </section>)}
      <p className="post-visit-safety">{result.safetyNote} Do not change medication or treatment based on this review. Confirm unclear instructions directly with your clinician or pharmacist.</p>
    </div>}
  </section>
}