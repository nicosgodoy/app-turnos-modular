export type Rol = 'ADMIN' | 'MEDICO' | 'PACIENTE'

export type EstadoTurno =
  | 'PENDIENTE'
  | 'CONFIRMADO'
  | 'ATENDIDO'
  | 'CANCELADO_PACIENTE'
  | 'CANCELADO_CLINICA'
  | 'AUSENTE'

export type TipoCobertura = 'OBRA_SOCIAL' | 'PARTICULAR'

export interface Usuario {
  id: string
  email: string
  nombre: string
  apellido: string
  rol: Rol
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  usuario: Usuario
}

export interface Especialidad {
  id: string
  nombre: string
  duracionDefaultMin: number
  activo: boolean
}

export interface Plan {
  id: string
  nombre: string
  codigo: string
  activo: boolean
}

export interface ObraSocial {
  id: string
  nombre: string
  codigo: string
  activo: boolean
  planes: Plan[]
}

export interface MedicoResumen {
  id: string
  nombre: string
  apellido: string
  matricula: string
  biografia: string | null
  duracionTurnoMin: number
  precioParticular: number | null
  especialidades: Especialidad[]
}

export interface Cobertura {
  obraSocialId: string
  obraSocial: string
  planId: string | null
  plan: string | null
}

export interface Medico extends MedicoResumen {
  email: string
  activo: boolean
  coberturas: Cobertura[]
}

export interface Slot {
  fechaHora: string
  consultorioId: string
  consultorio: string
  duracionMin: number
}

export interface Turno {
  id: string
  medicoId: string
  medico: string
  especialidades: string
  pacienteId: string
  paciente: string
  pacienteDni: string
  fechaHora: string
  duracionMin: number
  consultorio: string
  estado: EstadoTurno
  tipoCobertura: TipoCobertura
  obraSocial: string | null
  plan: string | null
  numeroAfiliado: string | null
  precio: number | null
  motivo: string | null
  cancelable: boolean
  creadoEn: string
}

export interface CoberturaResumen {
  id: string
  nombre: string
}

export interface Paciente {
  id: string
  nombre: string
  apellido: string
  dni: string
  fechaNacimiento: string | null
  telefono: string
  email: string
  obraSocial: CoberturaResumen | null
  plan: CoberturaResumen | null
  numeroAfiliado: string | null
  consentimientoDatos: boolean
}

export interface Page<T> {
  contenido: T[]
  pagina: number
  tamanio: number
  total: number
  totalPaginas: number
}

export interface Consultorio {
  id: string
  nombre: string
  ubicacion: string | null
  activo: boolean
}

export interface Disponibilidad {
  id: string
  medicoId: string
  consultorioId: string
  consultorio: string
  diaSemana: number
  horaDesde: string
  horaHasta: string
  vigenteDesde: string
  vigenteHasta: string | null
  activo: boolean
}

export interface Excepcion {
  id: string
  medicoId: string
  fechaDesde: string
  fechaHasta: string
  todoElDia: boolean
  horaDesde: string | null
  horaHasta: string | null
  motivo: string | null
}

export interface MiAgenda {
  perfil: Medico
  disponibilidad: Disponibilidad[]
  excepciones: Excepcion[]
}

export interface OtpSolicitado {
  mensaje: string
  expiraEnMinutos: number
  emailEnmascarado: string
}

export interface ProblemDetails {
  title?: string
  status?: number
  detail?: string
  codigo?: string
  instance?: string
}
