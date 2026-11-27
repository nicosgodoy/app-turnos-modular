import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  BadgeCheck,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Clock3,
  CreditCard,
  ShieldCheck,
} from 'lucide-react'
import { medicoApi } from '../lib/endpoints'
import {
  formatFecha,
  formatFechaLarga,
  horaCorta,
  hoyISO,
  iniciales,
  nombreDia,
  sumarDias,
} from '../lib/format'
import { Spinner } from '../components/Spinner'
import { EmptyState } from '../components/EmptyState'
import { TurnoCard } from '../components/TurnoCard'

export function MedicoPanelPage() {
  const [fecha, setFecha] = useState(hoyISO())

  const { data: agenda, isLoading: cargandoAgenda } = useQuery({
    queryKey: ['medico', 'agenda'],
    queryFn: medicoApi.agenda,
  })

  const { data: turnos = [], isFetching: cargandoTurnos } = useQuery({
    queryKey: ['medico', 'turnos', fecha],
    queryFn: () => medicoApi.turnos(fecha),
  })

  const resumen = useMemo(() => {
    const activos = turnos.filter(
      (turno) => turno.estado === 'PENDIENTE' || turno.estado === 'CONFIRMADO',
    )
    return {
      total: turnos.length,
      activos: activos.length,
      atendidos: turnos.filter((turno) => turno.estado === 'ATENDIDO').length,
    }
  }, [turnos])

  const disponibilidadActiva = useMemo(
    () =>
      (agenda?.disponibilidad ?? [])
        .filter((franja) => franja.activo)
        .sort((a, b) => a.diaSemana - b.diaSemana || a.horaDesde.localeCompare(b.horaDesde)),
    [agenda],
  )

  if (cargandoAgenda) {
    return (
      <div className="container-page py-20">
        <Spinner label="Cargando tu agenda…" />
      </div>
    )
  }

  const perfil = agenda?.perfil

  return (
    <div className="container-page py-10 sm:py-14">
      <div className="flex flex-wrap items-start justify-between gap-5">
        <div className="flex items-center gap-5">
          {perfil && (
            <span className="grid size-16 place-items-center rounded-3xl bg-sage-light font-display text-xl font-semibold text-forest">
              {iniciales(perfil.nombre, perfil.apellido)}
            </span>
          )}
          <div>
            <p className="eyebrow">Panel profesional</p>
            <h1 className="mt-1 flex items-center gap-2 text-3xl tracking-tight sm:text-4xl">
              {perfil ? `${perfil.nombre} ${perfil.apellido}` : 'Mi agenda'}
              <BadgeCheck className="size-5 text-forest" />
            </h1>
            {perfil && (
              <div className="mt-2 flex flex-wrap gap-1.5">
                {perfil.especialidades.map((especialidad) => (
                  <span key={especialidad.id} className="chip">
                    {especialidad.nombre}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
        {perfil && (
          <div className="flex gap-3">
            <div className="rounded-2xl bg-white/70 px-4 py-3 text-center">
              <Clock3 className="mx-auto size-4 text-clay" />
              <p className="mt-1 text-sm font-semibold">{perfil.duracionTurnoMin} min</p>
              <p className="text-[0.62rem] font-bold uppercase tracking-[0.14em] text-ink/40">
                Duración
              </p>
            </div>
            <div className="rounded-2xl bg-white/70 px-4 py-3 text-center">
              <CreditCard className="mx-auto size-4 text-clay" />
              <p className="mt-1 text-sm font-semibold">
                {perfil.precioParticular !== null
                  ? new Intl.NumberFormat('es-AR', {
                      style: 'currency',
                      currency: 'ARS',
                      maximumFractionDigits: 0,
                    }).format(perfil.precioParticular)
                  : '—'}
              </p>
              <p className="text-[0.62rem] font-bold uppercase tracking-[0.14em] text-ink/40">
                Particular
              </p>
            </div>
          </div>
        )}
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_360px] lg:items-start">
        <section className="card p-6 sm:p-8">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="eyebrow">Agenda del día</p>
              <h2 className="mt-1 font-display text-2xl capitalize">{formatFechaLarga(fecha)}</h2>
            </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                className="grid size-10 place-items-center rounded-full border border-ink/15 transition hover:bg-white"
                onClick={() => setFecha(sumarDias(fecha, -1))}
                aria-label="Día anterior"
              >
                <ChevronLeft className="size-4" />
              </button>
              <input
                type="date"
                className="field w-auto py-2"
                value={fecha}
                onChange={(event) => event.target.value && setFecha(event.target.value)}
                aria-label="Elegir fecha"
              />
              <button
                type="button"
                className="grid size-10 place-items-center rounded-full border border-ink/15 transition hover:bg-white"
                onClick={() => setFecha(sumarDias(fecha, 1))}
                aria-label="Día siguiente"
              >
                <ChevronRight className="size-4" />
              </button>
              {fecha !== hoyISO() && (
                <button type="button" className="btn-ghost px-4 py-2" onClick={() => setFecha(hoyISO())}>
                  Hoy
                </button>
              )}
            </div>
          </div>

          <div className="mt-5 flex flex-wrap gap-2">
            <span className="chip">{resumen.total} turnos</span>
            <span className="chip border-forest/20 bg-forest/5 text-forest">
              {resumen.activos} por atender
            </span>
            <span className="chip">{resumen.atendidos} atendidos</span>
          </div>

          <div className="mt-6 space-y-4">
            {cargandoTurnos ? (
              <Spinner label="Cargando turnos…" className="py-6" />
            ) : turnos.length === 0 ? (
              <EmptyState
                titulo="Sin turnos este día"
                descripcion="No hay reservas para la fecha seleccionada. Probá con otro día."
                icono={<CalendarDays className="size-6" />}
              />
            ) : (
              turnos.map((turno) => <TurnoCard key={turno.id} turno={turno} mostrarPaciente />)
            )}
          </div>
        </section>

        <aside className="space-y-6 lg:sticky lg:top-24">
          <section className="card p-6">
            <p className="eyebrow">Disponibilidad semanal</p>
            <h2 className="mt-1 font-display text-xl">Franjas de atención</h2>
            {disponibilidadActiva.length === 0 ? (
              <p className="mt-4 rounded-2xl border border-dashed border-ink/15 px-4 py-6 text-center text-sm text-ink/50">
                Sin franjas activas. Pedile a administración que cargue tu disponibilidad.
              </p>
            ) : (
              <ul className="mt-4 space-y-2">
                {disponibilidadActiva.map((franja) => (
                  <li
                    key={franja.id}
                    className="flex items-center justify-between gap-3 rounded-2xl bg-paper/70 px-4 py-3"
                  >
                    <div>
                      <p className="text-sm font-semibold">{nombreDia(franja.diaSemana)}</p>
                      <p className="text-xs text-ink/50">{franja.consultorio}</p>
                    </div>
                    <p className="text-sm font-semibold text-forest">
                      {horaCorta(franja.horaDesde)}–{horaCorta(franja.horaHasta)}
                    </p>
                  </li>
                ))}
              </ul>
            )}
            <p className="mt-4 text-xs leading-relaxed text-ink/45">
              Las franjas se gestionan desde administración. Vigencia desde{' '}
              {disponibilidadActiva[0] ? formatFecha(disponibilidadActiva[0].vigenteDesde) : '—'}.
            </p>
          </section>

          <section className="card p-6">
            <p className="eyebrow">Excepciones</p>
            <h2 className="mt-1 font-display text-xl">Licencias y bloqueos</h2>
            {(agenda?.excepciones ?? []).length === 0 ? (
              <p className="mt-4 rounded-2xl border border-dashed border-ink/15 px-4 py-6 text-center text-sm text-ink/50">
                No hay excepciones cargadas.
              </p>
            ) : (
              <ul className="mt-4 space-y-2">
                {(agenda?.excepciones ?? []).map((excepcion) => (
                  <li key={excepcion.id} className="rounded-2xl bg-paper/70 px-4 py-3">
                    <p className="text-sm font-semibold">
                      {formatFecha(excepcion.fechaDesde)}
                      {excepcion.fechaHasta !== excepcion.fechaDesde &&
                        ` → ${formatFecha(excepcion.fechaHasta)}`}
                    </p>
                    <p className="text-xs text-ink/50">
                      {excepcion.todoElDia
                        ? 'Todo el día'
                        : `${horaCorta(excepcion.horaDesde ?? '')}–${horaCorta(excepcion.horaHasta ?? '')}`}
                      {excepcion.motivo ? ` · ${excepcion.motivo}` : ''}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <div className="flex items-start gap-3 rounded-2xl bg-sage-light/70 p-4 text-sm text-forest">
            <ShieldCheck className="mt-0.5 size-5 shrink-0" />
            <p className="leading-relaxed">
              Los estados de los turnos (atendido / ausente) los gestiona administración desde el panel.
            </p>
          </div>
        </aside>
      </div>
    </div>
  )
}