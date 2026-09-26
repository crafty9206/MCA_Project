import { useEffect, useState } from 'react'
import { BatteryLow, BatteryMedium, BatteryFull, Check, CheckCircle2, Minus, Plus, Sparkles } from 'lucide-react'
import type { CareTask, DailyWellbeing } from '../api'

type EnergyLevel = DailyWellbeing['energyLevel']
type Props = {
  tasks: CareTask[]
  wellbeing: DailyWellbeing | null
  onSave: (energyLevel: EnergyLevel, focusTaskCount: number) => Promise<void>
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
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(true)

  useEffect(() => {
    if (!wellbeing) return
    setEnergyLevel(wellbeing.energyLevel ?? 'STEADY')
    setFocusTaskCount(wellbeing.focusTaskCount ?? 3)
    setSaved(true)
  }, [wellbeing])

  const unfinishedTasks = tasks.filter((task) => !task.completed)
  const focusTasks = unfinishedTasks.slice(0, focusTaskCount)
  const selectedOption = energyOptions.find((option) => option.value === energyLevel) ?? energyOptions[1]

  const chooseEnergy = (value: EnergyLevel, count: number) => {
    setEnergyLevel(value)
    setFocusTaskCount(count)
    setSaved(false)
  }

  const save = async () => {
    setSaving(true)
    try {
      await onSave(energyLevel, focusTaskCount)
      setSaved(true)
    } finally { setSaving(false) }
  }

  return (
    <section className="panel gentle-planner-panel">
      <div className="panel-heading"><div><p className="section-label">Capacity-aware planning</p><h2>Your gentle day</h2></div><span className="gentle-planner-icon"><Sparkles /></span></div>
      <p className="panel-description">Choose what feels realistic today. Your full checklist stays unchanged.</p>
      <div className="energy-options" role="group" aria-label="Today’s energy level">
        {energyOptions.map((option) => { const Icon = option.icon; return <button className={energyLevel === option.value ? 'active' : ''} type="button" aria-pressed={energyLevel === option.value} key={option.value} onClick={() => chooseEnergy(option.value, option.count)}><Icon /><span><strong>{option.label}</strong><small>{option.description}</small></span></button> })}
      </div>
      <div className="focus-count"><div><strong>Focus-list size</strong><span>{selectedOption.label} · choose what feels manageable</span></div><div><button type="button" aria-label="Show one fewer focus task" disabled={focusTaskCount <= 1} onClick={() => { setFocusTaskCount((count) => Math.max(1, count - 1)); setSaved(false) }}><Minus /></button><b>{focusTaskCount}</b><button type="button" aria-label="Show one more focus task" disabled={focusTaskCount >= 5} onClick={() => { setFocusTaskCount((count) => Math.min(5, count + 1)); setSaved(false) }}><Plus /></button></div></div>
      <div className="gentle-focus-list">
        <div className="gentle-focus-heading"><strong>{saved ? 'Active plan' : 'Plan preview'}</strong><span>{focusTasks.length} of {unfinishedTasks.length} unfinished tasks</span></div>
        {unfinishedTasks.length === 0 ? <p>Everything on today’s list is complete. Rest counts too.</p> : focusTasks.map((task, index) => <button type="button" key={task.id} onClick={() => onToggle(task.id, true)}><span>{index + 1}</span><b>{task.title}</b><i><Check /></i></button>)}
      </div>
      {saved && <div className="gentle-saved-status" role="status"><CheckCircle2 /><div><strong>Today’s plan is active</strong><span>{selectedOption.label} · {focusTaskCount} focus {focusTaskCount === 1 ? 'task' : 'tasks'} saved for {wellbeing?.date}</span></div></div>}
      <button className="primary-button gentle-save" type="button" disabled={saving || !wellbeing || saved} onClick={save}>{saving ? 'Saving…' : saved ? 'Saved for today' : 'Save today’s plan'}</button>
      <small className="gentle-note">This planner organizes tasks only. It does not decide which medical care is necessary; follow your healthcare professional’s instructions.</small>
    </section>
  )
}