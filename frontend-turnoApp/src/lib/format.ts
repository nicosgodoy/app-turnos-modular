import type { EstadoTurno, TipoCobertura } from './types'

const ZONA = 'America/Argentina/Buenos_Aires'

export function parseFecha(fecha: string): Date {
  if (/^\d{4}-\d{2}-\d{2}$/.test(fecha)) {
    const [anio, mes, dia] = fecha.split('-').map(Number)
    return new Date(anio, mes - 1, dia)
  }
  return new Date(fecha)
}

export function hoyISO(): string {
  return fechaISO(new Date())
}

export function fechaISO(fecha: Date): string {
  const anio = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${anio}-${mes}-${dia}`
}

export function sumarDias(fecha: string, dias: number): string {
  const base = parseFecha(fecha)
  base.setDate(base.getDate() + dias)
  return fechaISO(base)
}

export function formatFecha(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(parseFecha(fecha))
}

function esSoloFecha(fecha: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(fecha)
}

export function formatFechaLarga(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    ...(esSoloFecha(fecha) ? {} : { timeZone: ZONA }),
  }).format(parseFecha(fecha))
}

export function formatFechaCorta(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    ...(esSoloFecha(fecha) ? {} : { timeZone: ZONA }),
  }).format(parseFecha(fecha))
}

export function diaNumero(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    ...(esSoloFecha(fecha) ? {} : { timeZone: ZONA }),
  }).format(parseFecha(fecha))
}

export function mesCorto(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    month: 'short',
    ...(esSoloFecha(fecha) ? {} : { timeZone: ZONA }),
  })
    .format(parseFecha(fecha))
    .replace('.', '')
    .toUpperCase()
}

export function formatHora(fecha: string): string {
  return new Intl.DateTimeFormat('es-AR', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZone: ZONA,
  }).format(parseFecha(fecha))
}

export function formatPrecio(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return '—'
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(valor)
}

export function iniciales(nombre: string, apellido: string): string {
  return `${nombre.charAt(0)}${apellido.charAt(0)}`.toUpperCase()
}

export function nombreDia(dia: number): string {
  return ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'][dia] ?? '—'
}

export function horaCorta(hora: string): string {
  return hora.slice(0, 5)
}

export const estadoLabels: Record<EstadoTurno, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  ATENDIDO: 'Atendido',
  CANCELADO_PACIENTE: 'Cancelado por el paciente',
  CANCELADO_CLINICA: 'Cancelado por la clínica',
  AUSENTE: 'Ausente',
}

export function estadoTone(estado: EstadoTurno): string {
  switch (estado) {
    case 'CONFIRMADO':
      return 'bg-forest/10 text-forest border-forest/20'
    case 'PENDIENTE':
      return 'bg-gold/15 text-[#8a6414] border-gold/30'
    case 'ATENDIDO':
      return 'bg-sage-light text-forest border-sage/40'
    case 'AUSENTE':
      return 'bg-ink/10 text-ink/70 border-ink/15'
    default:
      return 'bg-clay/10 text-clay-deep border-clay/25'
  }
}

export const tipoCoberturaLabels: Record<TipoCobertura, string> = {
  OBRA_SOCIAL: 'Obra social',
  PARTICULAR: 'Particular',
}

export function esTurnoFuturo(turno: { fechaHora: string; estado: EstadoTurno }): boolean {
  return (
    new Date(turno.fechaHora).getTime() > Date.now() &&
    turno.estado !== 'CANCELADO_PACIENTE' &&
    turno.estado !== 'CANCELADO_CLINICA'
  )
}
