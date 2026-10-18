import { useEffect, useRef } from 'react'
import { BookOpen, Bot, CalendarDays, ClipboardList, FileText, LayoutDashboard, Library, NotebookPen, X } from 'lucide-react'

const modules = [
  { label: 'Overview', icon: LayoutDashboard },
  { label: 'Daily Care', icon: ClipboardList },
  { label: 'Appointments', icon: CalendarDays },
  { label: 'Journal', icon: NotebookPen },
  { label: 'Guides', icon: BookOpen },
  { label: 'Resources', icon: Library },
  { label: 'Reports', icon: FileText },
  { label: 'AI Assistant', icon: Bot },
]

type Props = { activeNav: string; onNavigate: (item: string) => void; mobileOpen: boolean; onClose: () => void }

export function Sidebar({ activeNav, onNavigate, mobileOpen, onClose }: Props) {
  const drawerRef = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const drawer = drawerRef.current
    if (mobileOpen && drawer && !drawer.open) drawer.showModal()
    if (!mobileOpen && drawer?.open) drawer.close()
  }, [mobileOpen])

  useEffect(() => {
    const media = window.matchMedia('(min-width: 761px)')
    const closeOnDesktop = () => { if (media.matches) onClose() }
    media.addEventListener('change', closeOnDesktop)
    return () => media.removeEventListener('change', closeOnDesktop)
  }, [onClose])

  const navigation = (mobile: boolean) => (
    <nav className="sidebar-links" aria-label={mobile ? 'Mobile module navigation' : 'Module navigation'}>
      {modules.map(({ label, icon: Icon }) => (
        <a key={label} href={`#${label.toLowerCase().replace(' ', '-')}`} title={label} aria-label={label}
          aria-current={activeNav === label ? 'page' : undefined}
          onClick={() => { onNavigate(label); if (mobile) onClose() }}>
          <Icon aria-hidden="true" /><span>{label}</span>
        </a>
      ))}
    </nav>
  )

  return <>
    <aside className="module-sidebar" id="desktop-module-sidebar">{navigation(false)}</aside>
    <dialog className="module-drawer" id="mobile-module-sidebar" aria-label="Navigation menu" ref={drawerRef} onCancel={onClose} onClose={onClose}
      onKeyDown={(event) => { if (event.key === 'Escape') { event.preventDefault(); onClose() } }}
      onClick={(event) => { if (event.target === event.currentTarget) { const bounds = event.currentTarget.getBoundingClientRect(); if (event.clientX < bounds.left || event.clientX > bounds.right || event.clientY < bounds.top || event.clientY > bounds.bottom) onClose() } }}>
      <header><strong>MaatriCare</strong><button type="button" aria-label="Close navigation" onClick={onClose}><X /></button></header>
      {navigation(true)}
    </dialog>
  </>
}