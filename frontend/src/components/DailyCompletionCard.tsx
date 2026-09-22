import { CheckCircle2 } from 'lucide-react'
import type { CSSProperties } from 'react'
import type { CareTask, DailyWellbeing } from '../api'

type DailyCompletionCardProps = { tasks: CareTask[]; wellbeing: DailyWellbeing | null }

export function DailyCompletionCard({ tasks, wellbeing }: DailyCompletionCardProps) {
  const taskScore = tasks.length === 0 ? 0 : (tasks.filter((task) => task.completed).length / tasks.length) * 100
  const waterScore = Math.min(100, ((wellbeing?.waterGlasses ?? 0) / (wellbeing?.waterGoal ?? 8)) * 100)
  const vitaminScore = wellbeing?.prenatalVitaminTaken ? 100 : 0
  const activityScore = Math.min(100, ((wellbeing?.activityMinutes ?? 0) / (wellbeing?.activityGoal ?? 20)) * 100)
  const sleepScore = Math.min(100, ((wellbeing?.sleepHours ?? 0) / (wellbeing?.sleepGoal ?? 8)) * 100)
  const completion = Math.round((taskScore + waterScore + vitaminScore + activityScore + sleepScore) / 5)

  return (
    <section className="panel completion-panel">
      <div className="panel-heading"><div><p className="section-label">Today at a glance</p><h2>Daily completion</h2></div><span className="completion-icon"><CheckCircle2 aria-hidden="true" /></span></div>
      <div className="completion-main"><div className="completion-ring" style={{ '--completion-progress': `${completion * 3.6}deg` } as CSSProperties}><strong>{completion}%</strong><span>complete</span></div><p>{completion >= 80 ? 'A strong day of caring for yourself.' : completion >= 40 ? 'You are building a thoughtful rhythm.' : 'Every small step still counts today.'}</p></div>
      <div className="completion-breakdown"><span>Tasks <b>{Math.round(taskScore)}%</b></span><span>Water <b>{Math.round(waterScore)}%</b></span><span>Vitamins <b>{Math.round(vitaminScore)}%</b></span><span>Movement <b>{Math.round(activityScore)}%</b></span><span>Sleep <b>{Math.round(sleepScore)}%</b></span></div>
    </section>
  )
}
