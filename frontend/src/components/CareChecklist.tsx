type CareChecklistProps = {
  completed: boolean[]
  onToggle: (index: number) => void
}

const tasks = [['Take prenatal vitamins', 'Morning routine'], ['Drink 8 glasses of water', '0 of 8 logged'], ['Take a 20 minute walk', 'Move gently today']]

export function CareChecklist({ completed, onToggle }: CareChecklistProps) {
  return (
    <section className="panel checklist-panel">
      <div className="panel-heading"><div><p className="section-label">Your daily rhythm</p><h2>Today’s care</h2></div><span className="task-count">{completed.filter(Boolean).length} of {tasks.length} complete</span></div>
      <div className="task-list">{tasks.map(([task, detail], index) => <button className={`task ${completed[index] ? 'done' : ''}`} key={task} type="button" onClick={() => onToggle(index)}><span className="check">{completed[index] ? '✓' : ''}</span><span className="task-copy"><b>{task}</b><small>{completed[index] ? 'Completed' : detail}</small></span><span className="task-arrow">›</span></button>)}</div>
      <button className="text-button" type="button">View all activities <span>→</span></button>
    </section>
  )
}
