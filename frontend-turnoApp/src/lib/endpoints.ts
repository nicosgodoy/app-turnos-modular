import { request } from './api'
import type {
  AuthResponse,
  Consultorio,
  Disponibilidad,
  Especialidad,
  Excepcion,
  Medico,
  MedicoResumen,
  MiAgenda,
  ObraSocial,
  OtpSolicitado,
  Paciente,
  Page,
  Plan,
  Slot,
  Turno,
  TipoCobertura,
  Usuario,
} from './types'

export interface LoginStaffPayload {
  email: string
  password: string
}

export interface OtpPayload {
  dni: string
  apellido: string
  nombre?: string
  email?: string
  telefono?: string
  fechaNacimiento?: string | null
  obraSocialId?: string | null
  planId?: string | null
  numeroAfiliado?: string | null
  consentimiento?: boolean
}

export interface ReservarPayload {
  medicoId: string
  fechaHora: string
  tipoCobertura?: TipoCobertura | null
  motivo?: string | null
}

export interface MedicoPayload {
  nombre: string
  apellido: string
  email: string
  password?: string
  matricula: string
  duracionTurnoMin: number
  precioParticular: number | null
  biografia: string | null
  especialidadIds: string[]
  coberturas: { obraSocialId: string; planId: string | null }[]
  activo?: boolean
}

export interface DisponibilidadPayload {
  consultorioId: string
  diaSemana: number
  horaDesde: string
  horaHasta: string
  vigenteDesde: string
  vigenteHasta: string | null
}

export interface ExcepcionPayload {
  fechaDesde: string
  fechaHasta: string
  todoElDia: boolean
  horaDesde: string | null
  horaHasta: string | null
  motivo: string | null
}

function query(params: Record<string, string | number | boolean | null | undefined>): string {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      search.set(key, String(value))
    }
  })
  const text = search.toString()
  return text ? `?${text}` : ''
}

export const authApi = {
  loginStaff: (body: LoginStaffPayload) =>
    request<AuthResponse>('/auth/staff/login', { method: 'POST', body, skipAuth: true }),
  solicitarOtp: (body: OtpPayload) =>
    request<OtpSolicitado>('/auth/paciente/otp', { method: 'POST', body, skipAuth: true }),
  verificarOtp: (dni: string, codigo: string) =>
    request<AuthResponse>('/auth/paciente/otp/verificar', {
      method: 'POST',
      body: { dni, codigo },
      skipAuth: true,
    }),
  refresh: () => request<AuthResponse>('/auth/refresh', { method: 'POST', skipAuth: true }),
  logout: () => request<void>('/auth/logout', { method: 'POST', skipAuth: true }),
  me: () => request<Usuario>('/auth/me'),
}

export const catalogoApi = {
  especialidades: () => request<Especialidad[]>('/especialidades'),
  obrasSociales: () => request<ObraSocial[]>('/obras-sociales'),
  medicos: (filtros: { especialidadId?: string; obraSocialId?: string; planId?: string } = {}) =>
    request<MedicoResumen[]>(`/medicos${query(filtros)}`),
  medico: (id: string) => request<Medico>(`/medicos/${id}`),
  slots: (id: string, fecha: string) => request<Slot[]>(`/medicos/${id}/slots${query({ fecha })}`),
}

export const pacienteApi = {
  perfil: () => request<Paciente>('/pacientes/me'),
  actualizar: (body: Partial<Paciente> & { obraSocialId?: string | null; planId?: string | null }) =>
    request<Paciente>('/pacientes/me', { method: 'PUT', body }),
  misTurnos: () => request<Turno[]>('/turnos/mios'),
  reservar: (body: ReservarPayload) => request<Turno>('/turnos', { method: 'POST', body }),
  cancelar: (id: string, motivo?: string) =>
    request<Turno>(`/turnos/${id}/cancelar`, { method: 'PATCH', body: { motivo: motivo ?? null } }),
  reprogramar: (id: string, fechaHora: string) =>
    request<Turno>(`/turnos/${id}/reprogramar`, { method: 'PATCH', body: { fechaHora } }),
}

export const medicoApi = {
  agenda: () => request<MiAgenda>('/medico/agenda'),
  turnos: (fecha: string) => request<Turno[]>(`/medico/turnos${query({ fecha })}`),
}

export const adminApi = {
  turnos: (params: {
    fecha?: string
    medicoId?: string
    estado?: string
    page?: number
    size?: number
  }) => request<Page<Turno>>(`/admin/turnos${query(params)}`),
  cambiarEstadoTurno: (id: string, estado: string, motivo?: string) =>
    request<Turno>(`/admin/turnos/${id}/estado`, {
      method: 'PATCH',
      body: { estado, motivo: motivo ?? null },
    }),
  cancelarTurno: (id: string, motivo?: string) =>
    request<Turno>(`/admin/turnos/${id}/cancelar${query({ motivo })}`, { method: 'PATCH' }),

  medicos: () => request<Medico[]>('/admin/medicos'),
  crearMedico: (body: MedicoPayload) =>
    request<Medico>('/admin/medicos', { method: 'POST', body }),
  actualizarMedico: (id: string, body: MedicoPayload) =>
    request<Medico>(`/admin/medicos/${id}`, { method: 'PUT', body }),
  cambiarEstadoMedico: (id: string, activo: boolean) =>
    request<Medico>(`/admin/medicos/${id}/estado${query({ activo })}`, { method: 'PATCH' }),

  especialidades: () => request<Especialidad[]>('/admin/especialidades'),
  crearEspecialidad: (body: { nombre: string; duracionDefaultMin: number }) =>
    request<Especialidad>('/admin/especialidades', { method: 'POST', body }),

  obrasSociales: () => request<ObraSocial[]>('/admin/obras-sociales'),
  crearObraSocial: (body: { nombre: string; codigo: string }) =>
    request<ObraSocial>('/admin/obras-sociales', { method: 'POST', body }),
  crearPlan: (obraSocialId: string, body: { nombre: string; codigo: string }) =>
    request<Plan>(`/admin/obras-sociales/${obraSocialId}/planes`, { method: 'POST', body }),

  consultorios: () => request<Consultorio[]>('/admin/consultorios'),
  crearConsultorio: (body: { nombre: string; ubicacion: string | null }) =>
    request<Consultorio>('/admin/consultorios', { method: 'POST', body }),

  disponibilidad: (medicoId: string) =>
    request<Disponibilidad[]>(`/admin/medicos/${medicoId}/disponibilidad`),
  crearDisponibilidad: (medicoId: string, body: DisponibilidadPayload) =>
    request<Disponibilidad>(`/admin/medicos/${medicoId}/disponibilidad`, { method: 'POST', body }),
  cambiarEstadoDisponibilidad: (id: string, activo: boolean) =>
    request<Disponibilidad>(`/admin/disponibilidad/${id}/estado${query({ activo })}`, {
      method: 'PATCH',
    }),

  excepciones: (medicoId: string) =>
    request<Excepcion[]>(`/admin/medicos/${medicoId}/excepciones`),
  crearExcepcion: (medicoId: string, body: ExcepcionPayload) =>
    request<Excepcion>(`/admin/medicos/${medicoId}/excepciones`, { method: 'POST', body }),
  eliminarExcepcion: (id: string) =>
    request<void>(`/admin/excepciones/${id}`, { method: 'DELETE' }),

  pacientes: (params: { q?: string; page?: number; size?: number }) =>
    request<Page<Paciente>>(`/admin/pacientes${query(params)}`),
  actualizarPaciente: (id: string, body: Record<string, unknown>) =>
    request<Paciente>(`/admin/pacientes/${id}`, { method: 'PUT', body }),
}
