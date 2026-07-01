import { useTranslation } from 'react-i18next'

const LANGUAGES = [
  { code: 'es', label: 'ES' },
  { code: 'eu', label: 'EU' },
]

export default function LanguageSelector() {
  const { i18n, t } = useTranslation()
  const current = i18n.language?.slice(0, 2) ?? 'es'

  function changeLanguage(code: string) {
    i18n.changeLanguage(code)
    // i18next-browser-languagedetector guarda en localStorage bajo la clave 'idioma'
  }

  return (
    <div style={styles.wrapper} role="group" aria-label={t('common.language')}>
      {LANGUAGES.map(({ code, label }) => (
        <button
          key={code}
          type="button"
          onClick={() => changeLanguage(code)}
          style={{
            ...styles.btn,
            ...(current === code ? styles.btnActive : {}),
          }}
          aria-pressed={current === code}
        >
          {label}
        </button>
      ))}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  wrapper: {
    display: 'flex',
    gap: 2,
    background: 'var(--color-bg)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-sm)',
    padding: 2,
  },
  btn: {
    background: 'transparent',
    border: 'none',
    color: 'var(--color-text-muted)',
    borderRadius: 4,
    padding: '4px 10px',
    cursor: 'pointer',
    fontSize: '0.75rem',
    fontWeight: 600,
    letterSpacing: '0.04em',
    transition: 'background-color var(--transition-fast), color var(--transition-fast)',
  },
  btnActive: {
    background: 'var(--color-primary)',
    color: 'var(--color-primary-contrast)',
  },
}
