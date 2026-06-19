import { useTranslation } from 'react-i18next'

const LANGUAGES = [
  { code: 'es', label: 'ES' },
  { code: 'eu', label: 'EU' },
]

export default function LanguageSelector() {
  const { i18n } = useTranslation()
  const current = i18n.language?.slice(0, 2) ?? 'es'

  function changeLanguage(code: string) {
    i18n.changeLanguage(code)
    // i18next-browser-languagedetector guarda en localStorage bajo la clave 'idioma'
  }

  return (
    <div style={styles.wrapper}>
      {LANGUAGES.map(({ code, label }) => (
        <button
          key={code}
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
  wrapper: { display: 'flex', gap: 4 },
  btn: {
    background: 'transparent',
    border: '1px solid #475569',
    color: '#94a3b8',
    borderRadius: 5,
    padding: '3px 9px',
    cursor: 'pointer',
    fontSize: '0.78rem',
    fontWeight: 600,
    letterSpacing: '0.04em',
    transition: 'background 0.15s, color 0.15s',
  },
  btnActive: {
    background: '#38bdf8',
    borderColor: '#38bdf8',
    color: '#0f172a',
  },
}
