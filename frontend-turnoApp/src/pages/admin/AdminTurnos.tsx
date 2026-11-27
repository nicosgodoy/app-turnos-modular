import { useState } from 'react'
import type { FormEvent } from 'react'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Ban, CheckCircle2, ChevronLeft, ChevronRight, UserX, XCircle } from 'lucide-react'
import { adminApi } from '../../lib/endpoints'
import { ApiError } from '../../lib/api'
import type { EstadoTurno, Turno } from '../../lib/types'
import { estadoLabels, formatFecha, formatHora } from '../../lib/format'
import { Spinner } from '../../components/Spinner'
import { EmptyState } from '../../components/EmptyState'
import { TurnoCard } from '../../components/TurnoCard'
import { Modal } from '../../components/Modal'

const TAMANIO = 10
const estados = Object.keys(estadoLabels) as EstadoTurno[]

function esGestionable(turno: Turno): boolean {
  return turno.estado === 'PENDIENTE' || turno.estado === 'CONFIRMADO'
}

export function AdminTurnos() {
  const queryClient = useQueryClient()
  const [fecha, setFecha] = useState('')
  const [medicoId, setMedicoId] = useState('')
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(0)
  const [turnoACancelar, setTurnoACancelar] = useState<Turno | null>(null)
  const [motivo, setMotivo] = useState('')

  const { data: medicos = [] } = useQuery({
    queryKey: ['admin', 'medicos'],
    queryFn: adminApi.medicos,
  })

  const filtros = {
    fecha: fecha || undefined,
    medicoId: medicoId || undefined,
    estado: estado || undefined,
  }

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['admin', 'turnos', filtros, pagina],
    queryFn: () => adminApi.turnos({ ...filtros, page: pagina, size: TAMANIO }),
    placeholderData: keepPreviousData,
  })

  const invalidar = () => queryClient.invalidateQueries({ queryKey: ['admin', 'turnos'] })

  const cambiarEstado = useMutation({
    mutationFn: ({ id, nuevoEstado }: { id: string; nuevoEstado: EstadoTurno }) =>
      adminApi.cambiarEstadoTurno(id, nuevoEstado),
    onSuccess: invalidar,
  })

  const cancelar = useMutation({
    mutationFn: () => adminApi.cancelarTurno(turnoACancelar!.id, motivo.trim() || undefined),
    onSuccess: () => {
      invalidar()
      setTurnoACancelar(null)
      setMotivo('')
    },
  })

  const cambiarFiltro = (setter: (valor: string) => void) => (valor: string) => {
    setter(valor)
    setPagina(0)
  }

  const turnos = data?.contenido ?? []
  const totalPaginas = data?.totalPaginas ?? 0
  const error = cambiarEstado.error ?? cancelar.error

  return (
    <div>
      <div className="card grid gap-4 p-5 sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <label htmlFor="filtro-fecha" className="label">
            Fecha
          </label>
          <input
            id="filtro-fecha"
            type="date"
            className="field"
            value={fecha}
            onChange={(event) => cambiarFiltro(setFecha)(event.target.value)}
          />
        </div>
        <div>
          <label htmlFor="filtro-medico" className="label">
            Profesional
          </label>
          <select
            id="filtro-medico"
            className="field"
            value={medicoId}
            onChange={(event) => cambiarFiltro(setMedicoId)(event.target.value)}
          >
            <option value="">Todos</option>
            {medicos.map((medico) => (
              <option key={medico.id} value={medico.id}>
                {medico.nombre} {medico.apellido}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="filtro-estado" className="label">
            Estado
          </label>
          <select
            id="filtro-estado"
            className="field"
            value={estado}
            onChange={(event) => cambiarFiltro(setEstado)(event.target.value)}
          >
            <option value="">Todos</option>
            {estados.map((valor) => (
              <option key={valor} value={valor}>
                {estadoLabels[valor]}
              </option>
            ))}
          </select>
        </div>
        <div className="flex items-end">
          <button
            type="button"
            className="btn-ghost w-full"
            onClick={() => {
              setFecha('')
              setMedicoId('')
              setEstado('')
              setPagina(0)
            }}
          >
            <Ban className="size-4" /> Limpiar filtros
          </button>
        </div>
      </div>

      {error instanceof ApiError && (
        <p className="mt-4 rounded-xl bg-clay/10 px-4 py-3 text-sm font-medium text-clay-deep">
          {error.message}
        </p>
      )}

      <div className="mt-6 space-y-4">
        {isLoading ? (
          <Spinner label="Cargando turnos…" className="py-8" />
        ) : turnos.length === 0 ? (
          <EmptyState
            titulo="No hay turnos con esos filtros"
            descripcion="Probá quitar filtros o elegir otra fecha."
          />
        ) : (
          turnos.map((turno) => (
            <TurnoCard
              key={turno.id}
              turno={turno}
              mostrarPaciente
              actions={
                esGestionable(turno) ? (
                  <>
                    <button
                      type="button"
                      className="btn-ghost px-4 py-2 text-forest"
                      disabled={cambiarEstado.isPending}
                      onClick={() => cambiarEstado.mutate({ id: turno.id, nuevoEstado: 'ATENDIDO' })}
                    >
                      <CheckCircle2 className="size-4" /> Atendido
                    </button>
                    <button
                      type="button"
                      className="btn-ghost px-4 py-2"
                      disabled={cambiarEstado.isPending}
                      onClick={() => cambiarEstado.mutate({ id: turno.id, nuevoEstado: 'AUSENTE' })}
                    >
                      <UserX className="size-4" /> Ausente
                    </button>
                    <button
                      type="button"
                      className="btn-ghost px-4 py-2 text-clay-deep hover:border-clay/50"
                      disabled={cancelar.isPending}
                      onClick={() => {
                        setTurnoACancelar(turno)
                        setMotivo('')
                        cancelar.reset()
                      }}
                    >
                      <XCircle className="size-4" /> Cancelar
                    </button>
                  </>
                ) : undefined
              }
            />
          ))
        )}
      </div>

      {data && data.total > 0 && (
        <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm text-ink/55">
            {data.total} turno{data.total === 1 ? '' : 's'} · página {data.pagina + 1} de{' '}
            {Math.max(totalPaginas, 1)}
            {isFetching && ' · actualizando…'}
          </p>
          <div className="flex gap-2">
            <button
              type="button"
              className="btn-ghost px-4 py-2"
              disabled={pagina === 0}
              onClick={() => setPagina((valor) => valor - 1)}
            >
              <ChevronLeft className="size-4" /> Anterior
            </button>
            <button
              type="button"
              className="btn-ghost px-4 py-2"
              disabled={pagina + 1 >= totalPaginas}
              onClick={() => setPagina((valor) => valor + 1)}
            >
              Siguiente <ChevronRight className="size-4" />
            </button>
          </div>
        </div>
      )}

      <Modal
        abierto={Boolean(turnoACancelar)}
        onCerrar={() => setTurnoACancelar(null)}
        titulo="Cancelar turno de la clínica"
        descripcion={
          turnoACancelar
            ? `${turnoACancelar.paciente} · ${turnoACancelar.medico} · ${formatFecha(turnoACancelar.fechaHora)} · ${formatHora(turnoACancelar.fechaHora)}`
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
            El paciente recibe un email con el motivo de la cancelación y el horario queda libre.
          </p>
          <div>
            <label htmlFor="motivo-clinica" className="label">
              Motivo (opcional)
            </label>
            <textarea
              id="motivo-clinica"
              rows={3}
              className="field resize-none"
              maxLength={500}
              placeholder="Ej.: el profesional no asistirá ese día…"
              value={motivo}
              onChange={(event) => setMotivo(event.target.value)}
            />
          </div>
          {cancelar.isError && (
            <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
              {cancelar.error instanceof ApiError
                ? cancelar.error.message
                : 'No pudimos cancelar el turno.'}
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
    </div>
  )
}