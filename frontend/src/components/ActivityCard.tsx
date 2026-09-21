import { Footprints, Minus, Plus } from 'lucide-react'
import type { DailyWellbeing } from '../api'

type ActivityCardProps = { wellbeing: DailyWellbeing | null; onChange: (minutes: number) => void; disabled?: boolean }

export function ActivityCard({ wellbeing, onChange, disabled = false }: ActivityCardProps) {
  const minutes = wellbeing?.activityMinutes ?? 0
  const goal = wellbeing?.activityGoal ?? 20
  const progress = Math.min(100, Math.round((minutes / goal) * 100))
  return (
    <section className="panel activity-panel">
      <div className="panel-heading"><div><p className="section-label">Gentle movement</p><h2>Walking & activity</h2></div><span className="activity-icon"><Footprints aria-hidden="true" /></span></div>
      <p className="activity-summary">{minutes >= goal ? 'Daily movement goal reached.' : `${goal - minutes} minutes to your gentle movement goal.`}</p><div className="activity-controls"><button type="button" aria-label="Remove five activity minutes" disabled={disabled || minutes === 0} onClick={() => onChange(Math.max(0, minutes - 5))}><Minus aria-hidden="true" /></button><div><strong>{minutes} <small>min</small></strong><span>{progress}% of {goal} min</span></div><button type="button" aria-label="Add five activity minutes" disabled={disabled || minutes >= 300} onClick={() => onChange(Math.min(300, minutes + 5))}><Plus aria-hidden="true" /></button></div><p className="activity-note">Choose gentle movement that feels right for you and check with your healthcare professional before changing activity.</p>
    </section>
  )
}
