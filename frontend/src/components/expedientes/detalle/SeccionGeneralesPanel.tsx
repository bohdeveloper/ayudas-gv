import { useTranslation } from 'react-i18next'
import type { SeccionGenerales } from '../../../types'
import EstadoBadge from '../EstadoBadge'
import CampoDato from './CampoDato'
import { formatFecha } from '../../../utils/formatFecha'
import { panelStyles } from './panelStyles'

interface Props {
  data: SeccionGenerales
}

export default function SeccionGeneralesPanel({ data }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <div style={panelStyles.grid}>
      <CampoDato label={t('expedientes.numero')}>{data.numeroExpediente}</CampoDato>
      <CampoDato label={t('expedientes.tipoAyuda')}>{data.tipoAyuda.descripcion}</CampoDato>
      <CampoDato label={t('expedientes.estado')}>
        <EstadoBadge codigo={data.estado.codigo} descripcion={data.estado.descripcion} />
      </CampoDato>
      <CampoDato label={t('expedientes.fechaRegistro')}>{formatFecha(data.fechaRegistro, i18n.language)}</CampoDato>
      <CampoDato label={t('expedientes.detalle.fechaResolucion')}>
        {formatFecha(data.fechaResolucion, i18n.language)}
      </CampoDato>
      <CampoDato label={t('expedientes.instructor')}>{data.instructorNombre}</CampoDato>
    </div>
  )
}
