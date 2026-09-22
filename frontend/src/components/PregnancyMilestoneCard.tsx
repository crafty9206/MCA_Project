import { useMemo, useState } from 'react'
import { Check } from 'lucide-react'
import type { PregnancyMilestone } from '../api'

type Props = {
  milestones: PregnancyMilestone[]
  onRefresh: () => Promise<void>
}

export function PregnancyMilestoneCard({ milestones, onRefresh }: Props) {
  const [expanded, setExpanded] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [togglingId, setTogglingId] = useState<string | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [weekNumber, setWeekNumber] = useState('20')
  const [error, setError] = useState('')

  const visibleMilestones = useMemo(() => {
    return [...milestones].sort((a, b) => a.weekNumber - b.weekNumber)
  }, [milestones])

  const activeMilestones = visibleMilestones.filter((milestone) => !milestone.completed)
  const completedCount = visibleMilestones.length - activeMilestones.length
  const currentTrimester = visibleMilestones.length > 0 ? (activeMilestones[0]?.weekNumber ?? visibleMilestones[0].weekNumber) < 14 ? 'First trimester' : (activeMilestones[0]?.weekNumber ?? visibleMilestones[0].weekNumber) < 28 ? 'Second trimester' : 'Third trimester' : 'Trimester overview'
  const trimesterGroups = [
    { label: 'First trimester', weeks: [1, 13], milestones: visibleMilestones.filter((milestone) => milestone.weekNumber <= 13) },
    { label: 'Second trimester', weeks: [14, 27], milestones: visibleMilestones.filter((milestone) => milestone.weekNumber >= 14 && milestone.weekNumber <= 27) },
    { label: 'Third trimester', weeks: [28, 42], milestones: visibleMilestones.filter((milestone) => milestone.weekNumber >= 28) },
  ].filter((group) => group.milestones.length > 0)

  return (
    <section className="panel milestone-panel">
      <div className="panel-heading">
        <div>
          <p className="section-label">Pregnancy journey</p>
          <h2>Milestones</h2>
        </div>
        <span className="task-count">{completedCount}/{visibleMilestones.length} done</span>
      </div>

      <p className="panel-description">Your pregnancy timeline keeps key milestones together in one place.</p>

      <div className="milestone-summary">
        <strong>{activeMilestones[0]?.weekNumber ?? visibleMilestones[0]?.weekNumber ?? 0}w</strong>
        <span>{currentTrimester}</span>
      </div>

      <button className="text-button" type="button" onClick={() => setShowForm((value) => !value)}>
        {showForm ? 'Close custom milestone' : 'Add custom milestone'} <span>{showForm ? '↑' : '→'}</span>
      </button>

      {showForm && (
        <form className="custom-milestone-form" onSubmit={async (event) => {
          event.preventDefault()
          const token = sessionStorage.getItem('maatricare-token')
          if (!token || !title.trim()) return
          try {
            const response = await fetch(`${import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'}/pregnancy-milestones`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
              body: JSON.stringify({ title: title.trim(), description: description.trim() || undefined, weekNumber: Number(weekNumber), type: 'CUSTOM' }),
            })
            if (!response.ok) throw new Error()
            setTitle(''); setDescription(''); setWeekNumber('20'); setError(''); setShowForm(false)
            await onRefresh()
          } catch { setError('Unable to save this milestone.') }
        }}>
          <label>Milestone name<input required maxLength={120} value={title} onChange={(event) => setTitle(event.target.value)} /></label>
          <label>Week number<input type="number" min={1} max={42} value={weekNumber} onChange={(event) => setWeekNumber(event.target.value)} /></label>
          <label>
            Notes
            <textarea maxLength={500} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Optional reminder or detail" />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button className="primary-button" type="submit">Save milestone</button>
        </form>
      )}

      <div className="milestone-list">
        {trimesterGroups.map((group) => (
          <div key={group.label} className="milestone-trimester-group">
            <span className="milestone-trimester-label">{group.label}</span>
            {group.milestones.slice(0, expanded ? group.milestones.length : 2).map((milestone) => (
              <button
                key={milestone.id}
                type="button"
                aria-pressed={milestone.completed}
                className={`milestone-item ${milestone.completed ? 'done' : ''}`}
                onClick={async () => {
                  const token = sessionStorage.getItem('maatricare-token')
                  if (!token || togglingId) return
                  setTogglingId(milestone.id)
                  try {
                    const response = await fetch(`${import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'}/pregnancy-milestones/${milestone.id}/complete`, {
                      method: 'PATCH',
                      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
                      body: JSON.stringify({ completed: !milestone.completed }),
                    })
                    if (!response.ok) throw new Error()
                    await onRefresh()
                  } finally { setTogglingId(null) }
                }}
              >
                <span className="milestone-check">{milestone.completed ? <Check size={14} /> : '○'}</span>
                <span className="milestone-copy">
                  <strong>{milestone.title}</strong>
                  <small>{milestone.weekNumber} weeks</small>
                  {milestone.description && <em>{milestone.description}</em>}
                </span>
                <span className="milestone-type">{milestone.type}</span>
              </button>
            ))}
          </div>
        ))}
      </div>

      {visibleMilestones.length > 4 && (
        <button className="text-button" type="button" onClick={() => setExpanded((value) => !value)}>
          {expanded ? 'Show fewer' : 'View all milestones'} <span>{expanded ? '↑' : '→'}</span>
        </button>
      )}
    </section>
  )
}
