import { BedDouble, Minus, Plus } from 'lucide-react'
import type { DailyWellbeing } from '../api'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type SleepCardProps = { wellbeing: DailyWellbeing | null; onChange: (hours: number) => void; disabled?: boolean; language: LanguageCode }

export function SleepCard({ wellbeing, onChange, disabled = false, language }: SleepCardProps) {
  const hours = wellbeing?.sleepHours ?? 0
  const goal = wellbeing?.sleepGoal ?? 8
  const progress = Math.min(100, Math.round((hours / goal) * 100))
  return (
    <section className="panel sleep-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'sleep.label')}</p><h2>{translate(language, 'sleep.title')}</h2></div><span className="sleep-icon"><BedDouble aria-hidden="true" /></span></div>
      <p className="sleep-summary">{hours >= goal ? translate(language, 'sleep.reached') : translate(language, 'sleep.remaining', { count: Math.max(0, goal - hours).toFixed(1) })}</p><div className="sleep-controls"><button type="button" aria-label="Remove thirty minutes" disabled={disabled || hours === 0} onClick={() => onChange(Math.max(0, Number((hours - 0.5).toFixed(1))))}><Minus aria-hidden="true" /></button><div><strong>{hours.toFixed(1)} <small>{translate(language, 'sleep.unit')}</small></strong><span>{progress}% / {goal}</span></div><button type="button" aria-label="Add thirty minutes" disabled={disabled || hours >= 24} onClick={() => onChange(Math.min(24, Number((hours + 0.5).toFixed(1))))}><Plus aria-hidden="true" /></button></div>
    </section>
  )
}
