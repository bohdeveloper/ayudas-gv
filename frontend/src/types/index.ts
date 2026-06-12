/**
 * Tipos TypeScript compartidos para toda la aplicación frontend.
 *
 * Centralizar los tipos aquí garantiza que el frontend y el backend hablen
 * el mismo "lenguaje de datos": si un DTO del backend cambia, solo hay que
 * actualizar este fichero y TypeScript marcará todos los usos incorrectos.
 *
 * Convención: los tipos union de string (ej. TipoAyuda) reflejan exactamente
 * los códigos que devuelve el backend; los interfaces con campos opcionales (?)
 * indican datos que pueden no existir en BD (nullable en SQL).
 */

export type Idioma = 'es' | 'eu'

export type RolUsuario = 'CONSULTOR' | 'INSTRUCTOR' | 'RESPONSABLE' | 'ADMIN'

export type TipoAyuda = 'ASIG_DISCAP' | 'PAGO_NAC' | 'PARTO_MULT'

export type EstadoExpediente = 'REGISTRADO' | 'ASIGNADO' | 'EN_REVISION' | 'RESUELTO'

export type RolInteresado = 'SOLICITANTE' | 'REPRESENTANTE' | 'CAUSANTE'

export type CanalAviso = 'EMAIL' | 'SMS' | 'POSTAL'

export type TipoDocumento = 'DNI' | 'NIE' | 'PAS'

// ─── Auth ────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  nombre: string
  roles: RolUsuario[]
  idiomaPreferido: Idioma
}

// ─── Expediente (resumen en listado) ─────────────────────────────────────────

export interface ExpedienteResumen {
  id: number
  numeroExpediente: string
  tipoAyuda: { codigo: TipoAyuda; descripcion: string }
  estado: { codigo: EstadoExpediente; descripcion: string }
  fechaRegistro: string
  instructorNombre?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// ─── Filtros de búsqueda ──────────────────────────────────────────────────────

export interface ExpedienteFiltros {
  numeroExpediente?: string
  tipoAyuda?: TipoAyuda
  estado?: EstadoExpediente
  documentoSolicitante?: string
  fechaDesde?: string
  fechaHasta?: string
  page?: number
  size?: number
  sort?: string
}

// ─── Secciones del expediente ─────────────────────────────────────────────────

export interface SeccionGenerales {
  id: number
  numeroExpediente: string
  tipoAyuda: { codigo: TipoAyuda; descripcion: string }
  estado: { codigo: EstadoExpediente; descripcion: string }
  fechaRegistro: string
  fechaResolucion?: string
  instructorNombre?: string
}

export interface Persona {
  id: number
  tipoDocumento: TipoDocumento
  numeroDocumento: string
  nombre: string
  apellido1: string
  apellido2?: string
  fechaNacimiento?: string
}

export interface SeccionInteresados {
  solicitante: Persona
  representante?: Persona
  causantes: Persona[]
}

export interface SeccionContacto {
  via?: string
  numero?: string
  piso?: string
  codigoPostal?: string
  municipio?: string
  provincia?: string
  telefono?: string
  email?: string
  canalAviso?: CanalAviso
  idiomaNotificacion?: Idioma
  aceptaNotifTelematica: boolean
}

export interface SeccionAyuda {
  fechaSolicitud: string
  importeSolicitado?: number
  observaciones?: string
  detalle: DetalleAyuda
}

export type DetalleAyuda =
  | DetalleAsignacionDiscapacidad
  | DetalleNacimientoAdopcion
  | DetallePartoMultiple

export interface DetalleAsignacionDiscapacidad {
  tipo: 'ASIG_DISCAP'
  gradoDiscapacidad: number
  necesitaTerceraPersona: boolean
  causante: Persona
}

export interface DetalleNacimientoAdopcion {
  tipo: 'PAGO_NAC'
  supuesto: 'FAMILIA_NUMEROSA' | 'MONOPARENTAL' | 'DISCAPACIDAD'
  fechaHecho: string
  ingresosAnuales?: number
  numeroHijosCargo?: number
}

export interface DetallePartoMultiple {
  tipo: 'PARTO_MULT'
  numeroNacidos: number
  fechaHecho: string
}
