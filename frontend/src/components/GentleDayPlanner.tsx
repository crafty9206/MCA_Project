import { useEffect, useState } from 'react'
import { BatteryLow, BatteryMedium, BatteryFull, Check, CheckCircle2, Minus, Plus, Sparkles } from 'lucide-react'
import type { CareTask, DailyWellbeing } from '../api'

type EnergyLevel = DailyWellbeing['energyLevel']
type Props = {
  tasks: CareTask[]
  wellbeing: DailyWellbeing | null
  onSave: (energyLevel: EnergyLevel, focusTaskCount: number, focusTaskIds: string[]) => Promise<void>
  onToggle: (id: string, completed: boolean) => void
}

const energyOptions: { value: EnergyLevel; label: string; description: string; count: number; icon: typeof BatteryLow }[] = [
  { value: 'LOW', label: 'Low energy', description: 'One gentle priority', count: 1, icon: BatteryLow },
  { value: 'STEADY', label: 'Steady', description: 'A small realistic list', count: 3, icon: BatteryMedium },
  { value: 'HIGH', label: 'Good energy', description: 'A fuller focus list', count: 5, icon: BatteryFull },
]

export function GentleDayPlanner({ tasks, wellbeing, onSave, onToggle }: Props) {
  const [energyLevel, setEnergyLevel] = useState<EnergyLevel>(wellbeing?.energyLevel ?? 'STEADY')
  const [focusTaskCount, setFocusTaskCount] = useState(wellbeing?.focusTaskCount ?? 3)
  const [selectedIds, setSelectedIds] = useState<string[]>(wellbeing?.focusTaskIds ?? [])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!wellbeing) return
    setEnergyLevel(wellbeing.energyLevel ?? 'STEADY')
    setFocusTaskCount(wellbeing.focusTaskIds?.length ? wellbeing.focusTaskCount : Math.min(wellbeing.focusTaskCount ?? 3, Math.max(1, tasks.filter((task) => !task.completed).length)))
    setSelectedIds(wellbeing.focusTaskIds ?? [])
  }, [wellbeing])

  const unfinishedTasks = tasks.filter((task) => !task.completed)
  const focusTasks = selectedIds.map((id) => tasks.find((task) => task.id === id)).filter((task): task is CareTask => Boolean(task))
  const activeIds = wellbeing?.focusTaskIds ?? []
  const saved = Boolean(wellbeing && selectedIds.length > 0 && energyLevel === wellbeing.energyLevel && focusTaskCount === wellbeing.focusTaskCount && selectedIds.length === activeIds.length && selectedIds.every((id, index) => id === activeIds[index]))
  const selectedOption = energyOptions.find((option) => option.value === energyLevel) ?? energyOptions[1]

  const chooseEnergy = (value: EnergyLevel, count: number) => {
    setEnergyLevel(value)
    const availableCount = Math.min(count, Math.max(1, unfinishedTasks.length))
    setFocusTaskCount(availableCount)
    setSelectedIds((current) => current.filter((id) => unfinishedTasks.some((task) => task.id === id)).slice(0, availableCount))
    setError('')
  }

  const selectTask = (id: string) => {
    setSelectedIds((current) => current.includes(id) ? current.filter((item) => item !== id) : current.length < focusTaskCount ? [...current, id] : current)
    setError('')
  }

  const save = async () => {
    if (selectedIds.length !== focusTaskCount) {
      setError(`Choose ${focusTaskCount} ${focusTaskCount === 1 ? 'task' : 'tasks'} from today's list before saving.`)
      return
    }
    setSaving(true)
    try {
      await onSave(energyLevel, focusTaskCount, selectedIds)
      setError('')
    } catch {
      setError('The plan could not be saved. Please try again.')
    } finally { setSaving(false) }
  }

  return (
    <section className="panel gentle-planner-panel">
      <div className="panel-heading"><div><p className="section-label">Capacity-aware planning</p><h2>Your gentle day</h2></div><span className="gentle-planner-icon"><Sparkles /></span></div>
      <p className="panel-description">Choose what feels realistic today. Your full checklist stays unchanged.</p>
      <div className="energy-options" role="group" aria-label="Today’s energy level">
        {energyOptions.map((option) => { const Icon = option.icon; return <button className={energyLevel === option.value ? 'active' : ''} type="button" aria-pressed={energyLevel === option.value} key={option.value} onClick={() => chooseEnergy(option.value, option.count)}><Icon /><span><strong>{option.label}</strong><small>{option.description}</small></span></button> })}
      </div>
      <div className="focus-count"><div><strong>Focus-list size</strong><span>{selectedOption.label} · choose what feels manageable</span></div><div><button type="button" aria-label="Show one fewer focus task" disabled={focusTaskCount <= 1} onClick={() => { setFocusTaskCount((count) => { const next = Math.max(1, count - 1); setSelectedIds((current) => current.filter((id) => unfinishedTasks.some((task) => task.id === id)).slice(0, next)); return next }) }}><Minus /></button><b>{focusTaskCount}</b><button type="button" aria-label="Show one more focus task" disabled={focusTaskCount >= 5 || focusTaskCount >= unfinishedTasks.length} onClick={() => { setFocusTaskCount((count) => Math.min(5, unfinishedTasks.length, count + 1)); setSelectedIds((current) => current.filter((id) => unfinishedTasks.some((task) => task.id === id))) }}><Plus /></button></div></div>
      <div className="gentle-task-choices"><div className="gentle-focus-heading"><strong>Choose your focus {focusTaskCount === 1 ? 'task' : 'tasks'}</strong><span>{selectedIds.length} / {focusTaskCount} selected</span></div>{unfinishedTasks.length === 0 ? <p>No unfinished tasks today. Add tasks in Today’s care below.</p> : unfinishedTasks.map((task) => <button type="button" key={task.id} className={selectedIds.includes(task.id) ? 'selected' : ''} aria-pressed={selectedIds.includes(task.id)} disabled={!selectedIds.includes(task.id) && selectedIds.length >= focusTaskCount} onClick={() => selectTask(task.id)}><span className="gentle-choice-check">{selectedIds.includes(task.id) && <Check />}</span>{task.title}</button>)}</div>
      <div className="gentle-focus-list">
        <div className="gentle-focus-heading"><strong>{saved ? 'Your focus today' : 'Your selection'}</strong><span>{focusTasks.length} chosen</span></div>
        {focusTasks.length === 0 ? <p>Select from today’s tasks above to decide what to focus on.</p> : focusTasks.map((task, index) => <button type="button" key={task.id} className={task.completed ? 'completed' : ''} disabled={!saved || task.completed} onClick={() => onToggle(task.id, true)}><span>{index + 1}</span><b>{task.title}{task.completed ? ' · Done' : ''}</b><i><Check /></i></button>)}
      </div>
      {saved && unfinishedTasks.length > 0 && <button className="text-button gentle-change" type="button" onClick={() => { setSelectedIds([]); setFocusTaskCount((count) => Math.min(count, unfinishedTasks.length)); setError('') }}>Choose different focus tasks</button>}
      {error && <p className="form-error" role="alert">{error}</p>}
      {saved && <div className="gentle-saved-status" role="status"><CheckCircle2 /><div><strong>Today’s plan is active</strong><span>{selectedOption.label} · {focusTaskCount} focus {focusTaskCount === 1 ? 'task' : 'tasks'} saved for {wellbeing?.date}</span></div></div>}
      <button className="primary-button gentle-save" type="button" disabled={saving || !wellbeing || saved || unfinishedTasks.length === 0} onClick={save}>{saving ? 'Saving…' : saved ? 'Saved for today' : 'Save selected tasks'}</button>
      <small className="gentle-note">This planner organizes tasks only. It does not decide which medical care is necessary; follow your healthcare professional’s instructions.</small>
    </section>
  )
}