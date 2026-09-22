import { useState } from 'react'
import type { FormEvent } from 'react'
import { createSymptom, deleteSymptom, updateSymptom } from '../api'
import type { SymptomEntry } from '../api'

const symptoms = ['Feeling well', 'Tired', 'Nauseous', 'Other']
type JournalCardProps = { selected: string | null; onSelect: (symptom: string) => void; entries: SymptomEntry[]; onCreated: (entry: SymptomEntry) => void; onUpdated: (entry: SymptomEntry) => void; onDeleted: (id: string) => void }

export function JournalCard({ selected, onSelect, entries, onCreated, onUpdated, onDeleted }: JournalCardProps) {
  const [showForm, setShowForm] = useState(false)
  const [symptom, setSymptom] = useState('')
  const [severity, setSeverity] = useState<SymptomEntry['severity']>('MILD')
  const [occurredAt, setOccurredAt] = useState(() => new Date().toISOString().slice(0, 16))
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')
  const [editingEntry, setEditingEntry] = useState<SymptomEntry | null>(null)
  const [deletingEntry, setDeletingEntry] = useState<SymptomEntry | null>(null)
  const [filterFrom, setFilterFrom] = useState('')
  const [filterTo, setFilterTo] = useState('')

  const filteredEntries = entries.filter((entry) => {
    const entryDate = new Date(entry.occurredAt).toISOString().slice(0, 10)
    if (filterFrom && entryDate < filterFrom) return false
    if (filterTo && entryDate > filterTo) return false
    return true
  })

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !symptom.trim()) return
    try {
      const payload = { symptom: symptom.trim(), severity, occurredAt: new Date(occurredAt).toISOString(), notes: notes.trim() || undefined }
      const entry = editingEntry ? await updateSymptom(token, editingEntry.id, payload) : await createSymptom(token, payload)
      if (editingEntry) onUpdated(entry); else onCreated(entry)
      setSymptom(''); setNotes(''); setSeverity('MILD'); setError(''); setEditingEntry(null); setShowForm(false)
    } catch { setError('Unable to save this journal entry.') }
  }

  return (
    <section className="panel" id="journal">
      <div className="panel-heading"><div><p className="section-label">Your wellbeing</p><h2>How are you feeling?</h2></div><span className="mood-mark">♡</span></div>
      <p className="panel-description">A small check-in can help you notice patterns over time.</p>
      <p className="journal-private-note">Private to your account only.</p>
      <div className="journal-safety-note"><strong>Safety note:</strong> This journal is for tracking patterns only and does not diagnose, treat, or replace medical care. Seek urgent help for severe symptoms or emergencies.</div>
      <div className="journal-safety-note secondary"><strong>Not a diagnosis:</strong> Symptom tracking is not a medical diagnostic tool and should not replace advice from your healthcare professional.</div>
      <div className="symptom-options">{symptoms.map((item) => <button className={selected === item ? 'selected' : ''} type="button" key={item} onClick={() => onSelect(item)}>{item}</button>)}</div>
      <button className="text-button" type="button" onClick={() => setShowForm((open) => !open)}>{showForm ? 'Close journal form' : 'Open my journal'} <span>{showForm ? '↑' : '→'}</span></button>
      {showForm && <form className="journal-form" onSubmit={submit}><label>Symptom or note<input required maxLength={80} value={symptom} onChange={(event) => setSymptom(event.target.value)} placeholder="Headache, tiredness, or a note" /></label><div className="form-row"><label>Severity<select value={severity} onChange={(event) => setSeverity(event.target.value as SymptomEntry['severity'])}><option value="MILD">Mild</option><option value="MODERATE">Moderate</option><option value="SEVERE">Severe</option></select></label><label>When<input required type="datetime-local" value={occurredAt} onChange={(event) => setOccurredAt(event.target.value)} /></label></div><label>Notes <span className="optional">Optional</span><textarea maxLength={1000} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="What would you like to remember?" /></label>{error && <p className="form-error">{error}</p>}<button className="primary-button" type="submit">{editingEntry ? 'Update entry' : 'Save entry'} <span>→</span></button></form>}
      <div className="date-filter-row"><label>From<input type="date" value={filterFrom} onChange={(event) => setFilterFrom(event.target.value)} /></label><label>To<input type="date" value={filterTo} onChange={(event) => setFilterTo(event.target.value)} /></label>{(filterFrom || filterTo) && <button type="button" className="outline-button small-button" onClick={() => { setFilterFrom(''); setFilterTo('') }}>Clear</button>}</div>
      {filteredEntries.length > 0 && <div className="journal-history">{filteredEntries.slice(0, 3).map((entry) => <article key={entry.id}><div><strong>{entry.symptom}</strong><span>{entry.severity.toLowerCase()} · {new Date(entry.occurredAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}</span></div><div className="journal-actions"><button type="button" aria-label="Edit journal entry" onClick={() => { setEditingEntry(entry); setSymptom(entry.symptom); setSeverity(entry.severity); setOccurredAt(new Date(entry.occurredAt).toISOString().slice(0, 16)); setNotes(entry.notes ?? ''); setShowForm(true) }}>Edit</button><button type="button" aria-label="Delete journal entry" onClick={() => setDeletingEntry(entry)}>×</button></div></article>)}</div>}
      {filteredEntries.length === 0 && entries.length > 0 && <p className="panel-description">No journal entries match the selected date range.</p>}
      {deletingEntry && <div className="confirm-overlay"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-symptom-title"><p className="section-label">Journal entry</p><h2 id="delete-symptom-title">Delete this entry?</h2><p>This removes the symptom note from your private journal history.</p><div className="confirm-actions"><button className="outline-button" type="button" onClick={() => setDeletingEntry(null)}>Cancel</button><button className="danger-button" type="button" onClick={async () => { const token = sessionStorage.getItem('maatricare-token'); if (!token) return; await deleteSymptom(token, deletingEntry.id); onDeleted(deletingEntry.id); setDeletingEntry(null) }}>Delete</button></div></section></div>}
    </section>
  )
}
