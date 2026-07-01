import { useTranslation } from 'react-i18next'
import { IconChevronLeft, IconChevronRight } from '../icons/Icon'

interface Props {
  page: number
  totalPages: number
  totalElements: number
  onPageChange: (page: number) => void
  disabled: boolean
}

export default function Paginacion({ page, totalPages, totalElements, onPageChange, disabled }: Props) {
  const { t } = useTranslation()

  if (totalElements === 0) return null

  return (
    <div style={styles.wrapper}>
      <span style={styles.info} className="tabular-nums">
        {t('common.page')} {page + 1} {t('common.of')} {Math.max(totalPages, 1)} — {totalElements} {t('common.results')}
      </span>
      <div style={styles.buttons}>
        <button
          style={styles.btn}
          onClick={() => onPageChange(page - 1)}
          disabled={disabled || page <= 0}
        >
          <IconChevronLeft />
          {t('common.previous')}
        </button>
        <button
          style={styles.btn}
          onClick={() => onPageChange(page + 1)}
          disabled={disabled || page + 1 >= totalPages}
        >
          {t('common.next')}
          <IconChevronRight />
        </button>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  wrapper: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '0.75rem',
    marginTop: '1.1rem',
  },
  info: { color: 'var(--color-text-muted)', fontSize: '0.82rem' },
  buttons: { display: 'flex', gap: '0.5rem' },
  btn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.3rem',
    background: 'transparent',
    border: '1px solid var(--color-border-strong)',
    color: 'var(--color-text-muted)',
    borderRadius: 'var(--radius-sm)',
    padding: '0.4rem 0.9rem',
    fontSize: '0.82rem',
    cursor: 'pointer',
    transition: 'border-color var(--transition-fast), color var(--transition-fast)',
  },
}
