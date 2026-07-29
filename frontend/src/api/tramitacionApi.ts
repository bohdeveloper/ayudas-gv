import axiosClient from './axiosClient'
import type { InstructorAsignable, NuevoTramiteRequest, SeccionTramitacion, Tramite } from '../types'

/**
 * Llamadas de la sección Tramitación (Fase 1).
 *
 * Los trámites cuelgan del expediente porque no existen sin él. El backend valida
 * todas las reglas (rol, expediente asignado, estado válido): aquí no se replica
 * ninguna — el frontend solo adapta la interfaz.
 */

export async function obtenerTramitacion(id: number): Promise<SeccionTramitacion> {
  const { data } = await axiosClient.get<SeccionTramitacion>(`/expedientes/${id}/tramites`)
  return data
}

export async function registrarTramite(id: number, peticion: NuevoTramiteRequest): Promise<Tramite> {
  const { data } = await axiosClient.post<Tramite>(`/expedientes/${id}/tramites`, peticion)
  return data
}

export async function asignarInstructor(id: number, instructorId: number): Promise<SeccionTramitacion> {
  const { data } = await axiosClient.put<SeccionTramitacion>(`/expedientes/${id}/instructor`, { instructorId })
  return data
}

/** Solo la pueden pedir RESPONSABLE y ADMIN: al resto el backend responde 403. */
export async function obtenerInstructores(): Promise<InstructorAsignable[]> {
  const { data } = await axiosClient.get<InstructorAsignable[]>('/instructores')
  return data
}
