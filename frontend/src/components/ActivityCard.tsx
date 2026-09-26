import { Footprints, Minus, Plus } from 'lucide-react'
import type { DailyWellbeing } from '../api'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type ActivityCardProps = { wellbeing: DailyWellbeing | null; onChange: (minutes: number) => void; disabled?: boolean; language: LanguageCode }

export function ActivityCard({ wellbeing, onChange, disabled = false, language }: ActivityCardProps) {
  const minutes = wellbeing?.activityMinutes ?? 0
  const goal = wellbeing?.activityGoal ?? 20
  const progress = Math.min(100, Math.round((minutes / goal) * 100))
  return (
    <section className="panel activity-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'activity.label')}</p><h2>{translate(language, 'activity.title')}</h2></div><span className="activity-icon"><Footprints aria-hidden="true" /></span></div>
      <p className="activity-summary">{minutes >= goal ? translate(language, 'activity.reached') : translate(language, 'activity.remaining', { count: goal - minutes })}</p><div className="activity-controls"><button type="button" aria-label="Remove five activity minutes" disabled={disabled || minutes === 0} onClick={() => onChange(Math.max(0, minutes - 5))}><Minus aria-hidden="true" /></button><div><strong>{minutes} <small>{translate(language, 'activity.unit')}</small></strong><span>{progress}% / {goal}</span></div><button type="button" aria-label="Add five activity minutes" disabled={disabled || minutes >= 300} onClick={() => onChange(Math.min(300, minutes + 5))}><Plus aria-hidden="true" /></button></div><p className="activity-note">{translate(language, 'activity.note')}</p>
    </section>
  )
}
