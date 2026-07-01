import axiosClient from './axiosClient'
import type { ExpedienteFiltros, ExpedienteResumen, PageResponse } from '../types'

export async function buscarExpedientes(
  filtros: ExpedienteFiltros,
): Promise<PageResponse<ExpedienteResumen>> {
  const { data } = await axiosClient.get<PageResponse<ExpedienteResumen>>('/expedientes', {
    params: filtros,
  })
  return data
}
