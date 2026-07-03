/** Formatea un importe en euros según el idioma activo, o cadena vacía si no hay valor. */
export function formatMoneda(importe: number | undefined | null, locale: string): string {
  if (importe === undefined || importe === null) return ''
  return new Intl.NumberFormat(locale, { style: 'currency', currency: 'EUR' }).format(importe)
}
