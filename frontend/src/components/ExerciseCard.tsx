const exercises = [
  ['01', 'First trimester', 'Breathing, easy walks & gentle mobility'],
  ['02', 'Second trimester', 'Prenatal yoga, walking & pelvic floor'],
  ['03', 'Third trimester', 'Stretching, posture & relaxation'],
]

export function ExerciseCard() {
  return (
    <section className="panel exercise-panel">
      <div className="panel-heading"><div><p className="section-label">Move with confidence</p><h2>Pregnancy-safe movement</h2></div><span className="exercise-icon">⌁</span></div>
      <div className="exercise-list">{exercises.map(([number, trimester, description], index) => <button key={trimester} type="button" className={index === 1 ? 'exercise active-exercise' : 'exercise'}><span>{number}</span><div><b>{trimester}</b><small>{description}</small></div><i>→</i></button>)}</div>
      <p className="exercise-note">Always check with your healthcare professional before starting or changing exercise.</p>
    </section>
  )
}
