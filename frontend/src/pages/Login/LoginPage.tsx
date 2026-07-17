import { useState, FormEvent } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { login } from '../../api/authApi'
import { useAuthStore } from '../../store/authStore'
import LanguageSelector from '../../components/LanguageSelector'
import { IconAlertCircle, IconEye, IconEyeOff } from '../../components/icons/Icon'

export default function LoginPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((s) => s.setSession)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  // Redirige de vuelta a la página que intentó acceder, o a /expedientes
  const from = (location.state as { from?: string })?.from ?? '/expedientes'

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const data = await login({ username, password })
      setSession(data)
      navigate(from, { replace: true })
    } catch {
      setError(t('auth.loginError'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.topRow}>
          <div style={styles.brand}>
            <span style={styles.brandMark}>GV</span>
            <div>
              <p style={styles.brandTitle}>Ayudas GV</p>
              <p style={styles.brandSubtitle}>{t('auth.tagline')}</p>
            </div>
          </div>
          <LanguageSelector />
        </div>

        <h1 style={styles.title}>{t('auth.login')}</h1>

        <form onSubmit={handleSubmit} style={styles.form} noValidate>
          <label style={styles.label} htmlFor="username">
            {t('auth.username')}
            <input
              id="username"
              style={styles.input}
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
              disabled={loading}
            />
          </label>

          <label style={styles.label} htmlFor="password">
            {t('auth.password')}
            <div style={styles.passwordWrapper}>
              <input
                id="password"
                style={{ ...styles.input, paddingRight: '2.5rem' }}
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
                disabled={loading}
              />
              <button
                type="button"
                style={styles.togglePassword}
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? t('auth.hidePassword') : t('auth.showPassword')}
                tabIndex={loading ? -1 : 0}
              >
                {showPassword ? <IconEyeOff /> : <IconEye />}
              </button>
            </div>
          </label>

          {error && (
            <p style={styles.errorMsg} role="alert">
              <IconAlertCircle style={{ flexShrink: 0 }} />
              {error}
            </p>
          )}

          <button style={styles.btn} type="submit" disabled={loading}>
            {loading ? t('common.loading') : t('auth.login')}
          </button>
        </form>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background:
      'radial-gradient(1200px 600px at 50% -10%, rgba(74,99,224,0.16), transparent), var(--color-bg)',
    padding: '1.5rem',
  },
  card: {
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: '2rem 2.25rem',
    width: '100%',
    maxWidth: 400,
    boxShadow: 'var(--shadow-md)',
  },
  topRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.75rem' },
  brand: { display: 'flex', alignItems: 'center', gap: '0.65rem' },
  brandMark: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: 38,
    height: 38,
    borderRadius: 'var(--radius-md)',
    background: 'var(--color-primary)',
    color: 'var(--color-primary-contrast)',
    fontWeight: 700,
    fontSize: '0.85rem',
    letterSpacing: '0.02em',
    flexShrink: 0,
  },
  brandTitle: { color: 'var(--color-text)', fontSize: '0.92rem', fontWeight: 700, lineHeight: 1.2, margin: 0 },
  brandSubtitle: { color: 'var(--color-text-muted)', fontSize: '0.75rem', margin: 0 },
  title: { color: 'var(--color-text)', fontSize: '1.35rem', fontWeight: 700, marginBottom: '1.5rem' },
  form: { display: 'flex', flexDirection: 'column', gap: '1.1rem' },
  label: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.4rem',
    color: 'var(--color-text)',
    fontSize: '0.85rem',
    fontWeight: 500,
  },
  input: {
    background: 'var(--color-bg)',
    border: '1px solid var(--color-border-strong)',
    borderRadius: 'var(--radius-sm)',
    padding: '0.65rem 0.8rem',
    color: 'var(--color-text)',
    fontSize: '0.95rem',
    fontFamily: 'inherit',
    outline: 'none',
    width: '100%',
    transition: 'border-color var(--transition-fast)',
  },
  passwordWrapper: { position: 'relative', display: 'flex' },
  togglePassword: {
    position: 'absolute',
    right: 4,
    top: '50%',
    transform: 'translateY(-50%)',
    background: 'transparent',
    border: 'none',
    color: 'var(--color-text-muted)',
    cursor: 'pointer',
    padding: '0.4rem',
    display: 'flex',
    borderRadius: 'var(--radius-sm)',
  },
  errorMsg: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    background: 'var(--color-danger-bg)',
    border: '1px solid rgba(248,113,113,0.35)',
    borderRadius: 'var(--radius-sm)',
    color: 'var(--color-danger)',
    fontSize: '0.85rem',
    padding: '0.6rem 0.75rem',
    margin: 0,
  },
  btn: {
    background: 'var(--color-primary)',
    color: 'var(--color-primary-contrast)',
    border: 'none',
    borderRadius: 'var(--radius-sm)',
    padding: '0.7rem',
    fontWeight: 700,
    fontSize: '0.95rem',
    cursor: 'pointer',
    marginTop: '0.25rem',
    transition: 'background-color var(--transition-fast)',
  },
}
