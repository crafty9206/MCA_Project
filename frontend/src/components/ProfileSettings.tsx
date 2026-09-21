import { useState } from 'react'
import type { FormEvent } from 'react'
import { updatePregnancyProfile, updateProfile } from '../api'
import type { DashboardProfile } from '../api'

type ProfileSettingsProps = {
  profile: DashboardProfile
  onClose: () => void
  onSaved: (profile: DashboardProfile) => void
}

export function ProfileSettings({ profile, onClose, onSaved }: ProfileSettingsProps) {
  const pregnancy = profile.pregnancy
  const [displayName, setDisplayName] = useState(profile.displayName)
  const [lastMenstrualPeriod, setLastMenstrualPeriod] = useState(pregnancy?.lastMenstrualPeriod ?? '')
  const [ageYears, setAgeYears] = useState(pregnancy?.ageYears?.toString() ?? '')
  const [heightCm, setHeightCm] = useState(pregnancy?.heightCm?.toString() ?? '')
  const [weightKg, setWeightKg] = useState(pregnancy?.prePregnancyWeightKg?.toString() ?? '')
  const [bloodPressure, setBloodPressure] = useState(pregnancy?.bloodPressure ?? '')
  const [bloodGroup, setBloodGroup] = useState(pregnancy?.bloodGroup ?? '')
  const [appointmentRemindersEnabled, setAppointmentRemindersEnabled] = useState(profile.appointmentRemindersEnabled)
  const [browserNotificationsEnabled, setBrowserNotificationsEnabled] = useState(profile.browserNotificationsEnabled)
  const [dailyCareRemindersEnabled, setDailyCareRemindersEnabled] = useState(profile.dailyCareRemindersEnabled)
  const [dailyCareReminderTime, setDailyCareReminderTime] = useState(profile.dailyCareReminderTime?.slice(0, 5) ?? '09:00')
  const [weeklyPregnancyRemindersEnabled, setWeeklyPregnancyRemindersEnabled] = useState(profile.weeklyPregnancyRemindersEnabled)
  const initialWeeklyDay = profile.weeklyPregnancyReminderDay >= 1 && profile.weeklyPregnancyReminderDay <= 7
    ? profile.weeklyPregnancyReminderDay : 1
  const [weeklyPregnancyReminderDay, setWeeklyPregnancyReminderDay] = useState(initialWeeklyDay.toString())
  const [weeklyPregnancyReminderTime, setWeeklyPregnancyReminderTime] = useState(profile.weeklyPregnancyReminderTime?.slice(0, 5) ?? '09:00')
  const [missedTaskRemindersEnabled, setMissedTaskRemindersEnabled] = useState(profile.missedTaskRemindersEnabled)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const token = sessionStorage.getItem('maatricare-token')
    if (!token) return
    setSaving(true)
    setError('')
    try {
      const reminderDay = Math.min(7, Math.max(1, Number(weeklyPregnancyReminderDay) || 1))
      let updated = await updateProfile(token, { displayName, appointmentRemindersEnabled, browserNotificationsEnabled, dailyCareRemindersEnabled, dailyCareReminderTime, weeklyPregnancyRemindersEnabled, weeklyPregnancyReminderDay: reminderDay, weeklyPregnancyReminderTime, missedTaskRemindersEnabled })
      if (pregnancy) {
        updated = await updatePregnancyProfile(token, {
          lastMenstrualPeriod,
          ageYears: Number(ageYears),
          heightCm: heightCm ? Number(heightCm) : undefined,
          prePregnancyWeightKg: weightKg ? Number(weightKg) : undefined,
          bloodPressure: bloodPressure || undefined,
          bloodGroup: bloodGroup || undefined,
        })
      }
      onSaved(updated)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Unable to update your profile.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="confirm-overlay" role="presentation">
      <section className="settings-dialog" role="dialog" aria-modal="true" aria-labelledby="profile-settings-title">
        <div className="settings-heading"><div><p className="section-label">Your account</p><h2 id="profile-settings-title">Profile settings</h2></div><button className="settings-close" type="button" aria-label="Close profile settings" onClick={onClose}>×</button></div>
        <form className="profile-form" onSubmit={submit}>
          <label>Display name<input required maxLength={120} value={displayName} onChange={(event) => setDisplayName(event.target.value)} /></label>
          <label>Email<input disabled value={profile.email} /></label>
          {pregnancy && <><div className="settings-divider"><span>Pregnancy details</span></div><label>First day of your last period<input required type="date" value={lastMenstrualPeriod} onChange={(event) => setLastMenstrualPeriod(event.target.value)} /></label><div className="form-row"><label>Age<input required min="13" max="60" type="number" value={ageYears} onChange={(event) => setAgeYears(event.target.value)} /></label><label>Blood group <span className="optional">Optional</span><select value={bloodGroup} onChange={(event) => setBloodGroup(event.target.value)}><option value="">Select</option><option>A+</option><option>A-</option><option>B+</option><option>B-</option><option>AB+</option><option>AB-</option><option>O+</option><option>O-</option></select></label></div><div className="form-row"><label>Height <span className="optional">Optional</span><div className="input-unit"><input min="50" max="250" type="number" step="0.1" value={heightCm} onChange={(event) => setHeightCm(event.target.value)} /><b>cm</b></div></label><label>Pre-pregnancy weight <span className="optional">Optional</span><div className="input-unit"><input min="20" max="300" type="number" step="0.1" value={weightKg} onChange={(event) => setWeightKg(event.target.value)} /><b>kg</b></div></label></div><label>Blood pressure <span className="optional">Optional</span><input maxLength={20} value={bloodPressure} onChange={(event) => setBloodPressure(event.target.value)} placeholder="For example, 120/80 mmHg" /></label></>}
          <div className="settings-divider"><span>Notifications</span></div><label className="preference-row"><span><strong>Appointment reminders</strong><small>Show saved appointment reminders in the notification center.</small></span><input type="checkbox" checked={appointmentRemindersEnabled} onChange={(event) => setAppointmentRemindersEnabled(event.target.checked)} /></label><label className="preference-row"><span><strong>Daily care reminder</strong><small>Remind you about incomplete care tasks each day.</small></span><input type="checkbox" checked={dailyCareRemindersEnabled} onChange={(event) => setDailyCareRemindersEnabled(event.target.checked)} /></label>{dailyCareRemindersEnabled && <label>Daily reminder time<input required type="time" value={dailyCareReminderTime} onChange={(event) => setDailyCareReminderTime(event.target.value)} /></label>}<label className="preference-row"><span><strong>Missed care tasks</strong><small>Show yesterday’s incomplete tasks the next day.</small></span><input type="checkbox" checked={missedTaskRemindersEnabled} onChange={(event) => setMissedTaskRemindersEnabled(event.target.checked)} /></label><label className="preference-row"><span><strong>Weekly pregnancy update</strong><small>Receive a weekly check-in for your current pregnancy week.</small></span><input type="checkbox" checked={weeklyPregnancyRemindersEnabled} onChange={(event) => setWeeklyPregnancyRemindersEnabled(event.target.checked)} /></label>{weeklyPregnancyRemindersEnabled && <div className="form-row"><label>Reminder day<select value={weeklyPregnancyReminderDay} onChange={(event) => setWeeklyPregnancyReminderDay(event.target.value)}><option value="1">Monday</option><option value="2">Tuesday</option><option value="3">Wednesday</option><option value="4">Thursday</option><option value="5">Friday</option><option value="6">Saturday</option><option value="7">Sunday</option></select></label><label>Reminder time<input required type="time" value={weeklyPregnancyReminderTime} onChange={(event) => setWeeklyPregnancyReminderTime(event.target.value)} /></label></div>}<label className="preference-row"><span><strong>Browser notifications</strong><small>Show browser alerts while MaatriCare is open.</small></span><input type="checkbox" checked={browserNotificationsEnabled} disabled={!appointmentRemindersEnabled && !dailyCareRemindersEnabled && !weeklyPregnancyRemindersEnabled && !missedTaskRemindersEnabled} onChange={(event) => setBrowserNotificationsEnabled(event.target.checked)} /></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <div className="settings-actions"><button className="outline-button" type="button" disabled={saving} onClick={onClose}>Cancel</button><button className="primary-button" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save changes'}</button></div>
        </form>
      </section>
    </div>
  )
}
