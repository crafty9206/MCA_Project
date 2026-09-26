import type { PregnancySummary } from '../api'
import { localeFor, translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type PregnancyStatusProps = { pregnancy: PregnancySummary; language: LanguageCode }

export function PregnancyStatus({ pregnancy, language }: PregnancyStatusProps) {
  const progress = Math.min(100, Math.round((pregnancy.currentWeek / 40) * 100))
  const dateFormat = new Intl.DateTimeFormat(localeFor(language), { day: 'numeric', month: 'long', year: 'numeric' })
  return (
    <section className="status-card">
      <div className="status-copy"><div className="section-label">{translate(language, 'pregnancy.label')}</div><div className="week-line"><strong>{translate(language, 'pregnancy.week')} {pregnancy.currentWeek}</strong><span>{pregnancy.trimester}</span></div><div className="progress-track"><span style={{ width: `${progress}%` }} /></div><div className="progress-meta"><span>{progress}%</span><span>{pregnancy.weeksRemaining} {translate(language, 'pregnancy.remaining')}</span></div></div>
      <div className="journey-visual" aria-hidden="true"><div className="orbit orbit-one" /><div className="orbit orbit-two" /><div className="baby-icon">♡</div><span className="sparkle one">✦</span><span className="sparkle two">✦</span></div>
      <div className="due-date"><span className="date-icon">▣</span><div><span>{translate(language, 'pregnancy.due')}</span><strong>{dateFormat.format(new Date(`${pregnancy.dueDate}T00:00:00`))}</strong><span>{translate(language, 'pregnancy.based')}</span></div></div>
    </section>
  )
}
