import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CalendarClock, CalendarDays, Pencil, Phone, Search, ShieldCheck, XCircle } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { catalogoApi, pacienteApi } from '../lib/endpoints'
import { ApiError } from '../lib/api'
import type { Paciente, Turno } from '../lib/types'
import { esTurnoFuturo, formatFecha, formatHora, hoyISO, sumarDias } from '../lib/format'
import { Spinner } from '../components/Spinner'
import { EmptyState } from '../components/EmptyState'
import { TurnoCard } from '../components/TurnoCard'
import { Modal } from '../components/Modal'

function mensajeError(error: unknown, fallback: string): string {
  return error instanceof ApiError ? error.message : fallback
}

export function MisTurnosPage() {
  const { usuario } = useAuth()
  const queryClient = useQueryClient()

  const [turnoACancelar, setTurnoACancelar] = useState<Turno | null>(null)
  const [motivoCancelacion, setMotivoCancelacion] = useState('')
  const [turnoAReprogramar, setTurnoAReprogramar] = useState<Turno | null>(null)
  const [fechaNueva, setFechaNueva] = useState(hoyISO())
  const [horaNueva, setHoraNueva] = useState('')
  const [datosAbiertos, setDatosAbiertos] = useState(false)

  const { data: turnos = [], isLoading, isError } = useQuery({
    queryKey: ['mis-turnos'],
    queryFn: pacienteApi.misTurnos,
  })

  const { data: paciente } = useQuery({
    queryKey: ['paciente', 'me'],
    queryFn: pacienteApi.perfil,
  })

  const proximos = useMemo(
    () =>
      turnos
        .filter(esTurnoFuturo)
        .sort((a, b) => a.fechaHora.localeCompare(b.fechaHora)),
    [turnos],
  )

  const historial = useMemo(
    () =>
      turnos
        .filter((turno) => !esTurnoFuturo(turno))
        .sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)),
    [turnos],
  )

  const invalidarTurnos = () => {
    queryClient.invalidateQueries({ queryKey: ['mis-turnos'] })
    queryClient.invalidateQueries({ queryKey: ['slots'] })
  }

  const cancelar = useMutation({
    mutationFn: () => pacienteApi.cancelar(turnoACancelar!.id, motivoCancelacion.trim() || undefined),
    onSuccess: () => {
      invalidarTurnos()
      setTurnoACancelar(null)
      setMotivoCancelacion('')
    },
  })

  const { data: slots = [], isFetching: cargandoSlots } = useQuery({
    queryKey: ['slots', turnoAReprogramar?.medicoId, fechaNueva],
    queryFn: () => catalogoApi.slots(turnoAReprogramar!.medicoId, fechaNueva),
    enabled: Boolean(turnoAReprogramar),
  })

  const reprogramar = useMutation({
    mutationFn: () => pacienteApi.reprogramar(turnoAReprogramar!.id, horaNueva),
    onSuccess: () => {
      invalidarTurnos()
      setTurnoAReprogramar(null)
      setHoraNueva('')
    },
  })

  const abrirReprogramar = (turno: Turno) => {
    setTurnoAReprogramar(turno)
    setFechaNueva(hoyISO())
    setHoraNueva('')
    reprogramar.reset()
  }

  const abrirCancelar = (turno: Turno) => {
    setTurnoACancelar(turno)
    setMotivoCancelacion('')
    cancelar.reset()
  }

  if (isLoading) {
    return (
      <div className="container-page py-20">
        <Spinner label="Cargando tus turnos…" />
      </div>
    )
  }

  return (
    <div className="container-page py-10 sm:py-14">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">Pacientes</p>
          <h1 className="mt-2 text-4xl tracking-tight sm:text-5xl">Mis turnos</h1>
          <p className="mt-2 text-sm text-ink/55">
            Hola, {usuario?.nombre}. Acá podés cancelar o reprogramar hasta 24 horas antes.
          </p>
        </div>
        <Link to="/" className="btn-primary">
          <Search className="size-4" /> Reservar un turno
        </Link>
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_340px] lg:items-start">
        <div className="space-y-10">
          <section>
            <h2 className="font-display text-2xl">Próximos</h2>
            <div className="mt-4 space-y-4">
              {isError && (
                <EmptyState
                  titulo="No pudimos cargar tus turnos"
                  descripcion="Revisá que el backend esté funcionando e intentá de nuevo en unos segundos."
                />
              )}
              {!isError && proximos.length === 0 && (
                <EmptyState
                  titulo="No tenés turnos próximos"
                  descripcion="Buscá un profesional por especialidad u obra social y reservá en minutos."
                  icono={<CalendarDays className="size-6" />}
                  accion={
                    <Link to="/" className="btn-primary">
                      Buscar turnos
                    </Link>
                  }
                />
              )}
              {proximos.map((turno) => (
                <TurnoCard
                  key={turno.id}
                  turno={turno}
                  actions={
                    turno.cancelable ? (
                      <>
                        <button
                          type="button"
                          className="btn-ghost px-4 py-2"
                          onClick={() => abrirReprogramar(turno)}
                        >
                          <CalendarClock className="size-4" /> Reprogramar
                        </button>
                        <button
                          type="button"
                          className="btn-ghost px-4 py-2 text-clay-deep hover:border-clay/50"
                          onClick={() => abrirCancelar(turno)}
                        >
                          <XCircle className="size-4" /> Cancelar
                        </button>
                      </>
                    ) : undefined
                  }
                />
              ))}
            </div>
          </section>

          {historial.length > 0 && (
            <section>
              <h2 className="font-display text-2xl">Historial</h2>
              <div className="mt-4 space-y-4">
                {historial.map((turno) => (
                  <TurnoCard key={turno.id} turno={turno} />
                ))}
              </div>
            </section>
          )}
        </div>

        <aside className="space-y-4 lg:sticky lg:top-24">
          <div className="card p-6">
            <div className="flex items-center justify-between gap-3">
              <p className="eyebrow">Mis datos</p>
              <button
                type="button"
                className="btn-ghost px-3 py-1.5 text-xs"
                onClick={() => setDatosAbiertos(true)}
              >
                <Pencil className="size-3.5" /> Editar
              </button>
            </div>
            {paciente ? (
              <dl className="mt-4 space-y-3 text-sm">
                <div>
                  <dt className="label">Nombre</dt>
                  <dd className="font-semibold">
                    {paciente.nombre} {paciente.apellido}
                  </dd>
                </div>
                <div>
                  <dt className="label">DNI</dt>
                  <dd className="font-semibold">{paciente.dni}</dd>
                </div>
                <div>
                  <dt className="label">Email</dt>
                  <dd className="break-all font-semibold">{paciente.email}</dd>
                </div>
                <div>
                  <dt className="label">Teléfono</dt>
                  <dd className="font-semibold">{paciente.telefono || '—'}</dd>
                </div>
                <div>
                  <dt className="label">Cobertura</dt>
                  <dd className="font-semibold">
                    {paciente.obraSocial
                      ? `${paciente.obraSocial.nombre}${paciente.plan ? ` · ${paciente.plan.nombre}` : ''}`
                      : 'Sin cobertura (particular)'}
                  </dd>
                  {paciente.numeroAfiliado && (
                    <dd className="text-xs text-ink/50">Afiliado N° {paciente.numeroAfiliado}</dd>
                  )}
                </div>
              </dl>
            ) : (
              <Spinner label="Cargando datos…" className="mt-4" />
            )}
          </div>

          <div className="flex items-start gap-3 rounded-2xl bg-sage-light/70 p-4 text-sm text-forest">
            <ShieldCheck className="mt-0.5 size-5 shrink-0" />
            <p className="leading-relaxed">
              Te enviamos la confirmación por email y un recordatorio <strong>24 h antes</strong> de
              cada turno.
            </p>
          </div>
        </aside>
      </div>

      <Modal
        abierto={Boolean(turnoACancelar)}
        onCerrar={() => setTurnoACancelar(null)}
        titulo="Cancelar turno"
        descripcion={
          turnoACancelar
            ? `${turnoACancelar.medico} · ${formatFecha(turnoACancelar.fechaHora)} · ${formatHora(turnoACancelar.fechaHora)}`
            : undefined
        }
        ancho="sm"
      >
        <form
          onSubmit={(event: FormEvent) => {
            event.preventDefault()
            cancelar.mutate()
          }}
          className="space-y-4"
        >
          <p className="text-sm leading-relaxed text-ink/60">
            El horario queda libre para otro paciente y te avisamos por email. Podés volver a reservar
            cuando quieras.
          </p>
          <div>
            <label htmlFor="motivo-cancelacion" className="label">
              Motivo (opcional)
            </label>
            <textarea
              id="motivo-cancelacion"
              rows={3}
              className="field resize-none"
              maxLength={500}
              placeholder="Ej.: no puedo asistir ese día…"
              value={motivoCancelacion}
              onChange={(event) => setMotivoCancelacion(event.target.value)}
            />
          </div>
          {cancelar.isError && (
            <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
              {mensajeError(cancelar.error, 'No pudimos cancelar el turno. Intentá de nuevo.')}
            </p>
          )}
          <div className="flex gap-2">
            <button
              type="button"
              className="btn-ghost flex-1"
              onClick={() => setTurnoACancelar(null)}
            >
              Volver
            </button>
            <button type="submit" disabled={cancelar.isPending} className="btn-clay flex-1">
              {cancelar.isPending ? 'Cancelando…' : 'Cancelar turno'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        abierto={Boolean(turnoAReprogramar)}
        onCerrar={() => setTurnoAReprogramar(null)}
        titulo="Reprogramar turno"
        descripcion={
          turnoAReprogramar
            ? `${turnoAReprogramar.medico} · turno actual: ${formatFecha(turnoAReprogramar.fechaHora)} · ${formatHora(turnoAReprogramar.fechaHora)}`
            : undefined
        }
      >
        <div className="space-y-5">
          <div>
            <label htmlFor="fecha-nueva" className="label">
              Nueva fecha
            </label>
            <input
              id="fecha-nueva"
              type="date"
              className="field"
              min={hoyISO()}
              max={sumarDias(hoyISO(), 90)}
              value={fechaNueva}
              onChange={(event) => {
                setFechaNueva(event.target.value)
                setHoraNueva('')
              }}
            />
          </div>

          <div>
            <p className="label">Horarios disponibles</p>
            {cargandoSlots ? (
              <Spinner label="Buscando horarios…" className="py-4" />
            ) : slots.length === 0 ? (
              <p className="rounded-2xl border border-dashed border-ink/15 px-4 py-6 text-center text-sm text-ink/50">
                Sin horarios ese día. Probá con otra fecha.
              </p>
            ) : (
              <div className="grid max-h-56 grid-cols-3 gap-2 overflow-y-auto sm:grid-cols-4">
                {slots.map((slot) => (
                  <button
                    key={slot.fechaHora}
                    type="button"
                    onClick={() => setHoraNueva(slot.fechaHora)}
                    className={`rounded-xl border px-3 py-2.5 text-sm font-semibold transition ${
                      horaNueva === slot.fechaHora
                        ? 'border-forest bg-forest text-paper shadow-md'
                        : 'border-ink/12 bg-white/75 text-ink hover:border-forest/50'
                    }`}
                  >
                    {formatHora(slot.fechaHora)}
                  </button>
                ))}
              </div>
            )}
          </div>

          {reprogramar.isError && (
            <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
              {mensajeError(reprogramar.error, 'No pudimos reprogramar el turno. Intentá de nuevo.')}
            </p>
          )}

          <div className="flex gap-2">
            <button
              type="button"
              className="btn-ghost flex-1"
              onClick={() => setTurnoAReprogramar(null)}
            >
              Volver
            </button>
            <button
              type="button"
              disabled={!horaNueva || reprogramar.isPending}
              className="btn-primary flex-1"
              onClick={() => reprogramar.mutate()}
            >
              {reprogramar.isPending ? 'Reprogramando…' : 'Confirmar cambio'}
            </button>
          </div>
        </div>
      </Modal>

      <MisDatosModal
        abierto={datosAbiertos}
        onCerrar={() => setDatosAbiertos(false)}
        paciente={paciente ?? null}
      />
    </div>
  )
}

function MisDatosModal({
  abierto,
  onCerrar,
  paciente,
}: {
  abierto: boolean
  onCerrar: () => void
  paciente: Paciente | null
}) {
  const queryClient = useQueryClient()
  const [telefono, setTelefono] = useState('')
  const [obraSocialId, setObraSocialId] = useState('')
  const [planId, setPlanId] = useState('')
  const [numeroAfiliado, setNumeroAfiliado] = useState('')
  const [inicializado, setInicializado] = useState(false)

  const { data: obrasSociales = [] } = useQuery({
    queryKey: ['obras-sociales'],
    queryFn: catalogoApi.obrasSociales,
    enabled: abierto,
  })

  if (abierto && paciente && !inicializado) {
    setTelefono(paciente.telefono ?? '')
    setObraSocialId(paciente.obraSocial?.id ?? '')
    setPlanId(paciente.plan?.id ?? '')
    setNumeroAfiliado(paciente.numeroAfiliado ?? '')
    setInicializado(true)
  }
  if (!abierto && inicializado) {
    setInicializado(false)
  }

  const planes = obrasSociales.find((obra) => obra.id === obraSocialId)?.planes ?? []

  const guardar = useMutation({
    mutationFn: () =>
      pacienteApi.actualizar({
        telefono: telefono.trim(),
        obraSocialId: obraSocialId || null,
        planId: planId || null,
        numeroAfiliado: numeroAfiliado.trim() || null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['paciente', 'me'] })
      onCerrar()
    },
  })

  return (
    <Modal
      abierto={abierto}
      onCerrar={onCerrar}
      titulo="Editar mis datos"
      descripcion="Podés actualizar tu teléfono y tu cobertura médica."
      ancho="sm"
    >
      <form
        onSubmit={(event: FormEvent) => {
          event.preventDefault()
          guardar.mutate()
        }}
        className="space-y-4"
      >
        <div>
          <label htmlFor="mi-telefono" className="label">
            Teléfono
          </label>
          <div className="relative">
            <Phone className="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-ink/35" />
            <input
              id="mi-telefono"
              className="field pl-11"
              required
              maxLength={30}
              value={telefono}
              onChange={(event) => setTelefono(event.target.value)}
            />
          </div>
        </div>
        <div>
          <label htmlFor="mi-obra" className="label">
            Obra social
          </label>
          <select
            id="mi-obra"
            className="field"
            value={obraSocialId}
            onChange={(event) => {
              setObraSocialId(event.target.value)
              setPlanId('')
            }}
          >
            <option value="">Sin cobertura</option>
            {obrasSociales.map((obra) => (
              <option key={obra.id} value={obra.id}>
                {obra.nombre}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="mi-plan" className="label">
            Plan
          </label>
          <select
            id="mi-plan"
            className="field"
            value={planId}
            onChange={(event) => setPlanId(event.target.value)}
            disabled={planes.length === 0}
          >
            <option value="">Sin plan</option>
            {planes.map((plan) => (
              <option key={plan.id} value={plan.id}>
                {plan.nombre}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="mi-afiliado" className="label">
            N° de afiliado
          </label>
          <input
            id="mi-afiliado"
            className="field"
            maxLength={50}
            value={numeroAfiliado}
            onChange={(event) => setNumeroAfiliado(event.target.value)}
          />
        </div>
        {guardar.isError && (
          <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
            {mensajeError(guardar.error, 'No pudimos guardar los cambios. Intentá de nuevo.')}
          </p>
        )}
        <div className="flex gap-2">
          <button type="button" className="btn-ghost flex-1" onClick={onCerrar}>
            Volver
          </button>
          <button type="submit" disabled={guardar.isPending} className="btn-primary flex-1">
            {guardar.isPending ? 'Guardando…' : 'Guardar cambios'}
          </button>
        </div>
      </form>
    </Modal>
  )
}