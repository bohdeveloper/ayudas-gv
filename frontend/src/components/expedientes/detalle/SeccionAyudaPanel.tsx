import { useTranslation } from 'react-i18next'
import type { SeccionAyuda } from '../../../types'
import CampoDato from './CampoDato'
import { formatFecha } from '../../../utils/formatFecha'
import { formatMoneda } from '../../../utils/formatMoneda'
import { panelStyles } from './panelStyles'

interface Props {
  data: SeccionAyuda
}

function DetalleAsignacionDiscapacidad({ detalle }: { detalle: Extract<SeccionAyuda['detalle'], { tipo: 'ASIG_DISCAP' }> }) {
  const { t, i18n } = useTranslation()
  const causante = detalle.causante
  const nombreCausante = [causante.nombre, causante.apellido1, causante.apellido2].filter(Boolean).join(' ')

  return (
    <div style={panelStyles.grid}>
      <CampoDato label={t('expedientes.detalle.gradoDiscapacidad')}>{detalle.gradoDiscapacidad}%</CampoDato>
      <CampoDato label={t('expedientes.detalle.necesitaTerceraPersona')}>
        {detalle.necesitaTerceraPersona ? t('common.yes') : t('common.no')}
      </CampoDato>
      <CampoDato label={t('expedientes.detalle.causante')}>{nombreCausante}</CampoDato>
      <CampoDato label={t('expedientes.detalle.fechaNacimiento')}>
        {formatFecha(causante.fechaNacimiento, i18n.language)}
      </CampoDato>
    </div>
  )
}

function DetalleNacimientoAdopcion({ detalle }: { detalle: Extract<SeccionAyuda['detalle'], { tipo: 'PAGO_NAC' }> }) {
  const { t, i18n } = useTranslation()

  return (
    <div style={panelStyles.grid}>
      <CampoDato label={t('expedientes.detalle.supuesto')}>{t(`expedientes.supuestos.${detalle.supuesto}`)}</CampoDato>
      <CampoDato label={t('expedientes.detalle.fechaHecho')}>{formatFecha(detalle.fechaHecho, i18n.language)}</CampoDato>
      <CampoDato label={t('expedientes.detalle.ingresosAnuales')}>
        {formatMoneda(detalle.ingresosAnuales, i18n.language)}
      </CampoDato>
      <CampoDato label={t('expedientes.detalle.numeroHijosCargo')}>{detalle.numeroHijosCargo}</CampoDato>
    </div>
  )
}

function DetallePartoMultiple({ detalle }: { detalle: Extract<SeccionAyuda['detalle'], { tipo: 'PARTO_MULT' }> }) {
  const { t, i18n } = useTranslation()

  return (
    <div style={panelStyles.grid}>
      <CampoDato label={t('expedientes.detalle.numeroNacidos')}>{detalle.numeroNacidos}</CampoDato>
      <CampoDato label={t('expedientes.detalle.fechaHecho')}>{formatFecha(detalle.fechaHecho, i18n.language)}</CampoDato>
    </div>
  )
}

export default function SeccionAyudaPanel({ data }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <div>
      <div style={panelStyles.grid}>
        <CampoDato label={t('expedientes.detalle.fechaSolicitud')}>
          {formatFecha(data.fechaSolicitud, i18n.language)}
        </CampoDato>
        <CampoDato label={t('expedientes.detalle.importeSolicitado')}>
          {formatMoneda(data.importeSolicitado, i18n.language)}
        </CampoDato>
        <CampoDato label={t('expedientes.detalle.observaciones')}>{data.observaciones}</CampoDato>
      </div>

      <div style={panelStyles.subseccion}>
        <h3 style={panelStyles.subseccionTitulo}>{t(`tiposAyuda.${data.detalle.tipo}`)}</h3>
        {data.detalle.tipo === 'ASIG_DISCAP' && <DetalleAsignacionDiscapacidad detalle={data.detalle} />}
        {data.detalle.tipo === 'PAGO_NAC' && <DetalleNacimientoAdopcion detalle={data.detalle} />}
        {data.detalle.tipo === 'PARTO_MULT' && <DetallePartoMultiple detalle={data.detalle} />}
      </div>
    </div>
  )
}
