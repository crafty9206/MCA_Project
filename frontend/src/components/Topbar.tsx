type TopbarProps = {
  language: string
  onLanguageChange: (language: string) => void
  activeNav: string
  onNavigate: (item: string) => void
}

const navigation = ['Overview', 'My journal', 'Resources']

export function Topbar({ language, onLanguageChange, activeNav, onNavigate }: TopbarProps) {
  return (
    <header className="topbar">
      <a className="brand" href="#overview" aria-label="MaatriCare home"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></a>
      <nav className="nav-links" aria-label="Main navigation">{navigation.map((item) => <a className={activeNav === item ? 'active' : ''} href={item === 'Overview' ? '#overview' : item === 'My journal' ? '#journal' : '#resources'} key={item} onClick={() => onNavigate(item)}>{item}</a>)}</nav>
      <div className="top-actions"><label className="language-select"><span className="globe">◎</span><span className="sr-only">Language</span><select value={language} onChange={(event) => onLanguageChange(event.target.value)}><option>English</option><option>Hindi</option><option>Tamil</option></select></label><button className="notification" type="button" aria-label="Notifications">♢<span /></button><button className="avatar" type="button" aria-label="Open profile menu">AS</button></div>
    </header>
  )
}
