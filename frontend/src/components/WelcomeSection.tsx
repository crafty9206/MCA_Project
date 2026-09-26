import { localeFor, translate } from '../i18n'
import type { LanguageCode } from '../i18n'

type WelcomeSectionProps = { name: string; language: LanguageCode }

export function WelcomeSection({ name, language }: WelcomeSectionProps) {
  const hour = new Date().getHours()
  const greeting = translate(language, hour < 12 ? 'welcome.morning' : hour < 17 ? 'welcome.afternoon' : 'welcome.evening')
  const today = new Intl.DateTimeFormat(localeFor(language), { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date())

  return (
    <section className="welcome-row">
      <div><p className="eyebrow"><span className="sun-dot">☀</span> {today}</p><h1>{greeting}, {name} <span>♡</span></h1><p className="intro">{translate(language, 'welcome.intro')}</p></div>
      <button className="outline-button" type="button"><span>✦</span> {translate(language, 'welcome.edit')}</button>
    </section>
  )
}
