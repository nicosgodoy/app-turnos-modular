import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft,
  BadgeCheck,
  CalendarDays,
  CheckCircle2,
  ChevronRight,
  Clock3,
  CreditCard,
  MapPin,
  ShieldCheck,
  Stethoscope,
} from 'lucide-react'
import { catalogoApi, pacienteApi } from '../lib/endpoints'
import type { ReservarPayload } from '../lib/endpoints'
import { useAuth } from '../auth/AuthContext'
import type { Slot, TipoCobertura, Turno } from '../lib/types'
import {
  formatFechaCorta,
  formatFechaLarga,
  formatHora,
  formatPrecio,
  hoyISO,
  iniciales,
  sumarDias,
} from '../lib/format'
import { Spinner } from '../components/Spinner'
import { EmptyState } from '../components/EmptyState'
import { ApiError } from '../lib/api'

type ModoCobertura = 'AUTO' | TipoCobertura

function SlotBoton({
  slot,
  activo,
  onClick,
}: {
  slot: Slot
  activo: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-xl border px-3 py-2.5 text-sm font-semibold transition ${
        activo
          ? 'border-forest bg-forest text-paper shadow-md'
          : 'border-ink/12 bg-white/75 text-ink hover:border-forest/50 hover:bg-white'
      }`}
    >
      {formatHora(slot.fechaHora)}
    </button>
  )
}

export function MedicoPage() {
  const { id = '' } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { usuario } = useAuth()

  const fecha = searchParams.get('fecha') || hoyISO()
  const hora = searchParams.get('hora') || ''
  const [modo, setModo] = useState<ModoCobertura>('AUTO')
  const [motivo, setMotivo] = useState('')
  const [turnoConfirmado, setTurnoConfirmado] = useState<Turno | null>(null)

  const {
    data: medico,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['medico', id],
    queryFn: () => catalogoApi.medico(id),
    enabled: Boolean(id),
  })

  const { data: slots = [], isFetching: cargandoSlots } = useQuery({
    queryKey: ['slots', id, fecha],
    queryFn: () => catalogoApi.slots(id, fecha),
    enabled: Boolean(id),
  })

  const { data: paciente } = useQuery({
    queryKey: ['paciente', 'me'],
    queryFn: pacienteApi.perfil,
    enabled: usuario?.rol === 'PACIENTE',
  })

  const dias = useMemo(() => Array.from({ length: 14 }, (_, indice) => sumarDias(hoyISO(), indice)), [])

  const aceptaCobertura = useMemo(() => {
    if (!medico || !paciente?.obraSocial) return false
    return medico.coberturas.some(
      (cobertura) =>
        cobertura.obraSocialId === paciente.obraSocial?.id &&
        (cobertura.planId === null || cobertura.planId === paciente.plan?.id),
    )
  }, [medico, paciente])

  const slotSeleccionado = slots.find((slot) => slot.fechaHora === hora) ?? null

  const cambiarFecha = (nuevaFecha: string) => {
    const params = new URLSearchParams(searchParams)
    params.set('fecha', nuevaFecha)
    params.delete('hora')
    setSearchParams(params, { replace: true })
  }

  const elegirSlot = (slot: Slot) => {
    const params = new URLSearchParams(searchParams)
    params.set('fecha', fecha)
    params.set('hora', slot.fechaHora)
    setSearchParams(params, { replace: true })
    setTurnoConfirmado(null)
  }

  const slotsManana = slots.filter((slot) => formatHora(slot.fechaHora) < '13:00')
  const slotsTarde = slots.filter((slot) => formatHora(slot.fechaHora) >= '13:00')

  const reserva = useMutation({
    mutationFn: (payload: ReservarPayload) => pacienteApi.reservar(payload),
    onSuccess: (turno) => {
      setTurnoConfirmado(turno)
      queryClient.invalidateQueries({ queryKey: ['mis-turnos'] })
      queryClient.invalidateQueries({ queryKey: ['slots', id] })
    },
  })

  const confirmar = () => {
    if (!slotSeleccionado) return
    if (usuario?.rol !== 'PACIENTE') {
      const redirect = encodeURIComponent(`/medicos/${id}?fecha=${fecha}&hora=${slotSeleccionado.fechaHora}`)
      navigate(`/ingresar?redirect=${redirect}`)
      return
    }
    reserva.mutate({
      medicoId: id,
      fechaHora: slotSeleccionado.fechaHora,
      tipoCobertura: modo === 'AUTO' ? null : modo,
      motivo: motivo.trim() || null,
    })
  }

  if (isLoading) {
    return (
      <div className="container-page py-20">
        <Spinner label="Cargando perfil…" />
      </div>
    )
  }

  if (isError || !medico) {
    return (
      <div className="container-page py-20">
        <EmptyState
          titulo="No encontramos a ese profesional"
          descripcion="Puede que el perfil ya no esté disponible. Volvé a la búsqueda para ver otros médicos."
          icono={<Stethoscope className="size-6" />}
          accion={
            <Link to="/" className="btn-primary">
              Volver a la búsqueda
            </Link>
          }
        />
      </div>
    )
  }

  return (
    <div className="container-page py-10 sm:py-14">
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-sm font-semibold text-ink/55 transition hover:text-ink"
      >
        <ArrowLeft className="size-4" /> Volver a la búsqueda
      </Link>

      <div className="mt-6 grid gap-8 lg:grid-cols-[1fr_400px] lg:items-start">
        <div>
          <section className="card p-6 sm:p-8">
            <div className="flex flex-wrap items-start gap-5">
              <span className="grid size-20 place-items-center rounded-3xl bg-sage-light font-display text-2xl font-semibold text-forest">
                {iniciales(medico.nombre, medico.apellido)}
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <h1 className="font-display text-3xl tracking-tight sm:text-4xl">
                    {medico.nombre} {medico.apellido}
                  </h1>
                  <BadgeCheck className="size-5 text-forest" />
                </div>
                <p className="mt-1 text-sm font-semibold uppercase tracking-[0.12em] text-ink/45">
                  Matrícula {medico.matricula}
                </p>
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {medico.especialidades.map((especialidad) => (
                    <span key={especialidad.id} className="chip">
                      {especialidad.nombre}
                    </span>
                  ))}
                </div>
              </div>
            </div>

            {medico.biografia && (
              <p className="mt-6 text-sm leading-relaxed text-ink/65">{medico.biografia}</p>
            )}

            <div className="mt-6 grid gap-3 sm:grid-cols-3">
              <div className="rounded-2xl bg-paper/80 p-4">
                <Clock3 className="size-4 text-clay" />
                <p className="mt-2 text-[0.65rem] font-bold uppercase tracking-[0.14em] text-ink/40">
                  Duración
                </p>
                <p className="text-sm font-semibold">{medico.duracionTurnoMin} minutos</p>
              </div>
              <div className="rounded-2xl bg-paper/80 p-4">
                <CreditCard className="size-4 text-clay" />
                <p className="mt-2 text-[0.65rem] font-bold uppercase tracking-[0.14em] text-ink/40">
                  Particular
                </p>
                <p className="text-sm font-semibold">{formatPrecio(medico.precioParticular)}</p>
              </div>
              <div className="rounded-2xl bg-paper/80 p-4">
                <MapPin className="size-4 text-clay" />
                <p className="mt-2 text-[0.65rem] font-bold uppercase tracking-[0.14em] text-ink/40">
                  Sede
                </p>
                <p className="text-sm font-semibold">Gral. Paz y Libertad 3</p>
              </div>
            </div>

            {medico.coberturas.length > 0 && (
              <div className="mt-6">
                <p className="label">Coberturas que acepta</p>
                <div className="flex flex-wrap gap-1.5">
                  {medico.coberturas.map((cobertura) => (
                    <span
                      key={`${cobertura.obraSocialId}-${cobertura.planId ?? 'todos'}`}
                      className="chip border-forest/20 bg-forest/5 text-forest"
                    >
                      <ShieldCheck className="size-3.5" />
                      {cobertura.obraSocial}
                      {cobertura.plan ? ` · ${cobertura.plan}` : ''}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </section>

          <section className="card mt-6 p-6 sm:p-8">
            <div className="flex flex-wrap items-end justify-between gap-3">
              <div>
                <p className="eyebrow">Agenda</p>
                <h2 className="mt-1 font-display text-2xl">Elegí un horario</h2>
              </div>
              <p className="text-sm text-ink/50">{formatFechaLarga(fecha)}</p>
            </div>

            <div className="mt-5 flex gap-2 overflow-x-auto pb-2">
              {dias.map((dia) => {
                const activo = dia === fecha
                const partes = dia.split('-')
                return (
                  <button
                    key={dia}
                    type="button"
                    onClick={() => cambiarFecha(dia)}
                    className={`flex min-w-[4.4rem] flex-col items-center rounded-2xl border px-3 py-2.5 transition ${
                      activo
                        ? 'border-forest bg-forest text-paper'
                        : 'border-ink/10 bg-white/70 hover:border-forest/40'
                    }`}
                  >
                    <span
                      className={`text-[0.62rem] font-bold uppercase tracking-[0.12em] ${
                        activo ? 'text-paper/60' : 'text-ink/40'
                      }`}
                    >
                      {formatFechaCorta(dia).split(' ')[0]}
                    </span>
                    <span className="font-display text-xl leading-tight">{Number(partes[2])}</span>
                    <span
                      className={`text-[0.6rem] uppercase ${
                        activo ? 'text-paper/60' : 'text-ink/40'
                      }`}
                    >
                      {formatFechaCorta(dia).split(' ').slice(1).join(' ')}
                    </span>
                  </button>
                )
              })}
            </div>

            {cargandoSlots ? (
              <Spinner label="Buscando horarios…" className="py-8" />
            ) : slots.length === 0 ? (
              <div className="mt-6 rounded-2xl border border-dashed border-ink/15 px-5 py-10 text-center">
                <CalendarDays className="mx-auto size-7 text-ink/30" />
                <p className="mt-3 font-display text-xl">Sin horarios este día</p>
                <p className="mt-1 text-sm text-ink/50">
                  Probá con otra fecha del calendario. La agenda muestra los próximos 14 días.
                </p>
              </div>
            ) : (
              <div className="mt-6 space-y-6">
                {slotsManana.length > 0 && (
                  <div>
                    <p className="label">Mañana</p>
                    <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-5">
                      {slotsManana.map((slot) => (
                        <SlotBoton
                          key={slot.fechaHora}
                          slot={slot}
                          activo={slot.fechaHora === hora}
                          onClick={() => elegirSlot(slot)}
                        />
                      ))}
                    </div>
                  </div>
                )}
                {slotsTarde.length > 0 && (
                  <div>
                    <p className="label">Tarde</p>
                    <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-5">
                      {slotsTarde.map((slot) => (
                        <SlotBoton
                          key={slot.fechaHora}
                          slot={slot}
                          activo={slot.fechaHora === hora}
                          onClick={() => elegirSlot(slot)}
                        />
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </section>
        </div>

        <aside className="lg:sticky lg:top-24">
          {turnoConfirmado ? (
            <div className="card overflow-hidden">
              <div className="bg-forest px-6 py-8 text-paper">
                <CheckCircle2 className="size-9 text-gold" />
                <h2 className="mt-4 font-display text-3xl">Turno confirmado</h2>
                <p className="mt-2 text-sm text-paper/70">
                  Te enviamos el detalle por email. También podés verlo en “Mis turnos”.
                </p>
              </div>
              <div className="space-y-4 p-6">
                <div>
                  <p className="label">Profesional</p>
                  <p className="font-display text-xl">{turnoConfirmado.medico}</p>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <p className="label">Fecha</p>
                    <p className="text-sm font-semibold">{formatFechaLarga(turnoConfirmado.fechaHora)}</p>
                  </div>
                  <div>
                    <p className="label">Hora</p>
                    <p className="text-sm font-semibold">{formatHora(turnoConfirmado.fechaHora)}</p>
                  </div>
                </div>
                <div>
                  <p className="label">Lugar</p>
                  <p className="text-sm font-semibold">{turnoConfirmado.consultorio}</p>
                  <p className="text-sm text-ink/50">Gral. Paz y Libertad 3</p>
                </div>
                <div>
                  <p className="label">Cobertura</p>
                  <p className="text-sm font-semibold">
                    {turnoConfirmado.tipoCobertura === 'PARTICULAR'
                      ? `Particular · ${formatPrecio(turnoConfirmado.precio)}`
                      : `${turnoConfirmado.obraSocial ?? 'Obra social'}${turnoConfirmado.plan ? ` · ${turnoConfirmado.plan}` : ''}`}
                  </p>
                </div>
                <div className="flex flex-col gap-2 pt-2">
                  <Link to="/mis-turnos" className="btn-primary w-full">
                    Ver mis turnos
                  </Link>
                  <button
                    type="button"
                    className="btn-ghost w-full"
                    onClick={() => {
                      setTurnoConfirmado(null)
                      setMotivo('')
                    }}
                  >
                    Reservar otro turno
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="card p-6">
              <p className="eyebrow">Reserva</p>
              <h2 className="mt-1 font-display text-2xl">
                {slotSeleccionado ? 'Confirmá tu turno' : 'Elegí un horario'}
              </h2>

              {slotSeleccionado ? (
                <div className="mt-5 space-y-5">
                  <div className="rounded-2xl bg-forest px-4 py-4 text-paper">
                    <p className="text-[0.62rem] font-bold uppercase tracking-[0.16em] text-paper/55">
                      Turno seleccionado
                    </p>
                    <p className="mt-1 font-display text-2xl">
                      {formatFechaLarga(fecha)} · {formatHora(slotSeleccionado.fechaHora)}
                    </p>
                    <p className="mt-1 text-sm text-paper/65">
                      {slotSeleccionado.consultorio} · {slotSeleccionado.duracionMin} min
                    </p>
                  </div>

                  {usuario?.rol === 'PACIENTE' ? (
                    <>
                      <div className="rounded-2xl border border-ink/10 bg-paper/60 p-4 text-sm">
                        {paciente?.obraSocial ? (
                          <>
                            <p className="font-semibold">
                              Tu cobertura: {paciente.obraSocial.nombre}
                              {paciente.plan ? ` · ${paciente.plan.nombre}` : ''}
                            </p>
                            <p className="mt-1 text-ink/55">
                              {aceptaCobertura
                                ? 'Este profesional acepta tu cobertura.'
                                : 'Este profesional no acepta tu cobertura: el turno será particular.'}
                            </p>
                          </>
                        ) : (
                          <p className="text-ink/55">
                            No tenés una cobertura cargada. El turno será particular.
                          </p>
                        )}
                      </div>

                      <div>
                        <p className="label">Cobertura del turno</p>
                        <div className="space-y-2">
                          <label className="flex cursor-pointer items-start gap-3 rounded-xl border border-ink/10 bg-white/60 px-3 py-2.5">
                            <input
                              type="radio"
                              name="cobertura"
                              checked={modo === 'AUTO'}
                              onChange={() => setModo('AUTO')}
                              className="mt-1 accent-[#244b3d]"
                            />
                            <span>
                              <span className="block text-sm font-semibold">Que decida el sistema</span>
                              <span className="block text-xs text-ink/50">
                                Usa tu obra social si el médico la acepta; si no, particular.
                              </span>
                            </span>
                          </label>
                          {paciente?.obraSocial && (
                            <label
                              className={`flex cursor-pointer items-start gap-3 rounded-xl border px-3 py-2.5 ${
                                aceptaCobertura
                                  ? 'border-ink/10 bg-white/60'
                                  : 'cursor-not-allowed border-ink/5 bg-ink/5 opacity-60'
                              }`}
                            >
                              <input
                                type="radio"
                                name="cobertura"
                                checked={modo === 'OBRA_SOCIAL'}
                                onChange={() => setModo('OBRA_SOCIAL')}
                                disabled={!aceptaCobertura}
                                className="mt-1 accent-[#244b3d]"
                              />
                              <span>
                                <span className="block text-sm font-semibold">Obra social</span>
                                <span className="block text-xs text-ink/50">
                                  {aceptaCobertura
                                    ? `${paciente.obraSocial.nombre}${paciente.plan ? ` · ${paciente.plan.nombre}` : ''}`
                                    : 'El profesional no acepta esta cobertura.'}
                                </span>
                              </span>
                            </label>
                          )}
                          <label className="flex cursor-pointer items-start gap-3 rounded-xl border border-ink/10 bg-white/60 px-3 py-2.5">
                            <input
                              type="radio"
                              name="cobertura"
                              checked={modo === 'PARTICULAR'}
                              onChange={() => setModo('PARTICULAR')}
                              className="mt-1 accent-[#244b3d]"
                            />
                            <span>
                              <span className="block text-sm font-semibold">Particular</span>
                              <span className="block text-xs text-ink/50">
                                {formatPrecio(medico.precioParticular)} · sin pago online
                              </span>
                            </span>
                          </label>
                        </div>
                      </div>

                      <div>
                        <label htmlFor="motivo" className="label">
                          Motivo de la consulta
                        </label>
                        <textarea
                          id="motivo"
                          rows={3}
                          className="field resize-none"
                          placeholder="Ej.: control anual, dolor de cabeza…"
                          value={motivo}
                          onChange={(event) => setMotivo(event.target.value)}
                          maxLength={500}
                        />
                      </div>

                      {reserva.isError && (
                        <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
                          {reserva.error instanceof ApiError
                            ? reserva.error.message
                            : 'No pudimos reservar el turno. Intentá de nuevo.'}
                        </p>
                      )}

                      <button
                        type="button"
                        onClick={confirmar}
                        disabled={reserva.isPending}
                        className="btn-clay w-full"
                      >
                        {reserva.isPending ? 'Reservando…' : 'Confirmar turno'}
                        {!reserva.isPending && <ChevronRight className="size-4" />}
                      </button>
                      <p className="text-center text-xs leading-relaxed text-ink/45">
                        Al confirmar aceptás el uso de tus datos para gestionar el turno, según la Ley
                        25.326.
                      </p>
                    </>
                  ) : (
                    <div className="space-y-4">
                      <p className="text-sm leading-relaxed text-ink/60">
                        Para reservar necesitás identificarte con tu DNI y un código que te enviamos por
                        email. Es rápido y no requiere contraseña.
                      </p>
                      <Link
                        to={`/ingresar?redirect=${encodeURIComponent(
                          `/medicos/${id}?fecha=${fecha}&hora=${slotSeleccionado.fechaHora}`,
                        )}`}
                        className="btn-primary w-full"
                      >
                        Ingresar y reservar
                      </Link>
                      {usuario && (
                        <p className="text-center text-xs text-ink/45">
                          Estás con una cuenta de {usuario.rol === 'ADMIN' ? 'administración' : 'médico'}.
                          Cerrá sesión para reservar como paciente.
                        </p>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <div className="mt-5 rounded-2xl border border-dashed border-ink/15 px-5 py-10 text-center">
                  <CalendarDays className="mx-auto size-7 text-ink/30" />
                  <p className="mt-3 text-sm text-ink/55">
                    Seleccioná uno de los horarios disponibles para ver el resumen y confirmar.
                  </p>
                </div>
              )}
            </div>
          )}

          <div className="mt-4 flex items-start gap-3 rounded-2xl bg-sage-light/70 p-4 text-sm text-forest">
            <ShieldCheck className="mt-0.5 size-5 shrink-0" />
            <p className="leading-relaxed">
              Podés cancelar o reprogramar hasta <strong>24 horas antes</strong> desde “Mis turnos”.
            </p>
          </div>
        </aside>
      </div>
    </div>
  )
}
