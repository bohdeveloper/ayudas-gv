import { useTranslation } from 'react-i18next'
import { useThemeStore } from '../store/themeStore'
import { IconMoon, IconSun } from './icons/Icon'

export default function ThemeToggle() {
  const { t } = useTranslation()
  const { theme, toggleTheme } = useThemeStore()
  const isLight = theme === 'light'

  return (
    <button
      type="button"
      onClick={toggleTheme}
      style={styles.btn}
      aria-label={isLight ? t('common.darkMode') : t('common.lightMode')}
      title={isLight ? t('common.darkMode') : t('common.lightMode')}
    >
      {isLight ? <IconMoon /> : <IconSun />}
    </button>
  )
}

const styles: Record<string, React.CSSProperties> = {
  btn: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: 32,
    height: 32,
    background: 'var(--color-bg)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-sm)',
    color: 'var(--color-text-muted)',
    cursor: 'pointer',
    transition: 'border-color var(--transition-fast), color var(--transition-fast)',
  },
}
