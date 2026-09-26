import { useState } from 'react'
import type { FormEvent } from 'react'
import { ChevronDown, ClipboardList, Plus, Sparkles, Trash2, Utensils } from 'lucide-react'
import { analyzeLabValues } from '../api'
import type { LabGuideResponse, LabValue } from '../api'

type EditableLabValue = { id: number; testName: string; value: string; unit: string; referenceMin: string; referenceMax: string }

const emptyValue = (id: number): EditableLabValue => ({ id, testName: '', value: '', unit: '', referenceMin: '', referenceMax: '' })

export function LabReportGuide() {
  const [expanded, setExpanded] = useState(false)
  const [values, setValues] = useState<EditableLabValue[]>([emptyValue(1)])
  const [reportNotes, setReportNotes] = useState('')
  const [result, setResult] = useState<LabGuideResponse | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const updateValue = (id: number, field: keyof Omit<EditableLabValue, 'id'>, value: string) => {
    setValues((current) => current.map((item) => item.id === id ? { ...item, [field]: value } : item))
    setResult(null)
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    const payload: LabValue[] = values.map((item) => ({
      testName: item.testName.trim(), value: Number(item.value), unit: item.unit.trim(),
      referenceMin: Number(item.referenceMin), referenceMax: Number(item.referenceMax),
    }))
    if (payload.some((item) => item.referenceMin > item.referenceMax)) {
      setError('The minimum reference value must be less than or equal to the maximum.')
      return
    }
    setLoading(true)
    setError('')
    try { setResult(await analyzeLabValues(token, payload, reportNotes)) }
    catch { setError('The values could not be reviewed. Check every field and try again.') }
    finally { setLoading(false) }
  }

  return (
    <section className={`panel lab-overview-panel${expanded ? ' expanded' : ''}`}>
      <header className="lab-overview-heading"><span className="lab-icon"><Sparkles /></span><div><p className="section-label">AI lab education</p><h2>Understand key report values</h2><p>Compare values with your laboratory ranges and review food-based guidance.</p></div><button type="button" aria-expanded={expanded} onClick={() => setExpanded((open) => !open)}>{expanded ? 'Close' : 'Review lab results'} <ChevronDown className={expanded ? 'rotated' : ''} /></button></header>
      {!expanded ? <p className="lab-overview-note">Values are not saved. AI analysis uses only the details you submit.</p> : <div className="lab-guide-layout">
      <section className="panel lab-entry-panel">
        <div className="panel-heading"><div><p className="section-label">Report values</p><h2>Enter key test results</h2></div><span className="lab-icon"><ClipboardList /></span></div>
        <div className="lab-instructions"><strong>Use the range printed on your report</strong><p>Enter each test’s name, result, unit, minimum, and maximum exactly as shown. Different laboratories can use different ranges and units.</p></div>
        <form className="lab-form" onSubmit={submit}>
          {values.map((item, index) => <fieldset className="lab-row" key={item.id}><legend>Test {index + 1}</legend><label className="lab-test-name">Test name<input required maxLength={100} value={item.testName} onChange={(event) => updateValue(item.id, 'testName', event.target.value)} placeholder="Example: Vitamin C or Ferritin" /></label><label>Result<input required type="number" min="0" step="any" value={item.value} onChange={(event) => updateValue(item.id, 'value', event.target.value)} placeholder="0.2" /></label><label>Unit<input required maxLength={30} value={item.unit} onChange={(event) => updateValue(item.id, 'unit', event.target.value)} placeholder="mg/dL" /></label><label>Range minimum<input required type="number" min="0" step="any" value={item.referenceMin} onChange={(event) => updateValue(item.id, 'referenceMin', event.target.value)} placeholder="0.4" /></label><label>Range maximum<input required type="number" min="0" step="any" value={item.referenceMax} onChange={(event) => updateValue(item.id, 'referenceMax', event.target.value)} placeholder="2.0" /></label>{values.length > 1 && <button className="lab-remove" type="button" aria-label={`Remove test ${index + 1}`} onClick={() => setValues((current) => current.filter((value) => value.id !== item.id))}><Trash2 /></button>}</fieldset>)}
          <button className="lab-add" type="button" disabled={values.length >= 20} onClick={() => setValues((current) => [...current, emptyValue(Date.now())])}><Plus /> Add another result</button>
          <label className="lab-report-notes">Report key points <span>Optional</span><textarea maxLength={5000} rows={4} value={reportNotes} onChange={(event) => { setReportNotes(event.target.value); setResult(null) }} placeholder="Paste relevant comments or key points from the report. Remove your name, address, report ID, and other identifiers." /><small>This text is sent only to your locally configured AI model and is not stored by MaatriCare.</small></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="primary-button" type="submit" disabled={loading}>{loading ? 'Reviewing…' : 'Review values'}</button>
        </form>
      </section>

      <section className="lab-results" aria-live="polite">
        {!result ? <div className="lab-empty"><Utensils /><strong>Food guidance will appear here</strong><p>The app only compares against the range you enter. It does not diagnose a deficiency.</p></div> : <><article className="lab-ai-summary"><span><Sparkles /> {result.aiSource === 'LOCAL_OLLAMA' ? 'Local AI explanation' : 'AI unavailable'}</span><p>{result.aiSummary}</p></article><div className="lab-result-list">{result.results.map((item, index) => <article className={`lab-result ${item.status.toLowerCase().replace('_', '-')}`} key={`${item.testName}-${index}`}><header><div><span>{item.status.replace('_', ' ')}</span><h3>{item.testName}</h3></div><strong>{item.value} {item.unit}</strong></header><p>{item.message}</p>{item.foodSources.length > 0 && <div className="lab-foods"><b>General food sources of {item.nutrient}</b><ul>{item.foodSources.map((food) => <li key={food}>{food}</li>)}</ul></div>}<small>Report range: {item.referenceMin}–{item.referenceMax} {item.unit}</small></article>)}</div><p className="lab-disclaimer">{result.disclaimer}</p></>}
      </section>
    </div>}
    </section>
  )
}