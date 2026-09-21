type LandingPageProps = {
  onSignUp: () => void
  onSignIn: () => void
}

const features = [
  ['01', 'A calmer daily rhythm', 'Keep vitamins, hydration, movement, and small moments of care in one gentle checklist.'],
  ['02', 'Progress you can understand', 'See your pregnancy week, trimester, due date, and the journey ahead at a glance.'],
  ['03', 'Notes for the conversations that matter', 'Keep questions, symptoms, and appointments close when it is time to speak with your care team.'],
]

export function LandingPage({ onSignUp, onSignIn }: LandingPageProps) {
  return (
    <main className="landing-page">
      <header className="landing-nav">
        <a className="landing-brand" href="#top" aria-label="MaatriCare home"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></a>
        <nav aria-label="Landing page navigation"><a href="#how-it-helps">How it helps</a><a href="#care-note">Our care note</a></nav>
        <div className="landing-actions"><button className="landing-sign-in" type="button" onClick={onSignIn}>Sign in</button><button className="landing-nav-cta" type="button" onClick={onSignUp}>Get started <span>→</span></button></div>
      </header>

      <section className="landing-hero" id="top">
        <div className="hero-copy"><p className="landing-kicker"><span /> Pregnancy care, made more personal</p><h1>A softer place for <em>every step</em> of your pregnancy.</h1><p className="landing-lede">MaatriCare brings your everyday care, appointments, and pregnancy milestones together in one calm, private space.</p><div className="hero-actions"><button className="hero-primary" type="button" onClick={onSignUp}>Create your care space <span>→</span></button><button className="hero-secondary" type="button" onClick={onSignIn}>I already have an account</button></div><p className="hero-note"><span>✦</span> Built for support and organization, never diagnosis.</p></div>
        <div className="hero-art" aria-label="Illustration of a calm pregnancy journey"><div className="art-sun" /><div className="art-ring ring-a" /><div className="art-ring ring-b" /><div className="art-card art-card-top"><span>THIS WEEK</span><strong>Week 24</strong><small>Second trimester</small></div><div className="art-card art-card-bottom"><span className="art-check">✓</span><div><strong>Today’s care</strong><small>2 of 3 complete</small></div></div><div className="art-belly"><span>♡</span></div><span className="art-star star-a">✦</span><span className="art-star star-b">✦</span></div>
      </section>

      <section className="landing-features" id="how-it-helps"><div className="feature-intro"><p className="landing-kicker"><span /> Made for the everyday</p><h2>Small support,<br /><em>right when you need it.</em></h2></div><div className="feature-list">{features.map(([number, title, description]) => <article className="feature-item" key={number}><span className="feature-number">{number}</span><div><h3>{title}</h3><p>{description}</p></div></article>)}</div></section>

      <section className="care-note" id="care-note"><div className="care-note-mark">m</div><div><p className="landing-kicker"><span /> A thoughtful boundary</p><h2>Your care belongs with you<br />and your healthcare team.</h2><p>MaatriCare helps you stay organized and informed. It does not diagnose conditions or replace professional medical advice.</p></div><button className="care-note-button" type="button" onClick={onSignUp}>Begin your journey <span>→</span></button></section>
      <footer className="landing-footer"><span>© 2026 MaatriCare</span><span>Private by design · Gentle by default</span></footer>
    </main>
  )
}