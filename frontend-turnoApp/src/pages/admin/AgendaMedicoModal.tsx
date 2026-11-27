import { useState } from 'react'
import type { FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CalendarOff, Plus, Trash2 } from 'lucide-react'
import { adminApi } from '../../lib/endpoints'
import { ApiError } from '../../lib/api'
import type { Medico } from '../../lib/types'
import { formatFecha, horaCorta, hoyISO, nombreDia } from '../../lib/format'
import { Modal } from '../../components/Modal'
import { Spinner } from '../../components/Spinner'

const dias = [1, 2, 3, 4, 5, 6, 7]

export function AgendaMedicoModal({
  medico,
  onCerrar,
}: {
  medico: Medico | null
  onCerrar: () => void
}) {
  const queryClient = useQueryClient()
  const abierto = Boolean(medico)
  const medicoId = medico?.id ?? ''

  const { data: consultorios = [] } = useQuery({
    queryKey: ['admin', 'consultorios'],
    queryFn: adminApi.consultorios,
    enabled: abierto,
  })

  const { data: disponibilidad = [], isLoading: cargandoFranjas } = useQuery({
    queryKey: ['admin', 'disponibilidad', medicoId],
    queryFn: () => adminApi.disponibilidad(medicoId),
    enabled: abierto,
  })

  const { data: excepciones = [], isLoading: cargandoExcepciones } = useQuery({
    queryKey: ['admin', 'excepciones', medicoId],
    queryFn: () => adminApi.excepciones(medicoId),
    enabled: abierto,
  })

  const invalidarAgenda = () => {
    queryClient.invalidateQueries({ queryKey: ['admin', 'disponibilidad', medicoId] })
    queryClient.invalidateQueries({ queryKey: ['admin', 'excepciones', medicoId] })
    queryClient.invalidateQueries({ queryKey: ['medico', 'agenda'] })
  }

  // Franja semanal
  const [consultorioId, setConsultorioId] = useState('')
  const [diaSemana, setDiaSemana] = useState(1)
  const [horaDesde, setHoraDesde] = useState('08:00')
  const [horaHasta, setHoraHasta] = useState('12:00')
  const [vigenteDesde, setVigenteDesde] = useState(hoyISO())
  const [vigenteHasta, setVigenteHasta] = useState('')

  const crearFranja = useMutation({
    mutationFn: () =>
      adminApi.crearDisponibilidad(medicoId, {
        consultorioId,
        diaSemana,
        horaDesde,
        horaHasta,
        vigenteDesde,
        vigenteHasta: vigenteHasta || null,
      }),
    onSuccess: invalidarAgenda,
  })

  const toggleFranja = useMutation({
    mutationFn: ({ id, activo }: { id: string; activo: boolean }) =>
      adminApi.cambiarEstadoDisponibilidad(id, activo),
    onSuccess: invalidarAgenda,
  })

  // Excepción
  const [fechaDesde, setFechaDesde] = useState(hoyISO())
  const [fechaHasta, setFechaHasta] = useState(hoyISO())
  const [todoElDia, setTodoElDia] = useState(true)
  const [excepcionHoraDesde, setExcepcionHoraDesde] = useState('08:00')
  const [excepcionHoraHasta, setExcepcionHoraHasta] = useState('12:00')
  const [motivoExcepcion, setMotivoExcepcion] = useState('')

  const crearExcepcion = useMutation({
    mutationFn: () =>
      adminApi.crearExcepcion(medicoId, {
        fechaDesde,
        fechaHasta,
        todoElDia,
        horaDesde: todoElDia ? null : excepcionHoraDesde,
        horaHasta: todoElDia ? null : excepcionHoraHasta,
        motivo: motivoExcepcion.trim() || null,
      }),
    onSuccess: () => {
      invalidarAgenda()
      setMotivoExcepcion('')
    },
  })

  const eliminarExcepcion = useMutation({
    mutationFn: (id: string) => adminApi.eliminarExcepcion(id),
    onSuccess: invalidarAgenda,
  })

  const error =
    crearFranja.error ?? toggleFranja.error ?? crearExcepcion.error ?? eliminarExcepcion.error

  return (
    <Modal
      abierto={abierto}
      onCerrar={onCerrar}
      titulo={`Agenda de ${medico?.nombre ?? ''} ${medico?.apellido ?? ''}`}
      descripcion="Franjas semanales de atención y excepciones (licencias, congresos, bloqueos)."
      ancho="lg"
    >
      <div className="space-y-8">
        {error instanceof ApiError && (
          <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
            {error.message}
          </p>
        )}

        <section>
          <h3 className="font-display text-xl">Franjas semanales</h3>
          {cargandoFranjas ? (
            <Spinner label="Cargando franjas…" className="py-4" />
          ) : disponibilidad.length === 0 ? (
            <p className="mt-3 rounded-2xl border border-dashed border-ink/15 px-4 py-5 text-center text-sm text-ink/50">
              Sin franjas cargadas. Agregá la primera abajo.
            </p>
          ) : (
            <ul className="mt-3 space-y-2">
              {disponibilidad.map((franja) => (
                <li
                  key={franja.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-paper/70 px-4 py-3"
                >
                  <div>
                    <p className="text-sm font-semibold">
                      {nombreDia(franja.diaSemana)} · {horaCorta(franja.horaDesde)}–
                      {horaCorta(franja.horaHasta)}
                    </p>
                    <p className="text-xs text-ink/50">
                      {franja.consultorio} · desde {formatFecha(franja.vigenteDesde)}
                      {franja.vigenteHasta ? ` hasta ${formatFecha(franja.vigenteHasta)}` : ''}
                    </p>
                  </div>
                  <button
                    type="button"
                    className={`rounded-full border px-3 py-1 text-xs font-bold uppercase tracking-[0.08em] transition ${
                      franja.activo
                        ? 'border-forest/25 bg-forest/10 text-forest hover:bg-forest/20'
                        : 'border-ink/15 bg-ink/5 text-ink/50 hover:bg-ink/10'
                    }`}
                    disabled={toggleFranja.isPending}
                    onClick={() => toggleFranja.mutate({ id: franja.id, activo: !franja.activo })}
                  >
                    {franja.activo ? 'Activa' : 'Inactiva'}
                  </button>
                </li>
              ))}
            </ul>
          )}

          <form
            onSubmit={(event: FormEvent) => {
              event.preventDefault()
              crearFranja.mutate()
            }}
            className="mt-4 rounded-2xl border border-ink/10 bg-white/50 p-4"
          >
            <p className="label">Nueva franja</p>
            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <label htmlFor="franja-consultorio" className="label">
                  Consultorio
                </label>
                <select
                  id="franja-consultorio"
                  className="field"
                  required
                  value={consultorioId}
                  onChange={(event) => setConsultorioId(event.target.value)}
                >
                  <option value="">Elegir…</option>
                  {consultorios
                    .filter((consultorio) => consultorio.activo)
                    .map((consultorio) => (
                      <option key={consultorio.id} value={consultorio.id}>
                        {consultorio.nombre}
                      </option>
                    ))}
                </select>
              </div>
              <div>
                <label htmlFor="franja-dia" className="label">
                  Día
                </label>
                <select
                  id="franja-dia"
                  className="field"
                  value={diaSemana}
                  onChange={(event) => setDiaSemana(Number(event.target.value))}
                >
                  {dias.map((dia) => (
                    <option key={dia} value={dia}>
                      {nombreDia(dia)}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="franja-desde" className="label">
                  Desde
                </label>
                <input
                  id="franja-desde"
                  type="time"
                  className="field"
                  required
                  value={horaDesde}
                  onChange={(event) => setHoraDesde(event.target.value)}
                />
              </div>
              <div>
                <label htmlFor="franja-hasta" className="label">
                  Hasta
                </label>
                <input
                  id="franja-hasta"
                  type="time"
                  className="field"
                  required
                  value={horaHasta}
                  onChange={(event) => setHoraHasta(event.target.value)}
                />
              </div>
              <div>
                <label htmlFor="franja-vigente-desde" className="label">
                  Vigente desde
                </label>
                <input
                  id="franja-vigente-desde"
                  type="date"
                  className="field"
                  required
                  value={vigenteDesde}
                  onChange={(event) => setVigenteDesde(event.target.value)}
                />
              </div>
              <div>
                <label htmlFor="franja-vigente-hasta" className="label">
                  Vigente hasta (opcional)
                </label>
                <input
                  id="franja-vigente-hasta"
                  type="date"
                  className="field"
                  value={vigenteHasta}
                  onChange={(event) => setVigenteHasta(event.target.value)}
                />
              </div>
            </div>
            <button
              type="submit"
              disabled={crearFranja.isPending || !consultorioId}
              className="btn-primary mt-4"
            >
              <Plus className="size-4" />
              {crearFranja.isPending ? 'Agregando…' : 'Agregar franja'}
            </button>
          </form>
        </section>

        <section>
          <h3 className="font-display text-xl">Excepciones</h3>
          {cargandoExcepciones ? (
            <Spinner label="Cargando excepciones…" className="py-4" />
          ) : excepciones.length === 0 ? (
            <p className="mt-3 rounded-2xl border border-dashed border-ink/15 px-4 py-5 text-center text-sm text-ink/50">
              Sin excepciones cargadas.
            </p>
          ) : (
            <ul className="mt-3 space-y-2">
              {excepciones.map((excepcion) => (
                <li
                  key={excepcion.id}
                  className="flex items-center justify-between gap-3 rounded-2xl bg-paper/70 px-4 py-3"
                >
                  <div>
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
                  </div>
                  <button
                    type="button"
                    className="grid size-9 shrink-0 place-items-center rounded-full border border-ink/10 text-clay-deep transition hover:bg-clay/10"
                    disabled={eliminarExcepcion.isPending}
                    onClick={() => eliminarExcepcion.mutate(excepcion.id)}
                    aria-label="Eliminar excepción"
                  >
                    <Trash2 className="size-4" />
                  </button>
                </li>
              ))}
            </ul>
          )}

          <form
            onSubmit={(event: FormEvent) => {
              event.preventDefault()
              crearExcepcion.mutate()
            }}
            className="mt-4 rounded-2xl border border-ink/10 bg-white/50 p-4"
          >
            <p className="label flex items-center gap-2">
              <CalendarOff className="size-4" /> Nueva excepción
            </p>
            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <label htmlFor="excepcion-desde" className="label">
                  Fecha desde
                </label>
                <input
                  id="excepcion-desde"
                  type="date"
                  className="field"
                  required
                  value={fechaDesde}
                  onChange={(event) => {
                    setFechaDesde(event.target.value)
                    if (fechaHasta < event.target.value) setFechaHasta(event.target.value)
                  }}
                />
              </div>
              <div>
                <label htmlFor="excepcion-hasta" className="label">
                  Fecha hasta
                </label>
                <input
                  id="excepcion-hasta"
                  type="date"
                  className="field"
                  required
                  min={fechaDesde}
                  value={fechaHasta}
                  onChange={(event) => setFechaHasta(event.target.value)}
                />
              </div>
            </div>
            <label className="mt-3 flex items-center gap-2 text-sm font-semibold">
              <input
                type="checkbox"
                checked={todoElDia}
                onChange={(event) => setTodoElDia(event.target.checked)}
                className="accent-[#244b3d]"
              />
              Todo el día
            </label>
            {!todoElDia && (
              <div className="mt-3 grid gap-3 sm:grid-cols-2">
                <div>
                  <label htmlFor="excepcion-hora-desde" className="label">
                    Desde
                  </label>
                  <input
                    id="excepcion-hora-desde"
                    type="time"
                    className="field"
                    required
                    value={excepcionHoraDesde}
                    onChange={(event) => setExcepcionHoraDesde(event.target.value)}
                  />
                </div>
                <div>
                  <label htmlFor="excepcion-hora-hasta" className="label">
                    Hasta
                  </label>
                  <input
                    id="excepcion-hora-hasta"
                    type="time"
                    className="field"
                    required
                    value={excepcionHoraHasta}
                    onChange={(event) => setExcepcionHoraHasta(event.target.value)}
                  />
                </div>
              </div>
            )}
            <div className="mt-3">
              <label htmlFor="excepcion-motivo" className="label">
                Motivo (opcional)
              </label>
              <input
                id="excepcion-motivo"
                className="field"
                maxLength={255}
                placeholder="Ej.: licencia, congreso…"
                value={motivoExcepcion}
                onChange={(event) => setMotivoExcepcion(event.target.value)}
              />
            </div>
            <button type="submit" disabled={crearExcepcion.isPending} className="btn-primary mt-4">
              <Plus className="size-4" />
              {crearExcepcion.isPending ? 'Agregando…' : 'Agregar excepción'}
            </button>
          </form>
        </section>
      </div>
    </Modal>
  )
}