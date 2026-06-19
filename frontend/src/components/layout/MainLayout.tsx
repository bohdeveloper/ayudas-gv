import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuthStore } from '../../store/authStore'
import LanguageSelector from '../LanguageSelector'

export default function MainLayout() {
  const { t } = useTranslation()
  const { nombre, logout } = useAuthStore()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <nav style={styles.nav}>
        <div style={styles.brand}>GV Ayudas</div>
        <div style={styles.navLinks}>
          <NavLink
            to="/expedientes"
            style={({ isActive }) => ({ ...styles.link, ...(isActive ? styles.linkActive : {}) })}
          >
            {t('nav.expedientes')}
          </NavLink>
        </div>
        <div style={styles.navRight}>
          <LanguageSelector />
          <span style={styles.user}>{nombre}</span>
          <button style={styles.btnLogout} onClick={handleLogout}>
            {t('common.logout')}
          </button>
        </div>
      </nav>
      <main style={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  nav: {
    display: 'flex',
    alignItems: 'center',
    gap: '1.5rem',
    padding: '0 1.5rem',
    height: 56,
    background: '#1e3a5f',
    color: '#fff',
  },
  brand: { fontWeight: 700, fontSize: '1.1rem', marginRight: '1rem' },
  navLinks: { display: 'flex', gap: '1rem', flex: 1 },
  link: { color: '#cbd5e1', textDecoration: 'none', fontSize: '0.9rem' },
  linkActive: { color: '#fff', fontWeight: 600, borderBottom: '2px solid #38bdf8' },
  navRight: { display: 'flex', alignItems: 'center', gap: '1rem', marginLeft: 'auto' },
  user: { fontSize: '0.85rem', color: '#94a3b8' },
  btnLogout: {
    background: 'transparent',
    border: '1px solid #475569',
    color: '#cbd5e1',
    borderRadius: 6,
    padding: '4px 12px',
    cursor: 'pointer',
    fontSize: '0.85rem',
  },
  main: { flex: 1, padding: '2rem 1.5rem' },
}
