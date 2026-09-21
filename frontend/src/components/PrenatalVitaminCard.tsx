import { Check, Pill } from 'lucide-react'
import type { DailyWellbeing } from '../api'

type PrenatalVitaminCardProps = {
  wellbeing: DailyWellbeing | null
  onChange: (taken: boolean) => void
  disabled?: boolean
}

export function PrenatalVitaminCard({ wellbeing, onChange, disabled = false }: PrenatalVitaminCardProps) {
  const taken = wellbeing?.prenatalVitaminTaken ?? false

  return (
    <section className="panel vitamin-panel">
      <div className="panel-heading"><div><p className="section-label">Daily care</p><h2>Prenatal vitamin</h2></div><span className="vitamin-icon"><Pill aria-hidden="true" /></span></div>
      <button className={taken ? 'vitamin-toggle taken' : 'vitamin-toggle'} type="button" disabled={disabled} onClick={() => onChange(!taken)}><span className="vitamin-check">{taken && <Check aria-hidden="true" />}</span><span><strong>{taken ? 'Taken today' : 'Mark as taken'}</strong><small>{taken ? 'Nice work keeping your routine.' : 'Tap when you have taken it.'}</small></span></button>
    </section>
  )
}
