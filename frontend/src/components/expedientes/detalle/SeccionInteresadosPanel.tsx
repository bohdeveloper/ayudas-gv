import { useTranslation } from 'react-i18next'
import type { Persona, SeccionInteresados } from '../../../types'
import CampoDato from './CampoDato'
import { formatFecha } from '../../../utils/formatFecha'
import { panelStyles } from './panelStyles'

interface Props {
  data: SeccionInteresados
}

function PersonaFicha({ persona }: { persona: Persona }) {
  const { t, i18n } = useTranslation()
  const nombreCompleto = [persona.nombre, persona.apellido1, persona.apellido2].filter(Boolean).join(' ')

  return (
    <div style={panelStyles.grid}>
      <CampoDato label={t('expedientes.detalle.nombre')}>{nombreCompleto}</CampoDato>
      <CampoDato label={t('expedientes.detalle.documento')}>
        {persona.tipoDocumento} {persona.numeroDocumento}
      </CampoDato>
      <CampoDato label={t('expedientes.detalle.fechaNacimiento')}>
        {formatFecha(persona.fechaNacimiento, i18n.language)}
      </CampoDato>
    </div>
  )
}

export default function SeccionInteresadosPanel({ data }: Props) {
  const { t } = useTranslation()

  return (
    <div>
      <h3 style={panelStyles.subseccionTitulo}>{t('expedientes.detalle.solicitante')}</h3>
      <div style={{ marginTop: '0.9rem' }}>
        <PersonaFicha persona={data.solicitante} />
      </div>

      <div style={panelStyles.subseccion}>
        <h3 style={panelStyles.subseccionTitulo}>{t('expedientes.detalle.representante')}</h3>
        {data.representante ? (
          <PersonaFicha persona={data.representante} />
        ) : (
          <p style={panelStyles.vacio}>{t('expedientes.detalle.sinRepresentante')}</p>
        )}
      </div>

      <div style={panelStyles.subseccion}>
        <h3 style={panelStyles.subseccionTitulo}>{t('expedientes.detalle.causantes')}</h3>
        {data.causantes.length === 0 ? (
          <p style={panelStyles.vacio}>{t('expedientes.detalle.sinCausantes')}</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {data.causantes.map((causante) => (
              <PersonaFicha key={causante.id} persona={causante} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
