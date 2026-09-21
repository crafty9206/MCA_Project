import { useState } from 'react'
import type { FormEvent } from 'react'
import { createPregnancyProfile, forgotPassword, getProfile, login, register, resetPassword } from '../api'
import type { DashboardProfile } from '../api'

export type PregnancyDetails = {
  displayName: string
  lastPeriod: string
  age: string
  height: string
  weight: string
  bloodPressure: string
  bloodGroup: string 
}

type AuthFlowProps = { onComplete: (profile: DashboardProfile) => void; onAdmin: () => void; initialScreen?: 'signup' | 'signin' }

const emptyDetails: PregnancyDetails = { displayName: '', lastPeriod: '', age: '', height: '', weight: '', bloodPressure: '', bloodGroup: '' }

export function AuthFlow({ onComplete, onAdmin, initialScreen = 'signup' }: AuthFlowProps) {
  const [screen, setScreen] = useState<'signup' | 'signin' | 'profile' | 'forgot' | 'reset'>(initialScreen)
  const [details, setDetails] = useState(emptyDetails)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [message, setMessage] = useState('')
  const [token, setToken] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const update = (key: keyof PregnancyDetails, value: string) => setDetails((current) => ({ ...current, [key]: value }))
  const startProfile = async (event: FormEvent) => { event.preventDefault(); setError(''); setSubmitting(true); try { const auth = await register({ displayName: details.displayName, email, password }); setToken(auth.token); sessionStorage.setItem('maatricare-token', auth.token); sessionStorage.removeItem('maatricare-auth-screen'); setScreen('profile') } catch (cause) { setError(cause instanceof Error ? cause.message : 'Unable to create your account.') } finally { setSubmitting(false) } }
  const submitProfile = async (event: FormEvent) => { event.preventDefault(); setError(''); setSubmitting(true); try { const profile = await createPregnancyProfile(token, { lastMenstrualPeriod: details.lastPeriod, ageYears: Number(details.age), heightCm: details.height ? Number(details.height) : undefined, prePregnancyWeightKg: details.weight ? Number(details.weight) : undefined, bloodPressure: details.bloodPressure || undefined, bloodGroup: details.bloodGroup || undefined }); onComplete(profile) } catch (cause) { setError(cause instanceof Error ? cause.message : 'Unable to save your pregnancy profile.') } finally { setSubmitting(false) } }
  const signIn = async (event: FormEvent) => { event.preventDefault(); setError(''); setSubmitting(true); try { const auth = await login(email, password); sessionStorage.setItem('maatricare-token', auth.token); sessionStorage.removeItem('maatricare-auth-screen'); if (auth.role === 'ADMIN') { onAdmin(); return } const profile = await getProfile(auth.token); if (profile.pregnancy) onComplete(profile); else { setToken(auth.token); setScreen('profile') } } catch (cause) { setError(cause instanceof Error ? cause.message : 'Unable to sign in.') } finally { setSubmitting(false) } }
  const requestPasswordReset = async (event: FormEvent) => { event.preventDefault(); setError(''); setMessage(''); setSubmitting(true); try { const response = await forgotPassword(email); if (response.resetToken) { setResetToken(response.resetToken); setScreen('reset') } else { setMessage(response.message) } } catch (cause) { setError(cause instanceof Error ? cause.message : 'Unable to create reset instructions.') } finally { setSubmitting(false) } }
  const submitPasswordReset = async (event: FormEvent) => { event.preventDefault(); setError(''); if (password !== confirmPassword) { setError('Passwords do not match.'); return } setSubmitting(true); try { await resetPassword(resetToken, password); setPassword(''); setConfirmPassword(''); setMessage('Password updated. You can now sign in.'); setScreen('signin') } catch (cause) { setError(cause instanceof Error ? cause.message : 'Unable to reset your password.') } finally { setSubmitting(false) } }
  const passwordStrength = password.length < 8 ? 'Too short' : /[A-Z]/.test(password) && /[a-z]/.test(password) && /\d/.test(password) ? 'Strong' : 'Good'

  if (screen === 'profile') return (
    <main className="auth-page"><div className="auth-orb orb-a" /><div className="auth-orb orb-b" />
      <section className="auth-card profile-card">
        <div className="auth-brand"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></div>
        <div className="progress-steps"><span className="complete">1</span><i /><span className="current">2</span><i /><span>3</span></div>
        <p className="auth-eyebrow">YOUR PREGNANCY PROFILE</p><h1>Let’s personalise your care</h1><p className="auth-intro">These details help tailor your dashboard. You can update them later.</p>
        <form className="profile-form" onSubmit={submitProfile}>
          <label>First day of your last period<input required type="date" value={details.lastPeriod} onChange={(event) => update('lastPeriod', event.target.value)} /></label>
          <div className="form-row"><label>Age<input required min="13" max="60" inputMode="numeric" type="number" placeholder="Years" value={details.age} onChange={(event) => update('age', event.target.value)} /></label><label>Blood group <span className="optional">Optional</span><select value={details.bloodGroup} onChange={(event) => update('bloodGroup', event.target.value)}><option value="">Select</option><option>A+</option><option>A-</option><option>B+</option><option>B-</option><option>AB+</option><option>AB-</option><option>O+</option><option>O-</option></select></label></div>
          <div className="form-row"><label>Height <span className="optional">Optional</span><div className="input-unit"><input inputMode="decimal" type="number" placeholder="0" value={details.height} onChange={(event) => update('height', event.target.value)} /><b>cm</b></div></label><label>Pre-pregnancy weight <span className="optional">Optional</span><div className="input-unit"><input inputMode="decimal" type="number" placeholder="0" value={details.weight} onChange={(event) => update('weight', event.target.value)} /><b>kg</b></div></label></div>
          <label>Most recent blood pressure <span className="optional">Optional</span><input placeholder="For example, 120/80 mmHg" value={details.bloodPressure} onChange={(event) => update('bloodPressure', event.target.value)} /></label>
          <p className="privacy-note">⌁ Your health details are private. MaatriCare is for tracking and education, not medical diagnosis or urgent care.</p>
          {error && <p className="form-error">{error}</p>}<button className="auth-submit" disabled={submitting} type="submit">{submitting ? 'Saving…' : 'Create my care plan'} <span>→</span></button><button className="back-button" type="button" onClick={() => setScreen('signup')}>← Back</button>
        </form>
      </section>
    </main>
  )

  if (screen === 'forgot' || screen === 'reset') return (
    <main className="auth-page"><div className="auth-orb orb-a" /><div className="auth-orb orb-b" />
      <section className="auth-card"><div className="auth-brand"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></div>
        <p className="auth-eyebrow">ACCOUNT RECOVERY</p><h1>{screen === 'forgot' ? 'Reset your password' : 'Choose a new password'}</h1><p className="auth-intro">{screen === 'forgot' ? 'Enter your account email to create secure reset instructions.' : 'Reset tokens expire after 30 minutes and can only be used once.'}</p>
        <form className="auth-form" onSubmit={screen === 'forgot' ? requestPasswordReset : submitPasswordReset}>
          {screen === 'forgot' ? <label>Email address<input required autoComplete="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label> : <><label>Reset token<input required value={resetToken} onChange={(event) => setResetToken(event.target.value)} /></label><label>New password<input required minLength={8} maxLength={72} type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label><div className={`password-strength strength-${passwordStrength.toLowerCase().replace(' ', '-')}`}><span /><small>{passwordStrength}</small></div><label>Confirm password<input required minLength={8} maxLength={72} type="password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} /></label></>}
          {message && <p className="form-success">{message}</p>}{error && <p className="form-error">{error}</p>}<button className="auth-submit" disabled={submitting} type="submit">{submitting ? 'Please wait…' : screen === 'forgot' ? 'Create reset instructions' : 'Update password'} <span>→</span></button><button className="back-button" type="button" onClick={() => setScreen('signin')}>← Back to sign in</button>
        </form>
      </section>
    </main>
  )

  const signingIn = screen === 'signin'
  return (
    <main className="auth-page"><div className="auth-orb orb-a" /><div className="auth-orb orb-b" />
      <section className="auth-card"><div className="auth-brand"><span className="brand-mark"><span>m</span></span><span>Maatri<span>care</span></span></div>
        <div className="auth-illustration" aria-hidden="true"><span>✦</span><div>♡</div></div>
        <p className="auth-eyebrow">A GENTLER WAY TO TRACK</p><h1>{signingIn ? 'Welcome back' : 'Care for you, every step of the way'}</h1><p className="auth-intro">{signingIn ? 'Sign in to continue your pregnancy journey.' : 'Your calm, private space for pregnancy care and everyday support.'}</p>
        <form className="auth-form" onSubmit={signingIn ? signIn : startProfile}>
          {!signingIn && <label>Your name<input required autoComplete="name" placeholder="Enter your name" value={details.displayName} onChange={(event) => update('displayName', event.target.value)} /></label>}
          <label>Email address<input required autoComplete="email" type="email" placeholder="you@example.com" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
          <label>Password<input required minLength={8} autoComplete={signingIn ? 'current-password' : 'new-password'} type="password" placeholder="At least 8 characters" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          {!signingIn && <div className={`password-strength strength-${passwordStrength.toLowerCase().replace(' ', '-')}`}><span /><small>{passwordStrength}</small></div>}
          {signingIn && <button className="forgot-button" type="button" onClick={() => { setError(''); setMessage(''); setScreen('forgot') }}>Forgot password?</button>}
          {message && <p className="form-success">{message}</p>}{error && <p className="form-error">{error}</p>}<button className="auth-submit" disabled={submitting} type="submit">{submitting ? 'Please wait…' : signingIn ? 'Sign in' : 'Continue'} <span>→</span></button>
        </form>
        <p className="auth-switch">{signingIn ? 'New to MaatriCare?' : 'Already have an account?'} <button type="button" onClick={() => setScreen(signingIn ? 'signup' : 'signin')}>{signingIn ? 'Create an account' : 'Sign in'}</button></p>
      </section>
    </main>
  )
}
