const symptoms = ['Feeling well', 'Tired', 'Nauseous', 'Other']

type JournalCardProps = { selected: string | null; onSelect: (symptom: string) => void }

export function JournalCard({ selected, onSelect }: JournalCardProps) {
  return (
    <section className="panel" id="journal">
      <div className="panel-heading"><div><p className="section-label">Your wellbeing</p><h2>How are you feeling?</h2></div><span className="mood-mark">♡</span></div>
      <p className="panel-description">A small check-in can help you notice patterns over time.</p>
      <div className="symptom-options">{symptoms.map((symptom) => <button className={selected === symptom ? 'selected' : ''} type="button" key={symptom} onClick={() => onSelect(symptom)}>{symptom}</button>)}</div>
      <button className="text-button" type="button">Open my journal <span>→</span></button>
    </section>
  )
}
