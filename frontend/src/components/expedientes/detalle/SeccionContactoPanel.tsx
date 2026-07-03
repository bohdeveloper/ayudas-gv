import { useTranslation } from 'react-i18next'
import type { SeccionContacto } from '../../../types'
import CampoDato from './CampoDato'
import { panelStyles } from './panelStyles'

interface Props {
  data: SeccionContacto
}

export default function SeccionContactoPanel({ data }: Props) {
  const { t } = useTranslation()

  const direccion = [data.via, data.numero, data.piso].filter(Boolean).join(', ')

  return (
    <div style={panelStyles.grid}>
      <CampoDato label={t('expedientes.detalle.direccion')}>{direccion}</CampoDato>
      <CampoDato label={t('expedientes.detalle.codigoPostal')}>{data.codigoPostal}</CampoDato>
      <CampoDato label={t('expedientes.detalle.municipio')}>{data.municipio}</CampoDato>
      <CampoDato label={t('expedientes.detalle.provincia')}>{data.provincia}</CampoDato>
      <CampoDato label={t('expedientes.detalle.telefono')}>{data.telefono}</CampoDato>
      <CampoDato label={t('expedientes.detalle.email')}>{data.email}</CampoDato>
      <CampoDato label={t('expedientes.detalle.canalAviso')}>
        {data.canalAviso ? t(`expedientes.canales.${data.canalAviso}`) : null}
      </CampoDato>
      <CampoDato label={t('expedientes.detalle.idiomaNotificacion')}>
        {data.idiomaNotificacion?.toUpperCase()}
      </CampoDato>
      <CampoDato label={t('expedientes.detalle.aceptaNotifTelematica')}>
        {data.aceptaNotifTelematica ? t('common.yes') : t('common.no')}
      </CampoDato>
    </div>
  )
}
