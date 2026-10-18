import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { HeartPulse, Pencil, Phone, Plus, Trash2, X } from 'lucide-react'
import { deleteEmergencyContact, getEmergencyContacts, saveEmergencyContact } from '../api'
import type { EmergencyContact, EmergencyContactInput } from '../api'

const contactTypes: { value: EmergencyContact['type']; label: string }[] = [
  { value: 'HOSPITAL', label: 'Maternity hospital' }, { value: 'DOCTOR', label: 'Doctor / maternity team' },
  { value: 'PARTNER', label: 'Husband / partner' }, { value: 'FAMILY', label: 'Family' },
  { value: 'FRIEND', label: 'Friend' }, { value: 'OTHER', label: 'Other' },
]
const blankContact: EmergencyContactInput = { name: '', phone: '', type: 'PARTNER' }

export function EmergencyContacts() {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const [open, setOpen] = useState(false)
  const [region, setRegion] = useState('')
  const [contacts, setContacts] = useState<EmergencyContact[]>([])
  const [loading, setLoading] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState<EmergencyContactInput>(blankContact)
  const [editingId, setEditingId] = useState<string>()
  const [showForm, setShowForm] = useState(false)
  const [deletingId, setDeletingId] = useState<string>()
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (open) dialogRef.current?.showModal()
    else dialogRef.current?.close()
  }, [open])

  const loadContacts = async () => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setLoading(true)
    setLoadError('')
    try { setContacts(await getEmergencyContacts(token)) }
    catch { setLoadError('Saved contacts could not be loaded.') }
    finally { setLoading(false) }
  }

  const close = () => {
    setOpen(false)
    setShowForm(false)
    setDeletingId(undefined)
    setRegion('')
    setError('')
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || saving) return
    const phone = form.phone.trim().replace(/[\s()-]/g, '')
    if (!form.name.trim() || !/^\+?[0-9]{7,15}$/.test(phone)) {
      setError('Enter a name and a phone number with 7–15 digits, optionally starting with +.')
      return
    }
    setSaving(true)
    setError('')
    try {
      const saved = await saveEmergencyContact(token, { ...form, name: form.name.trim(), phone }, editingId)
      setContacts((current) => [...current.filter((contact) => contact.id !== saved.id), saved].sort((first, second) => first.name.localeCompare(second.name)))
      setShowForm(false)
    } catch { setError('Contact could not be saved. Please try again.') }
    finally { setSaving(false) }
  }

  const remove = async (id: string) => {
    const token = sessionStorage.getItem('maatricare-token')
    if (!token || saving) return
    setSaving(true)
    setError('')
    try {
      await deleteEmergencyContact(token, id)
      setContacts((current) => current.filter((contact) => contact.id !== id))
      setDeletingId(undefined)
    } catch { setError('Contact could not be deleted. Please try again.') }
    finally { setSaving(false) }
  }

  return <>
    <button className="emergency-open" type="button" title="Emergency contacts" aria-haspopup="dialog" onClick={() => { setOpen(true); void loadContacts() }}><HeartPulse aria-hidden="true" /><span>Emergency contacts</span></button>
    <dialog ref={dialogRef} className="emergency-dialog" aria-labelledby="emergency-title" onCancel={(event) => { event.preventDefault(); close() }} onClose={() => setOpen(false)}>
      <header className="emergency-heading"><div><p className="section-label">Quick access</p><h2 id="emergency-title">Emergency & support</h2></div><button type="button" className="emergency-icon-button" title="Close emergency contacts" aria-label="Close emergency contacts" onClick={close}><X /></button></header>
      <p className="emergency-warning" role="note">For an immediate emergency, call your local emergency number. Do not wait for an app response.</p>
      <section className="emergency-service" aria-label="Emergency services">
        <label>Current location<select value={region} onChange={(event) => setRegion(event.target.value)}><option value="">Choose your country / region</option><option value="IN">India</option><option value="OTHER">Another country / region</option></select></label>
        {region === 'IN' ? <div className="emergency-service-call"><div><strong>India emergency services</strong><span>112 · Police, fire, and medical emergencies</span></div><a href="tel:112" className="emergency-call"><Phone aria-hidden="true" /> Call 112</a></div> : <p>{region === 'OTHER' ? 'Use the verified local emergency number for your current location.' : 'Confirm your current location to see the supported emergency number.'}</p>}
      </section>
      <section className="emergency-personal" aria-labelledby="personal-contacts-title"><div className="emergency-list-heading"><h3 id="personal-contacts-title">Your contacts</h3><button type="button" className="text-button" onClick={() => { setEditingId(undefined); setForm(blankContact); setShowForm(true); setError('') }}><Plus aria-hidden="true" /> Add contact</button></div>
        {loading && <p role="status">Loading contacts…</p>}
        {loadError && <div className="form-error" role="alert">{loadError} <button type="button" onClick={() => void loadContacts()}>Retry</button></div>}
        {!loading && !loadError && contacts.length === 0 && <p className="emergency-empty">No contacts saved yet.</p>}
        {contacts.map((contact) => <article className="emergency-contact" key={contact.id}>
          <div className="emergency-contact-info"><strong>{contact.name}</strong><span>{contactTypes.find((type) => type.value === contact.type)?.label}</span><span>{contact.phone}</span></div>
          <a className="emergency-call" href={`tel:${contact.phone}`} aria-label={`Call ${contact.name}`}><Phone aria-hidden="true" /> Call</a>
          <div className="emergency-contact-actions"><button className="emergency-icon-button" type="button" title={`Edit ${contact.name}`} aria-label={`Edit ${contact.name}`} onClick={() => { setEditingId(contact.id); setForm({ name: contact.name, phone: contact.phone, type: contact.type }); setShowForm(true); setError('') }}><Pencil /></button><button className="emergency-icon-button" type="button" title={`Delete ${contact.name}`} aria-label={`Delete ${contact.name}`} onClick={() => setDeletingId(contact.id)}><Trash2 /></button></div>
          {deletingId === contact.id && <div className="emergency-delete"><span>Delete this contact?</span><button type="button" disabled={saving} onClick={() => void remove(contact.id)}>Delete</button><button type="button" disabled={saving} onClick={() => setDeletingId(undefined)}>Cancel</button></div>}
        </article>)}
      </section>
      {showForm && <form className="emergency-form" onSubmit={submit}><h3>{editingId ? 'Edit contact' : 'Add contact'}</h3><label>Name<input required maxLength={100} value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label><label>Contact type<select value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value as EmergencyContact['type'] })}>{contactTypes.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}</select></label><label>Phone number<input required type="tel" maxLength={30} autoComplete="tel" placeholder="+91…" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label><div className="emergency-form-actions"><button className="outline-button" type="button" disabled={saving} onClick={() => setShowForm(false)}>Cancel</button><button className="primary-button" disabled={saving} type="submit">{saving ? 'Saving…' : 'Save contact'}</button></div></form>}
      {error && <p className="form-error" role="alert">{error}</p>}
      <p className="emergency-footnote">Calls open your device’s dialer or calling app. MaatriCare does not dispatch ambulances or guarantee a response. Contacts are private to your account.</p>
    </dialog>
  </>
}