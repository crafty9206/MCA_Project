import { translate } from '../i18n'
import type { LanguageCode } from '../i18n'

export function ExerciseCard({ language }: { language: LanguageCode }) {
  const exercises = [
    ['01', translate(language, 'exercise.first'), translate(language, 'exercise.firstDesc')],
    ['02', translate(language, 'exercise.second'), translate(language, 'exercise.secondDesc')],
    ['03', translate(language, 'exercise.third'), translate(language, 'exercise.thirdDesc')],
  ]
  return (
    <section className="panel exercise-panel">
      <div className="panel-heading"><div><p className="section-label">{translate(language, 'exercise.label')}</p><h2>{translate(language, 'exercise.title')}</h2></div><span className="exercise-icon">⌁</span></div>
      <div className="exercise-list">{exercises.map(([number, trimester, description], index) => <button key={trimester} type="button" className={index === 1 ? 'exercise active-exercise' : 'exercise'}><span>{number}</span><div><b>{trimester}</b><small>{description}</small></div><i>→</i></button>)}</div>
      <p className="exercise-note">{translate(language, 'exercise.note')}</p>
    </section>
  )
}
