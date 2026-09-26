import { useEffect, useState } from 'react'
import { Check, Copy, Download, FileText, Link2, Printer, ShieldCheck, Trash2 } from 'lucide-react'
import { createReportShare, getHealthcareReport, getReportShares, revokeReportShare } from '../api'
import type { HealthcareReport as HealthcareReportData, ReportShare } from '../api'
import { localeFor } from '../i18n'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

export function HealthcareReport({ language }: { language: LanguageCode }) {
  const dateFormat = new Intl.DateTimeFormat(localeFor(language), { day: 'numeric', month: 'short', year: 'numeric' })
  const dateTimeFormat = new Intl.DateTimeFormat(localeFor(language), { day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit' })
  const [includeJournal, setIncludeJournal] = useState(false)
  const [report, setReport] = useState<HealthcareReportData | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [expiresInHours, setExpiresInHours] = useState(24)
  const [shares, setShares] = useState<ReportShare[]>([])
  const [shareUrl, setShareUrl] = useState('')
  const [copied, setCopied] = useState(false)

  const loadShares = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (token) try { setShares(await getReportShares(token)) } catch { /* Report preview remains available. */ }
  }

  useEffect(() => { void loadShares() }, [])

  const generate = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setLoading(true)
    setError('')
    try { setReport(await getHealthcareReport(token, includeJournal)) }
    catch { setError('Your report could not be generated. Please try again.') }
    finally { setLoading(false) }
  }

  const download = () => {
    if (!report) return
    const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `maatricare-summary-${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    URL.revokeObjectURL(link.href)
  }

  const createShare = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || !report) return
    setLoading(true)
    setError('')
    try {
      const share = await createReportShare(token, report.journalIncluded, expiresInHours)
      setShareUrl(`${window.location.origin}${window.location.pathname}?share=${share.token}`)
      await loadShares()
    } catch { setError('The secure link could not be created.') }
    finally { setLoading(false) }
  }

  const copyShare = async () => {
    await navigator.clipboard.writeText(shareUrl)
    setCopied(true)
    window.setTimeout(() => setCopied(false), 1800)
  }

  return (
    <section className="report-workspace">
      <aside className="panel report-controls">
        <span className="report-control-icon" aria-hidden="true"><ShieldCheck /></span>
        <h2>{translate(language, 'report.choose')}</h2>
        <p>{translate(language, 'report.description')}</p>
        <label className="preference-row report-consent"><span><strong>{translate(language, 'report.journal')}</strong><small>{translate(language, 'journal.private')}</small></span><input type="checkbox" checked={includeJournal} onChange={(event) => { setIncludeJournal(event.target.checked); setReport(null) }} /></label>
        <button className="primary-button" type="button" disabled={loading} onClick={generate}>{loading ? '…' : translate(language, 'report.generate')}</button>
        {report && <div className="report-share-create"><label>Link expires<select value={expiresInHours} onChange={(event) => setExpiresInHours(Number(event.target.value))}><option value={1}>In 1 hour</option><option value={24}>In 24 hours</option><option value={72}>In 3 days</option><option value={168}>In 7 days</option></select></label><button className="outline-button" type="button" disabled={loading} onClick={createShare}><Link2 /> Create secure link</button></div>}
        {shareUrl && <div className="report-share-url"><input aria-label="Secure share URL" readOnly value={shareUrl} /><button type="button" aria-label="Copy secure share URL" onClick={copyShare}>{copied ? <Check /> : <Copy />}</button></div>}
        <small className="report-privacy-note">{translate(language, 'report.privacy')}</small>
        {error && <p className="form-error" role="alert">{error}</p>}
        {shares.length > 0 && <div className="report-share-list"><h3>Recent links</h3>{shares.slice(0, 5).map((share) => <div key={share.id}><span><strong>{share.status}</strong><small>Expires {dateTimeFormat.format(new Date(share.expiresAt))} · {share.accessCount} views{share.journalIncluded ? ' · Journal included' : ''}</small></span>{share.status === 'ACTIVE' && <button type="button" aria-label="Revoke shared link" onClick={async () => { const token = sessionStorage.getItem('maatricare-token'); if (!token) return; await revokeReportShare(token, share.id); await loadShares() }}><Trash2 /></button>}</div>)}</div>}
      </aside>

      <div className="report-preview-wrap">
        {!report ? <div className="report-empty"><FileText /><strong>{translate(language, 'report.empty')}</strong><span>{translate(language, 'report.generate')}</span></div> : (
          <article className="report-document" id="healthcare-report">
            <header><div><p>MaatriCare</p><h2>Pregnancy summary</h2><span>Generated {dateTimeFormat.format(new Date(report.generatedAt))}</span></div><div className="report-actions"><button type="button" onClick={download}><Download /> <span>JSON</span></button><button type="button" onClick={() => window.print()}><Printer /> <span>Print / PDF</span></button></div></header>
            <section className="report-person"><div><span>Name</span><strong>{report.person.displayName}</strong></div><div><span>Email</span><strong>{report.person.email}</strong></div><div><span>Pregnancy</span><strong>Week {report.pregnancy.currentWeek} · {report.pregnancy.trimester}</strong></div><div><span>Due date</span><strong>{dateFormat.format(new Date(`${report.pregnancy.dueDate}T00:00:00`))}</strong></div></section>
            <section><h3>Profile details</h3><dl className="report-details"><div><dt>Age</dt><dd>{report.pregnancy.ageYears}</dd></div><div><dt>Blood group</dt><dd>{report.pregnancy.bloodGroup || 'Not recorded'}</dd></div><div><dt>Blood pressure</dt><dd>{report.pregnancy.bloodPressure || 'Not recorded'}</dd></div><div><dt>Pre-pregnancy weight</dt><dd>{report.pregnancy.prePregnancyWeightKg ? `${report.pregnancy.prePregnancyWeightKg} kg` : 'Not recorded'}</dd></div></dl></section>
            <section><h3>Tracking summary · last 30 days</h3><div className="report-metrics"><div><strong>{report.tracking.completionPercentage}%</strong><span>Tasks completed</span></div><div><strong>{report.tracking.averageWaterGlasses}</strong><span>Average glasses</span></div><div><strong>{report.tracking.averageActivityMinutes}</strong><span>Average activity min</span></div><div><strong>{report.tracking.averageSleepHours}</strong><span>Average sleep hours</span></div></div></section>
            <section><h3>Appointments</h3>{report.appointments.length === 0 ? <p className="report-muted">No appointments recorded.</p> : <div className="report-list">{report.appointments.map((appointment, index) => <div key={`${appointment.startsAt}-${index}`}><strong>{appointment.title}</strong><span>{dateTimeFormat.format(new Date(appointment.startsAt))}{appointment.providerName ? ` · ${appointment.providerName}` : ''}{appointment.clinicName ? ` · ${appointment.clinicName}` : ''}</span>{appointment.notes && <p>{appointment.notes}</p>}</div>)}</div>}</section>
            <section><h3>Milestones</h3>{report.milestones.length === 0 ? <p className="report-muted">No milestones recorded.</p> : <div className="report-list compact">{report.milestones.map((milestone) => <div key={`${milestone.weekNumber}-${milestone.title}`}><strong>{milestone.completed ? 'Completed' : 'Planned'} · Week {milestone.weekNumber}</strong><span>{milestone.title}</span></div>)}</div>}</section>
            {report.journalIncluded && <section><h3>Private symptom and journal history</h3>{report.journal.length === 0 ? <p className="report-muted">No journal entries recorded.</p> : <div className="report-list">{report.journal.map((entry, index) => <div key={`${entry.occurredAt}-${index}`}><strong>{entry.symptom} · {entry.severity.toLowerCase()}</strong><span>{dateTimeFormat.format(new Date(entry.occurredAt))}</span>{entry.notes && <p>{entry.notes}</p>}</div>)}</div>}</section>}
            <footer>{report.disclaimer}</footer>
          </article>
        )}
      </div>
    </section>
  )
}