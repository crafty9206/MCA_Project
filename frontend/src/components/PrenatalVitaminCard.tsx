import { Check, Pill } from 'lucide-react'
import type { DailyWellbeing } from '../api'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type PrenatalVitaminCardProps = {
  wellbeing: DailyWellbeing | null
  onChange: (taken: boolean) => void
  disabled?: boolean
  language: LanguageCode
}

export function PrenatalVitaminCard({ wellbeing, onChange, disabled = false, language }: PrenatalVitaminCardProps) {
  const taken = wellbeing?.prenatalVitaminTaken ?? false

  return (
    <section className="panel vitamin-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'vitamin.label')}</p><h2>{translate(language, 'vitamin.title')}</h2></div><span className="vitamin-icon"><Pill aria-hidden="true" /></span></div>
      <button className={taken ? 'vitamin-toggle taken' : 'vitamin-toggle'} type="button" disabled={disabled} onClick={() => onChange(!taken)}><span className="vitamin-check">{taken && <Check aria-hidden="true" />}</span><span><strong>{translate(language, taken ? 'vitamin.taken' : 'vitamin.mark')}</strong><small>{translate(language, taken ? 'vitamin.done' : 'vitamin.hint')}</small></span></button>
    </section>
  )
}
