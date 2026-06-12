/**
 * Configuración del sistema de internacionalización (i18n) con react-i18next.
 *
 * Este módulo inicializa i18next una sola vez al arrancar la aplicación.
 * Los componentes React acceden a las traducciones mediante el hook useTranslation().
 *
 * Flujo de detección del idioma activo:
 *   1. Busca la clave 'idioma' en localStorage (persiste la elección del usuario).
 *   2. Si no existe, usa el idioma del navegador (navigator.language).
 *   3. Si no coincide con ninguno soportado, usa el fallback ('es').
 *
 * Los ficheros de traducción están en:
 *   - locales/es.json  → castellano
 *   - locales/eu.json  → euskera
 */
import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import es from './locales/es.json'
import eu from './locales/eu.json'

i18n
  // LanguageDetector: detecta el idioma automáticamente (localStorage → navegador)
  .use(LanguageDetector)
  // initReactI18next: conecta i18next con el sistema de contexto de React
  .use(initReactI18next)
  .init({
    // Recursos de traducción agrupados por código de idioma
    resources: {
      es: { translation: es },
      eu: { translation: eu },
    },
    // Idioma de reserva si el detectado no está disponible
    fallbackLng: 'es',
    supportedLngs: ['es', 'eu'],
    interpolation: {
      // React ya escapa HTML por defecto, así que desactivamos el escape doble
      escapeValue: false,
    },
    detection: {
      // Orden de detección: primero localStorage, luego el idioma del navegador
      order: ['localStorage', 'navigator'],
      // Guarda el idioma elegido en localStorage para que persista entre sesiones
      caches: ['localStorage'],
      lookupLocalStorage: 'idioma',   // clave en localStorage
    },
  })

export default i18n
