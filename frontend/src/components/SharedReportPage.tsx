import { useEffect, useState } from 'react'
import { FileWarning, HeartPulse, Printer } from 'lucide-react'
import { getSharedReport } from '../api'
import type { HealthcareReport } from '../api'

const dateFormat = new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
const dateTimeFormat = new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit' })

export function SharedReportPage({ token }: { token: string }) {
  const [report, setReport] = useState<HealthcareReport | null>(null)
  const [unavailable, setUnavailable] = useState(false)

  useEffect(() => {
    getSharedReport(token).then(setReport).catch(() => setUnavailable(true))
  }, [token])

  if (unavailable) return <main className="shared-report-page"><div className="shared-report-state"><FileWarning /><h1>Report unavailable</h1><p>This link has expired, was revoked, or is invalid.</p></div></main>
  if (!report) return <main className="shared-report-page"><div className="shared-report-state"><HeartPulse /><p>Loading shared report…</p></div></main>

  return <main className="shared-report-page"><article className="report-document shared-report-document" id="healthcare-report">
    <header><div><p>MaatriCare · Shared report</p><h2>Pregnancy summary</h2><span>Generated {dateTimeFormat.format(new Date(report.generatedAt))}</span></div><div className="report-actions"><button type="button" onClick={() => window.print()}><Printer /><span>Print / PDF</span></button></div></header>
    <section className="report-person"><div><span>Name</span><strong>{report.person.displayName}</strong></div><div><span>Email</span><strong>{report.person.email}</strong></div><div><span>Pregnancy</span><strong>Week {report.pregnancy.currentWeek} · {report.pregnancy.trimester}</strong></div><div><span>Due date</span><strong>{dateFormat.format(new Date(`${report.pregnancy.dueDate}T00:00:00`))}</strong></div></section>
    <section><h3>Tracking summary · last 30 days</h3><div className="report-metrics"><div><strong>{report.tracking.completionPercentage}%</strong><span>Tasks completed</span></div><div><strong>{report.tracking.averageWaterGlasses}</strong><span>Average glasses</span></div><div><strong>{report.tracking.averageActivityMinutes}</strong><span>Average activity min</span></div><div><strong>{report.tracking.averageSleepHours}</strong><span>Average sleep hours</span></div></div></section>
    <section><h3>Appointments</h3>{report.appointments.length === 0 ? <p className="report-muted">No appointments recorded.</p> : <div className="report-list">{report.appointments.map((appointment, index) => <div key={`${appointment.startsAt}-${index}`}><strong>{appointment.title}</strong><span>{dateTimeFormat.format(new Date(appointment.startsAt))}{appointment.providerName ? ` · ${appointment.providerName}` : ''}{appointment.clinicName ? ` · ${appointment.clinicName}` : ''}</span>{appointment.notes && <p>{appointment.notes}</p>}</div>)}</div>}</section>
    <section><h3>Milestones</h3><div className="report-list compact">{report.milestones.map((milestone) => <div key={`${milestone.weekNumber}-${milestone.title}`}><strong>{milestone.completed ? 'Completed' : 'Planned'} · Week {milestone.weekNumber}</strong><span>{milestone.title}</span></div>)}</div></section>
    {report.journalIncluded && <section><h3>Private symptom and journal history</h3><div className="report-list">{report.journal.map((entry, index) => <div key={`${entry.occurredAt}-${index}`}><strong>{entry.symptom} · {entry.severity.toLowerCase()}</strong><span>{dateTimeFormat.format(new Date(entry.occurredAt))}</span>{entry.notes && <p>{entry.notes}</p>}</div>)}</div></section>}
    <footer>{report.disclaimer}</footer>
  </article></main>
}