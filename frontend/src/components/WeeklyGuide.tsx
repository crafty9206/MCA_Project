type WeeklyGuideProps = { currentWeek?: number; trimester?: string }

const guideContent = [
  {
    id: 'first',
    title: 'Early pregnancy care',
    summary: 'Your body is adjusting to pregnancy, and rest can help you feel more steady.',
    guidance: [
      'Aim for regular meals and hydration, especially if nausea is affecting your appetite.',
      'Keep prenatal vitamins consistent unless your clinician tells you otherwise.',
      'Notice when you feel more tired than usual and build in rest breaks.'
    ]
  },
  {
    id: 'second',
    title: 'Gentle movement and rhythm',
    summary: 'Many people feel more energetic in the second trimester, which can be a good time to focus on routine.',
    guidance: [
      'Choose light movement you enjoy, such as walking or stretching, if your clinician agrees it is appropriate.',
      'Keep an eye on hydration and sleep so your daily habits stay sustainable.',
      'Use the journal to track patterns in mood, fatigue, or movement.'
    ]
  },
  {
    id: 'third',
    title: 'Preparing for the final stretch',
    summary: 'Later pregnancy often brings more changes in comfort, sleep, and energy, and it is okay to ask for support.',
    guidance: [
      'Plan rest, hydration, and practical support around your schedule as your body changes.',
      'Review your birth and hospital plans with your care team when the time is right.',
      'Keep tracking symptoms, sleep, and mood so you can share clear information with your clinician.'
    ]
  }
]

import { useState } from 'react'

export function WeeklyGuide({ currentWeek = 24, trimester = 'Second trimester' }: WeeklyGuideProps) {
  const [expanded, setExpanded] = useState(false)
  const guide = guideContent[(currentWeek >= 28 ? 2 : currentWeek >= 14 ? 1 : 0)]

  return (
    <section className="panel insight-panel" id="resources">
      <div className="guide-art" aria-hidden="true"><span>✦</span><div>♥</div></div>
      <div className="insight-tag">YOUR {trimester.toUpperCase()} GUIDE · WEEK {currentWeek}</div>
      <h2>{guide.title}</h2>
      <p>{guide.summary}</p>
      {expanded && (
        <ul className="guide-points">
          {guide.guidance.map((point) => <li key={point}>{point}</li>)}
        </ul>
      )}
      <button className="light-button" type="button" onClick={() => setExpanded((value) => !value)}>
        {expanded ? 'Hide this week’s guide' : 'Read this week’s guide'} <span>{expanded ? '↑' : '→'}</span>
      </button>
    </section>
  )
}
