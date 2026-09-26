import { CheckCircle2 } from 'lucide-react'
import type { CSSProperties } from 'react'
import type { CareTask, DailyWellbeing } from '../api'
import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type DailyCompletionCardProps = { tasks: CareTask[]; wellbeing: DailyWellbeing | null; language: LanguageCode }

export function DailyCompletionCard({ tasks, wellbeing, language }: DailyCompletionCardProps) {
  const taskScore = tasks.length === 0 ? 0 : (tasks.filter((task) => task.completed).length / tasks.length) * 100
  const waterScore = Math.min(100, ((wellbeing?.waterGlasses ?? 0) / (wellbeing?.waterGoal ?? 8)) * 100)
  const vitaminScore = wellbeing?.prenatalVitaminTaken ? 100 : 0
  const activityScore = Math.min(100, ((wellbeing?.activityMinutes ?? 0) / (wellbeing?.activityGoal ?? 20)) * 100)
  const sleepScore = Math.min(100, ((wellbeing?.sleepHours ?? 0) / (wellbeing?.sleepGoal ?? 8)) * 100)
  const completion = Math.round((taskScore + waterScore + vitaminScore + activityScore + sleepScore) / 5)

  return (
    <section className="panel completion-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'completion.label')}</p><h2>{translate(language, 'completion.title')}</h2></div><span className="completion-icon"><CheckCircle2 aria-hidden="true" /></span></div>
      <div className="completion-main"><div className="completion-ring" style={{ '--completion-progress': `${completion * 3.6}deg` } as CSSProperties}><strong>{completion}%</strong><span>{translate(language, 'completion.complete')}</span></div><p>{translate(language, completion >= 80 ? 'completion.high' : completion >= 40 ? 'completion.medium' : 'completion.low')}</p></div>
      <div className="completion-breakdown"><span>{translate(language, 'metric.tasks')} <b>{Math.round(taskScore)}%</b></span><span>{translate(language, 'metric.water')} <b>{Math.round(waterScore)}%</b></span><span>{translate(language, 'metric.vitamins')} <b>{Math.round(vitaminScore)}%</b></span><span>{translate(language, 'metric.movement')} <b>{Math.round(activityScore)}%</b></span><span>{translate(language, 'metric.sleep')} <b>{Math.round(sleepScore)}%</b></span></div>
    </section>
  )
}
