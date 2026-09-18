type WelcomeSectionProps = { name: string }

export function WelcomeSection({ name }: WelcomeSectionProps) {
  return (
    <section className="welcome-row">
      <div><p className="eyebrow"><span className="sun-dot">☀</span> Wednesday, September 16</p><h1>Good morning, {name} <span>♡</span></h1><p className="intro">A gentle check-in for you and your growing little one.</p></div>
      <button className="outline-button" type="button"><span>✦</span> Edit profile</button>
    </section>
  )
}
