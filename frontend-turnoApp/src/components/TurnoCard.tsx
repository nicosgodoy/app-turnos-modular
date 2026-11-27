import type { ReactNode } from 'react'
import { Clock, MapPin, Stethoscope, User } from 'lucide-react'
import type { Turno } from '../lib/types'
import {
  diaNumero,
  formatHora,
  formatPrecio,
  mesCorto,
  tipoCoberturaLabels,
} from '../lib/format'
import { EstadoBadge } from './EstadoBadge'

interface TurnoCardProps {
  turno: Turno
  actions?: ReactNode
  mostrarPaciente?: boolean
}

export function TurnoCard({ turno, actions, mostrarPaciente = false }: TurnoCardProps) {
  return (
    <article className="card flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:gap-6">
      <div className="flex w-20 shrink-0 flex-col items-center justify-center rounded-2xl bg-forest px-3 py-3 text-paper">
        <span className="font-display text-3xl leading-none">{diaNumero(turno.fechaHora)}</span>
        <span className="mt-1 text-[0.62rem] font-bold tracking-[0.18em] text-paper/65">
          {mesCorto(turno.fechaHora)}
        </span>
        <span className="mt-1 text-sm font-semibold">{formatHora(turno.fechaHora)}</span>
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <EstadoBadge estado={turno.estado} />
          <span className="chip">{tipoCoberturaLabels[turno.tipoCobertura]}</span>
          {turno.precio !== null && turno.precio !== undefined && (
            <span className="chip border-clay/25 bg-clay/10 text-clay-deep">
              {formatPrecio(turno.precio)}
            </span>
          )}
        </div>

        <h3 className="mt-3 font-display text-xl leading-tight">
          {mostrarPaciente ? turno.paciente : turno.medico}
        </h3>
        <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-ink/60">
          {mostrarPaciente ? (
            <span className="inline-flex items-center gap-1.5">
              <User className="size-3.5" /> DNI {turno.pacienteDni}
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5">
              <Stethoscope className="size-3.5" /> {turno.especialidades || 'Consulta'}
            </span>
          )}
          <span className="inline-flex items-center gap-1.5">
            <Clock className="size-3.5" /> {turno.duracionMin} min
          </span>
          <span className="inline-flex items-center gap-1.5">
            <MapPin className="size-3.5" /> {turno.consultorio}
          </span>
          {turno.obraSocial && (
            <span className="inline-flex items-center gap-1.5">
              <span className="size-3.5 rounded-full border border-ink/30" /> {turno.obraSocial}
              {turno.plan ? ` · ${turno.plan}` : ''}
            </span>
          )}
        </div>
        {turno.motivo && <p className="mt-2 line-clamp-2 text-sm text-ink/50">{turno.motivo}</p>}
      </div>

      {actions && <div className="flex shrink-0 flex-wrap gap-2 sm:justify-end">{actions}</div>}
    </article>
  )
}
