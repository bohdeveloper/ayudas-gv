import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuthStore } from '../store/authStore'
import LanguageSelector from './LanguageSelector'
import ThemeToggle from './ThemeToggle'
import { IconChevronRight, IconLogout, IconX } from './icons/Icon'

export default function UserMenu() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { nombre, logout } = useAuthStore()
  const [open, setOpen] = useState(false)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const closeRef = useRef<HTMLButtonElement>(null)

  const initials = (nombre ?? '')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()

  // Foco: al abrir, mueve el foco al botón de cerrar; al cerrar, lo devuelve al disparador
  useEffect(() => {
    if (open) closeRef.current?.focus()
    else triggerRef.current?.focus()
  }, [open])

  // Cierre con Escape y bloqueo de scroll del fondo mientras el panel está abierto
  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('keydown', onKeyDown)
    document.body.style.overflow = open ? 'hidden' : ''
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      document.body.style.overflow = ''
    }
  }, [open])

  function handleLogout() {
    setOpen(false)
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <>
      <button
        ref={triggerRef}
        type="button"
        style={styles.trigger}
        onClick={() => setOpen(true)}
        aria-haspopup="dialog"
        aria-expanded={open}
      >
        <span style={styles.avatar}>{initials || '?'}</span>
        <span style={styles.name}>{nombre}</span>
        <IconChevronRight style={{ color: 'var(--color-text-muted)', flexShrink: 0 }} />
      </button>

      <div
        style={{ ...styles.backdrop, opacity: open ? 1 : 0, pointerEvents: open ? 'auto' : 'none' }}
        onClick={() => setOpen(false)}
        aria-hidden="true"
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-label={t('common.settings')}
        style={{ ...styles.panel, transform: open ? 'translateX(0)' : 'translateX(100%)' }}
      >
        <div style={styles.panelHeader}>
          <span style={styles.panelTitle}>{t('common.settings')}</span>
          <button
            ref={closeRef}
            type="button"
            style={styles.closeBtn}
            onClick={() => setOpen(false)}
            aria-label={t('common.close')}
          >
            <IconX />
          </button>
        </div>

        <div style={styles.panelBody}>
          <div style={styles.userBlock}>
            <span style={{ ...styles.avatar, width: 40, height: 40, fontSize: '0.9rem' }}>{initials || '?'}</span>
            <p style={styles.userName}>{nombre}</p>
          </div>

          <div style={styles.divider} />

          <div style={styles.section}>
            <span style={styles.sectionLabel}>{t('common.appearance')}</span>
            <ThemeToggle />
          </div>

          <div style={styles.section}>
            <span style={styles.sectionLabel}>{t('common.language')}</span>
            <LanguageSelector />
          </div>

          <div style={styles.divider} />

          <button type="button" style={styles.logoutBtn} onClick={handleLogout}>
            <IconLogout />
            {t('common.logout')}
          </button>
        </div>
      </div>
    </>
  )
}

const styles: Record<string, React.CSSProperties> = {
  trigger: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.55rem',
    background: 'transparent',
    border: '1px solid var(--color-border-strong)',
    borderRadius: 'var(--radius-sm)',
    padding: '0.3rem 0.65rem 0.3rem 0.3rem',
    cursor: 'pointer',
    color: 'var(--color-text)',
    transition: 'border-color var(--transition-fast)',
  },
  avatar: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: 26,
    height: 26,
    borderRadius: '50%',
    background: 'var(--color-surface-raised)',
    border: '1px solid var(--color-border-strong)',
    color: 'var(--color-text-muted)',
    fontSize: '0.68rem',
    fontWeight: 700,
    flexShrink: 0,
  },
  name: { fontSize: '0.85rem', color: 'var(--color-text-muted)' },
  backdrop: {
    position: 'fixed',
    inset: 0,
    background: 'rgba(0,0,0,0.5)',
    transition: 'opacity var(--transition-base)',
    zIndex: 40,
  },
  panel: {
    position: 'fixed',
    top: 0,
    right: 0,
    height: '100vh',
    width: 320,
    maxWidth: '85vw',
    background: 'var(--color-surface)',
    borderLeft: '1px solid var(--color-border)',
    boxShadow: 'var(--shadow-md)',
    transition: 'transform var(--transition-base)',
    zIndex: 50,
    display: 'flex',
    flexDirection: 'column',
  },
  panelHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '1rem 1.25rem',
    borderBottom: '1px solid var(--color-border)',
  },
  panelTitle: { fontSize: '0.95rem', fontWeight: 700, color: 'var(--color-text)' },
  closeBtn: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: 30,
    height: 30,
    background: 'transparent',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-sm)',
    color: 'var(--color-text-muted)',
    cursor: 'pointer',
  },
  panelBody: { padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1.1rem', overflowY: 'auto' },
  userBlock: { display: 'flex', alignItems: 'center', gap: '0.65rem' },
  userName: { fontSize: '0.9rem', fontWeight: 600, color: 'var(--color-text)' },
  divider: { height: 1, background: 'var(--color-border)' },
  section: { display: 'flex', alignItems: 'center', justifyContent: 'space-between' },
  sectionLabel: { fontSize: '0.85rem', color: 'var(--color-text)', fontWeight: 500 },
  logoutBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    background: 'transparent',
    border: '1px solid var(--color-border-strong)',
    color: 'var(--color-danger)',
    borderRadius: 'var(--radius-sm)',
    padding: '0.6rem 0.9rem',
    cursor: 'pointer',
    fontSize: '0.85rem',
    fontWeight: 600,
    justifyContent: 'center',
  },
}
