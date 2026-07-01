import { Outlet, NavLink } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import UserMenu from '../UserMenu'

export default function MainLayout() {
  const { t } = useTranslation()

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <nav style={styles.nav}>
        <div style={styles.brand}>
          <span style={styles.brandMark}>GV</span>
          <span style={styles.brandLabel}>Ayudas GV</span>
        </div>
        <div style={styles.navLinks}>
          <NavLink
            to="/expedientes"
            style={({ isActive }) => ({ ...styles.link, ...(isActive ? styles.linkActive : {}) })}
          >
            {t('nav.expedientes')}
          </NavLink>
        </div>
        <div style={styles.navRight}>
          <UserMenu />
        </div>
      </nav>
      <main style={styles.main}>
        <div style={styles.mainInner}>
          <Outlet />
        </div>
      </main>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  nav: {
    display: 'flex',
    alignItems: 'center',
    gap: '1.5rem',
    flexWrap: 'wrap',
    padding: '0.65rem 1.5rem',
    minHeight: 60,
    background: 'var(--color-surface)',
    borderBottom: '1px solid var(--color-border)',
    color: 'var(--color-text)',
  },
  brand: { display: 'flex', alignItems: 'center', gap: '0.6rem', marginRight: '0.5rem' },
  brandMark: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: 30,
    height: 30,
    borderRadius: 'var(--radius-sm)',
    background: 'var(--color-primary)',
    color: 'var(--color-primary-contrast)',
    fontWeight: 700,
    fontSize: '0.72rem',
    flexShrink: 0,
  },
  brandLabel: { fontWeight: 700, fontSize: '0.95rem' },
  navLinks: { display: 'flex', gap: '0.25rem', flex: 1 },
  link: {
    color: 'var(--color-text-muted)',
    textDecoration: 'none',
    fontSize: '0.88rem',
    fontWeight: 500,
    padding: '0.5rem 0.75rem',
    borderRadius: 'var(--radius-sm)',
    transition: 'background-color var(--transition-fast), color var(--transition-fast)',
  },
  linkActive: { color: 'var(--color-text)', background: 'var(--color-surface-raised)', fontWeight: 600 },
  navRight: { display: 'flex', alignItems: 'center', gap: '0.85rem', marginLeft: 'auto' },
  main: { flex: 1, padding: '2rem 1.5rem' },
  mainInner: { maxWidth: 1600, margin: '0 auto', width: '100%' },
}
