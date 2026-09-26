import { useState } from 'react'
import type { WeeklyGuideData } from '../api'
import { localeFor, translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type WeeklyGuideProps = { guide: WeeklyGuideData | null; language: LanguageCode }

export function WeeklyGuide({ guide, language }: WeeklyGuideProps) {
  const [expanded, setExpanded] = useState(false)

  if (!guide) {
    return <section className="panel insight-panel"><div className="insight-tag">{translate(language, 'guide.label')}</div><h2>{translate(language, 'guide.preparing')}</h2><p>{translate(language, 'guide.profile')}</p></section>
  }

  const sections = [
    [translate(language, 'guide.nutrition'), guide.nutrition],
    [translate(language, 'guide.activity'), guide.activity],
    [translate(language, 'guide.body'), guide.bodyChanges],
    [translate(language, 'guide.discuss'), guide.questionsForClinician]
  ] as const

  return (
    <section className="panel insight-panel" id="resources">
      <div className="guide-art" aria-hidden="true"><span>✦</span><div>♥</div></div>
      <div className="insight-tag">YOUR {guide.trimester.toUpperCase()} GUIDE · WEEK {guide.week}</div>
      <h2>{guide.title}</h2>
      <p>{guide.summary}</p>
      {expanded && (
        <div className="guide-sections">
          {sections.map(([title, points]) => <section key={title}><h3>{title}</h3><ul className="guide-points">{points.map((point) => <li key={point}>{point}</li>)}</ul></section>)}
          <small>v{guide.contentVersion} · {new Date(`${guide.reviewedOn}T00:00:00`).toLocaleDateString(localeFor(language))}</small>
        </div>
      )}
      <button className="light-button" type="button" onClick={() => setExpanded((value) => !value)}>
        {translate(language, expanded ? 'guide.hide' : 'guide.read')} <span>{expanded ? '↑' : '→'}</span>
      </button>
    </section>
  )
}
