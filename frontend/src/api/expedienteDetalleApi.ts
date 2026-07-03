import axiosClient from './axiosClient'
import type { SeccionAyuda, SeccionContacto, SeccionGenerales, SeccionInteresados } from '../types'

export async function obtenerGenerales(id: number): Promise<SeccionGenerales> {
  const { data } = await axiosClient.get<SeccionGenerales>(`/expedientes/${id}/generales`)
  return data
}

export async function obtenerInteresados(id: number): Promise<SeccionInteresados> {
  const { data } = await axiosClient.get<SeccionInteresados>(`/expedientes/${id}/interesados`)
  return data
}

export async function obtenerContacto(id: number): Promise<SeccionContacto> {
  const { data } = await axiosClient.get<SeccionContacto>(`/expedientes/${id}/contacto`)
  return data
}

export async function obtenerAyuda(id: number): Promise<SeccionAyuda> {
  const { data } = await axiosClient.get<SeccionAyuda>(`/expedientes/${id}/ayuda`)
  return data
}
