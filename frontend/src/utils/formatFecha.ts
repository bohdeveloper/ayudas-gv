/** Formatea una fecha ISO ("2024-01-10" o "2024-01-10T09:00:00") según el idioma activo. */
export function formatFecha(fecha: string | undefined | null, locale: string): string {
  if (!fecha) return ''
  return new Date(fecha).toLocaleDateString(locale, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}
