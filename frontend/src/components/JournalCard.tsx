import { useState } from 'react'
import type { FormEvent } from 'react'
import { CalendarRange, RotateCcw } from 'lucide-react'
import { createSymptom, deleteSymptom, updateSymptom } from '../api'
import type { SymptomEntry } from '../api'
import { localeFor } from '../i18n'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

const symptoms = ['Feeling well', 'Tired', 'Nauseous', 'Other']
type DateFilter = 'ALL' | '7_DAYS' | '30_DAYS' | 'CUSTOM'
type JournalCardProps = { selected: string | null; onSelect: (symptom: string) => void; entries: SymptomEntry[]; onCreated: (entry: SymptomEntry) => void; onUpdated: (entry: SymptomEntry) => void; onDeleted: (id: string) => void; language: LanguageCode }

const localDateKey = (value: string | Date) => {
  const date = value instanceof Date ? value : new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

export function JournalCard({ selected, onSelect, entries, onCreated, onUpdated, onDeleted, language }: JournalCardProps) {
  const [showForm, setShowForm] = useState(false)
  const [symptom, setSymptom] = useState('')
  const [severity, setSeverity] = useState<SymptomEntry['severity']>('MILD')
  const [occurredAt, setOccurredAt] = useState(() => new Date().toISOString().slice(0, 16))
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')
  const [editingEntry, setEditingEntry] = useState<SymptomEntry | null>(null)
  const [deletingEntry, setDeletingEntry] = useState<SymptomEntry | null>(null)
  const [dateFilter, setDateFilter] = useState<DateFilter>('ALL')
  const [filterFrom, setFilterFrom] = useState('')
  const [filterTo, setFilterTo] = useState('')

  const today = new Date()
  const presetStart = new Date(today)
  presetStart.setDate(today.getDate() - (dateFilter === '7_DAYS' ? 6 : 29))
  const activeFrom = dateFilter === 'CUSTOM' ? filterFrom : dateFilter === 'ALL' ? '' : localDateKey(presetStart)
  const activeTo = dateFilter === 'CUSTOM' ? filterTo : dateFilter === 'ALL' ? '' : localDateKey(today)
  const invalidRange = dateFilter === 'CUSTOM' && Boolean(activeFrom && activeTo && activeFrom > activeTo)
  const filteredEntries = entries.filter((entry) => {
    if (invalidRange) return false
    const entryDate = localDateKey(entry.occurredAt)
    if (activeFrom && entryDate < activeFrom) return false
    if (activeTo && entryDate > activeTo) return false
    return true
  })

  const resetDateFilter = () => {
    setDateFilter('ALL')
    setFilterFrom('')
    setFilterTo('')
  }

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
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'journal.label')}</p><h2>{translate(language, 'journal.title')}</h2></div><span className="mood-mark">♡</span></div>
      <p className="panel-description">{translate(language, 'journal.description')}</p>
      <p className="journal-private-note">{translate(language, 'journal.private')}</p>
      <div className="journal-safety-note"><strong>Safety:</strong> {translate(language, 'journal.safety')}</div>
      <div className="journal-safety-note secondary"><strong>Not a diagnosis:</strong> Symptom tracking is not a medical diagnostic tool and should not replace advice from your healthcare professional.</div>
      <div className="symptom-options">{symptoms.map((item) => <button className={selected === item ? 'selected' : ''} type="button" key={item} onClick={() => onSelect(item)}>{item}</button>)}</div>
      <button className="text-button" type="button" onClick={() => setShowForm((open) => !open)}>{translate(language, showForm ? 'journal.close' : 'journal.open')} <span>{showForm ? '↑' : '→'}</span></button>
      {showForm && <form className="journal-form" onSubmit={submit}><label>Symptom or note<input required maxLength={80} value={symptom} onChange={(event) => setSymptom(event.target.value)} placeholder="Headache, tiredness, or a note" /></label><div className="form-row"><label>Severity<select value={severity} onChange={(event) => setSeverity(event.target.value as SymptomEntry['severity'])}><option value="MILD">Mild</option><option value="MODERATE">Moderate</option><option value="SEVERE">Severe</option></select></label><label>When<input required type="datetime-local" value={occurredAt} onChange={(event) => setOccurredAt(event.target.value)} /></label></div><label>Notes <span className="optional">Optional</span><textarea maxLength={1000} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="What would you like to remember?" /></label>{error && <p className="form-error">{error}</p>}<button className="primary-button" type="submit">{editingEntry ? 'Update entry' : 'Save entry'} <span>→</span></button></form>}
      <section className="journal-filter" aria-labelledby="journal-filter-title">
        <div className="journal-filter-heading"><span className="journal-filter-icon" aria-hidden="true"><CalendarRange /></span><div><strong id="journal-filter-title">Filter journal history</strong><small>Choose a quick period or set your own dates.</small></div>{dateFilter !== 'ALL' && <button type="button" aria-label="Reset date filter" title="Reset date filter" onClick={resetDateFilter}><RotateCcw /></button>}</div>
        <div className="journal-filter-presets" aria-label="Journal date range">
          {([['ALL', 'All entries'], ['7_DAYS', 'Last 7 days'], ['30_DAYS', 'Last 30 days'], ['CUSTOM', 'Custom range']] as const).map(([value, label]) => <button key={value} type="button" className={dateFilter === value ? 'active' : ''} aria-pressed={dateFilter === value} onClick={() => setDateFilter(value)}>{label}</button>)}
        </div>
        {dateFilter === 'CUSTOM' && <div className="date-filter-row"><label>Start date<input type="date" value={filterFrom} max={filterTo || undefined} onChange={(event) => setFilterFrom(event.target.value)} /></label><label>End date<input type="date" value={filterTo} min={filterFrom || undefined} max={localDateKey(today)} onChange={(event) => setFilterTo(event.target.value)} /></label></div>}
        {invalidRange && <p className="form-error" role="alert">Start date must be before the end date.</p>}
        <p className="journal-filter-result">Showing <strong>{filteredEntries.length}</strong> of {entries.length} entries{activeFrom || activeTo ? ` · ${activeFrom || 'earliest'} to ${activeTo || 'today'}` : ''}</p>
      </section>
      {filteredEntries.length > 0 && <div className="journal-history">{filteredEntries.map((entry) => <article key={entry.id}><div><strong>{entry.symptom}</strong><span>{entry.severity.toLowerCase()} · {new Date(entry.occurredAt).toLocaleDateString(localeFor(language), { day: 'numeric', month: 'short' })}</span></div><div className="journal-actions"><button type="button" aria-label="Edit journal entry" onClick={() => { setEditingEntry(entry); setSymptom(entry.symptom); setSeverity(entry.severity); setOccurredAt(new Date(entry.occurredAt).toISOString().slice(0, 16)); setNotes(entry.notes ?? ''); setShowForm(true) }}>Edit</button><button type="button" aria-label="Delete journal entry" onClick={() => setDeletingEntry(entry)}>×</button></div></article>)}</div>}
      {!invalidRange && filteredEntries.length === 0 && entries.length > 0 && <p className="journal-filter-empty">No entries were recorded in this period. Try another range.</p>}
      {deletingEntry && <div className="confirm-overlay"><section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-symptom-title"><p className="section-label">Journal entry</p><h2 id="delete-symptom-title">Delete this entry?</h2><p>This removes the symptom note from your private journal history.</p><div className="confirm-actions"><button className="outline-button" type="button" onClick={() => setDeletingEntry(null)}>Cancel</button><button className="danger-button" type="button" onClick={async () => { const token = sessionStorage.getItem('maatricare-token'); if (!token) return; await deleteSymptom(token, deletingEntry.id); onDeleted(deletingEntry.id); setDeletingEntry(null) }}>Delete</button></div></section></div>}
    </section>
  )
}
