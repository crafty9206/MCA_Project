import { useState } from 'react'
import { recordQuestionAnswer } from '../api'
import type { AppointmentQuestion } from '../api'

export function QuestionAnswer({ question, onSaved }: { question: AppointmentQuestion; onSaved: (item: AppointmentQuestion) => void }) {
  const [editing, setEditing] = useState(false)
  const [answer, setAnswer] = useState(question.answer ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)

  return <div className="question-answer">
    {question.answer && <p><strong>Your recorded answer</strong><span>{question.answer}</span></p>}
    {!editing ? <button className="text-button" type="button" onClick={() => { setAnswer(question.answer ?? ''); setEditing(true); setError(''); setSaved(false) }}>{question.answer ? 'Edit answer' : 'Record answer'}</button> : <form onSubmit={async (event) => {
      event.preventDefault()
      const token = sessionStorage.getItem('maatricare-token')
      if (!token) { setError('Please sign in again.'); return }
      setSaving(true)
      setError('')
      try { onSaved(await recordQuestionAnswer(token, question.id, answer)); setEditing(false); setSaved(true) }
      catch { setError('Answer was not saved. Your draft is still here; please retry.') }
      finally { setSaving(false) }
    }}><label>What your clinician said<textarea rows={3} maxLength={2000} value={answer} disabled={saving} onChange={(event) => setAnswer(event.target.value)} /></label><small>Record the original wording. Confirm unclear medical instructions with your care team. Saving a blank answer clears this note.</small><div><button className="outline-button" type="button" disabled={saving} onClick={() => setEditing(false)}>Cancel</button><button className="primary-button" disabled={saving}>{saving ? 'Saving…' : 'Save answer'}</button></div></form>}
    {error && <p className="form-error" role="alert">{error}</p>}
    {saved && <small role="status">Answer saved.</small>}
  </div>
}