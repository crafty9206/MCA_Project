import { Droplets, Minus, Plus } from 'lucide-react'
import type { CSSProperties } from 'react'
import type { DailyWellbeing } from '../api'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type HydrationCardProps = {
  wellbeing: DailyWellbeing | null
  onChange: (glasses: number) => void
  disabled?: boolean
  language: LanguageCode
}

export function HydrationCard({ wellbeing, onChange, disabled = false, language }: HydrationCardProps) {
  const glasses = wellbeing?.waterGlasses ?? 0
  const goal = wellbeing?.waterGoal ?? 8
  const progress = Math.min(100, Math.round((glasses / goal) * 100))

  return (
    <section className="panel hydration-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'water.label')}</p><h2>{translate(language, 'water.title')}</h2></div><span className="hydration-icon"><Droplets aria-hidden="true" /></span></div>
      <div className="hydration-content"><div className="water-ring" style={{ '--water-progress': `${progress * 3.6}deg` } as CSSProperties}><strong>{glasses}</strong><span>{translate(language, 'water.of', { goal })}</span></div><div className="hydration-copy"><p>{glasses >= goal ? translate(language, 'water.reached') : translate(language, 'water.remaining', { count: goal - glasses })}</p><div className="hydration-controls"><button type="button" aria-label="Remove one glass" disabled={disabled || glasses === 0} onClick={() => onChange(glasses - 1)}><Minus aria-hidden="true" /></button><span>{progress}%</span><button type="button" aria-label="Add one glass" disabled={disabled || glasses >= 30} onClick={() => onChange(glasses + 1)}><Plus aria-hidden="true" /></button></div></div></div>
    </section>
  )
}
