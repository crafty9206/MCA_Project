import type { PregnancySummary } from '../api'

type PregnancyStatusProps = { pregnancy: PregnancySummary }

const dateFormat = new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })

export function PregnancyStatus({ pregnancy }: PregnancyStatusProps) {
  const progress = Math.min(100, Math.round((pregnancy.currentWeek / 40) * 100))
  return (
    <section className="status-card">
      <div className="status-copy"><div className="section-label">Your pregnancy journey</div><div className="week-line"><strong>Week {pregnancy.currentWeek}</strong><span>{pregnancy.trimester}</span></div><p>Take it one beautiful day at a time.</p><div className="progress-track"><span style={{ width: `${progress}%` }} /></div><div className="progress-meta"><span>{progress}% complete</span><span>{pregnancy.weeksRemaining} weeks to go</span></div></div>
      <div className="journey-visual" aria-hidden="true"><div className="orbit orbit-one" /><div className="orbit orbit-two" /><div className="baby-icon">♡</div><span className="sparkle one">✦</span><span className="sparkle two">✦</span></div>
      <div className="due-date"><span className="date-icon">▣</span><div><span>Estimated due date</span><strong>{dateFormat.format(new Date(`${pregnancy.dueDate}T00:00:00`))}</strong><span>Based on your profile details</span></div></div>
    </section>
  )
}
