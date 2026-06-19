import { useState, FormEvent } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { login } from '../../api/authApi'
import { useAuthStore } from '../../store/authStore'
import LanguageSelector from '../../components/LanguageSelector'

export default function LoginPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((s) => s.setSession)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
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
        <div style={styles.langRow}>
          <LanguageSelector />
        </div>
        <h1 style={styles.title}>GV Ayudas</h1>
        <p style={styles.subtitle}>{t('auth.login')}</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>
            {t('auth.username')}
            <input
              style={styles.input}
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
              disabled={loading}
            />
          </label>

          <label style={styles.label}>
            {t('auth.password')}
            <input
              style={styles.input}
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
              disabled={loading}
            />
          </label>

          {error && <p style={styles.errorMsg}>{error}</p>}

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
    background: '#0f172a',
  },
  card: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: 12,
    padding: '2rem 2.5rem',
    width: '100%',
    maxWidth: 380,
    boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
  },
  langRow: { display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' },
  title: { color: '#38bdf8', fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.25rem' },
  subtitle: { color: '#94a3b8', fontSize: '0.9rem', marginBottom: '1.5rem' },
  form: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  label: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.4rem',
    color: '#e2e8f0',
    fontSize: '0.875rem',
    fontWeight: 500,
  },
  input: {
    background: '#0f172a',
    border: '1px solid #475569',
    borderRadius: 6,
    padding: '0.55rem 0.75rem',
    color: '#e2e8f0',
    fontSize: '0.9rem',
    outline: 'none',
  },
  errorMsg: {
    background: 'rgba(248,113,113,0.12)',
    border: '1px solid #f87171',
    borderRadius: 6,
    color: '#f87171',
    fontSize: '0.85rem',
    padding: '0.5rem 0.75rem',
    margin: 0,
  },
  btn: {
    background: '#38bdf8',
    color: '#0f172a',
    border: 'none',
    borderRadius: 6,
    padding: '0.65rem',
    fontWeight: 700,
    fontSize: '0.95rem',
    cursor: 'pointer',
    marginTop: '0.5rem',
    transition: 'opacity 0.2s',
  },
}
