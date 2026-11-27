import { useState } from 'react'
import type { FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BadgeCheck, CalendarClock, Pencil, Plus, ShieldCheck, Stethoscope, Trash2 } from 'lucide-react'
import { adminApi } from '../../lib/endpoints'
import type { MedicoPayload } from '../../lib/endpoints'
import { ApiError } from '../../lib/api'
import type { Medico } from '../../lib/types'
import { formatPrecio, iniciales } from '../../lib/format'
import { Spinner } from '../../components/Spinner'
import { EmptyState } from '../../components/EmptyState'
import { Modal } from '../../components/Modal'
import { AgendaMedicoModal } from './AgendaMedicoModal'

export function AdminMedicos() {
  const queryClient = useQueryClient()
  const [formularioAbierto, setFormularioAbierto] = useState(false)
  const [medicoAEditar, setMedicoAEditar] = useState<Medico | null>(null)
  const [medicoAgenda, setMedicoAgenda] = useState<Medico | null>(null)

  const { data: medicos = [], isLoading } = useQuery({
    queryKey: ['admin', 'medicos'],
    queryFn: adminApi.medicos,
  })

  const cambiarEstado = useMutation({
    mutationFn: ({ id, activo }: { id: string; activo: boolean }) =>
      adminApi.cambiarEstadoMedico(id, activo),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'medicos'] })
      queryClient.invalidateQueries({ queryKey: ['medicos'] })
    },
  })

  const abrirNuevo = () => {
    setMedicoAEditar(null)
    setFormularioAbierto(true)
  }

  const abrirEdicion = (medico: Medico) => {
    setMedicoAEditar(medico)
    setFormularioAbierto(true)
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-ink/55">
          {medicos.length} profesional{medicos.length === 1 ? '' : 'es'} registrado
          {medicos.length === 1 ? '' : 's'}
        </p>
        <button type="button" className="btn-primary" onClick={abrirNuevo}>
          <Plus className="size-4" /> Nuevo médico
        </button>
      </div>

      {cambiarEstado.error instanceof ApiError && (
        <p className="mt-4 rounded-xl bg-clay/10 px-4 py-3 text-sm font-medium text-clay-deep">
          {cambiarEstado.error.message}
        </p>
      )}

      <div className="mt-6">
        {isLoading ? (
          <Spinner label="Cargando profesionales…" className="py-8" />
        ) : medicos.length === 0 ? (
          <EmptyState
            titulo="Todavía no hay médicos"
            descripcion="Creá el primer profesional para empezar a ofrecer turnos."
            icono={<Stethoscope className="size-6" />}
            accion={
              <button type="button" className="btn-primary" onClick={abrirNuevo}>
                Crear médico
              </button>
            }
          />
        ) : (
          <div className="grid gap-5 md:grid-cols-2">
            {medicos.map((medico) => (
              <article key={medico.id} className="card flex flex-col gap-4 p-6">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-4">
                    <span className="grid size-12 place-items-center rounded-2xl bg-sage-light font-display text-lg font-semibold text-forest">
                      {iniciales(medico.nombre, medico.apellido)}
                    </span>
                    <div>
                      <h3 className="flex items-center gap-1.5 font-display text-xl leading-tight">
                        {medico.nombre} {medico.apellido}
                        <BadgeCheck className="size-4 text-forest" />
                      </h3>
                      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-ink/45">
                        MP {medico.matricula} · {medico.duracionTurnoMin} min
                      </p>
                    </div>
                  </div>
                  <span
                    className={`rounded-full border px-2.5 py-1 text-[0.65rem] font-bold uppercase tracking-[0.08em] ${
                      medico.activo
                        ? 'border-forest/25 bg-forest/10 text-forest'
                        : 'border-ink/15 bg-ink/5 text-ink/50'
                    }`}
                  >
                    {medico.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </div>

                <p className="break-all text-sm text-ink/55">{medico.email}</p>

                <div className="flex flex-wrap gap-1.5">
                  {medico.especialidades.map((especialidad) => (
                    <span key={especialidad.id} className="chip">
                      {especialidad.nombre}
                    </span>
                  ))}
                </div>

                {medico.coberturas.length > 0 && (
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
                )}

                <div className="mt-auto flex flex-wrap items-center justify-between gap-3 border-t border-ink/10 pt-4">
                  <p className="text-sm font-semibold">
                    Particular: {formatPrecio(medico.precioParticular)}
                  </p>
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      className="btn-ghost px-4 py-2"
                      onClick={() => setMedicoAgenda(medico)}
                    >
                      <CalendarClock className="size-4" /> Agenda
                    </button>
                    <button
                      type="button"
                      className="btn-ghost px-4 py-2"
                      onClick={() => abrirEdicion(medico)}
                    >
                      <Pencil className="size-4" /> Editar
                    </button>
                    <button
                      type="button"
                      className={`btn-ghost px-4 py-2 ${
                        medico.activo ? 'text-clay-deep hover:border-clay/50' : 'text-forest'
                      }`}
                      disabled={cambiarEstado.isPending}
                      onClick={() => cambiarEstado.mutate({ id: medico.id, activo: !medico.activo })}
                    >
                      {medico.activo ? 'Desactivar' : 'Activar'}
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>

      {formularioAbierto && (
        <MedicoFormModal
          key={medicoAEditar?.id ?? 'nuevo'}
          medico={medicoAEditar}
          onCerrar={() => setFormularioAbierto(false)}
        />
      )}

      {medicoAgenda && (
        <AgendaMedicoModal
          key={medicoAgenda.id}
          medico={medicoAgenda}
          onCerrar={() => setMedicoAgenda(null)}
        />
      )}
    </div>
  )
}

function MedicoFormModal({ medico, onCerrar }: { medico: Medico | null; onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const editando = Boolean(medico)

  const [nombre, setNombre] = useState(medico?.nombre ?? '')
  const [apellido, setApellido] = useState(medico?.apellido ?? '')
  const [email, setEmail] = useState(medico?.email ?? '')
  const [password, setPassword] = useState('')
  const [matricula, setMatricula] = useState(medico?.matricula ?? '')
  const [duracionTurnoMin, setDuracionTurnoMin] = useState(medico?.duracionTurnoMin ?? 30)
  const [precioParticular, setPrecioParticular] = useState(
    medico?.precioParticular !== null && medico?.precioParticular !== undefined
      ? String(medico.precioParticular)
      : '',
  )
  const [biografia, setBiografia] = useState(medico?.biografia ?? '')
  const [especialidadIds, setEspecialidadIds] = useState<string[]>(
    medico?.especialidades.map((especialidad) => especialidad.id) ?? [],
  )
  const [coberturas, setCoberturas] = useState<{ obraSocialId: string; planId: string | null }[]>(
    medico?.coberturas.map((cobertura) => ({
      obraSocialId: cobertura.obraSocialId,
      planId: cobertura.planId,
    })) ?? [],
  )
  const [activo, setActivo] = useState(medico?.activo ?? true)

  const { data: especialidades = [] } = useQuery({
    queryKey: ['admin', 'especialidades'],
    queryFn: adminApi.especialidades,
  })

  const { data: obrasSociales = [] } = useQuery({
    queryKey: ['admin', 'obras-sociales'],
    queryFn: adminApi.obrasSociales,
  })

  const guardar = useMutation({
    mutationFn: (payload: MedicoPayload) =>
      editando ? adminApi.actualizarMedico(medico!.id, payload) : adminApi.crearMedico(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'medicos'] })
      queryClient.invalidateQueries({ queryKey: ['medicos'] })
      onCerrar()
    },
  })

  const alternarEspecialidad = (id: string) => {
    setEspecialidadIds((actual) =>
      actual.includes(id) ? actual.filter((valor) => valor !== id) : [...actual, id],
    )
  }

  const enviar = (event: FormEvent) => {
    event.preventDefault()
    const payload: MedicoPayload = {
      nombre: nombre.trim(),
      apellido: apellido.trim(),
      email: email.trim(),
      matricula: matricula.trim(),
      duracionTurnoMin,
      precioParticular: precioParticular === '' ? null : Number(precioParticular),
      biografia: biografia.trim() || null,
      especialidadIds,
      coberturas: coberturas.filter((cobertura) => cobertura.obraSocialId),
      activo,
    }
    if (password) payload.password = password
    guardar.mutate(payload)
  }

  return (
    <Modal
      abierto
      onCerrar={onCerrar}
      titulo={editando ? `Editar a ${medico?.nombre} ${medico?.apellido}` : 'Nuevo médico'}
      descripcion="Los médicos ingresan al panel con su email y contraseña."
      ancho="lg"
    >
      <form onSubmit={enviar} className="space-y-5">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="medico-nombre" className="label">
              Nombre
            </label>
            <input
              id="medico-nombre"
              className="field"
              required
              maxLength={120}
              value={nombre}
              onChange={(event) => setNombre(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="medico-apellido" className="label">
              Apellido
            </label>
            <input
              id="medico-apellido"
              className="field"
              required
              maxLength={120}
              value={apellido}
              onChange={(event) => setApellido(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="medico-email" className="label">
              Email
            </label>
            <input
              id="medico-email"
              type="email"
              className="field"
              required
              maxLength={255}
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="medico-password" className="label">
              Contraseña {editando && '(dejar vacía para mantener)'}
            </label>
            <input
              id="medico-password"
              type="password"
              className="field"
              required={!editando}
              minLength={8}
              maxLength={100}
              autoComplete="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="medico-matricula" className="label">
              Matrícula
            </label>
            <input
              id="medico-matricula"
              className="field"
              required
              maxLength={40}
              value={matricula}
              onChange={(event) => setMatricula(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="medico-duracion" className="label">
              Duración del turno (min)
            </label>
            <input
              id="medico-duracion"
              type="number"
              className="field"
              required
              min={5}
              max={240}
              value={duracionTurnoMin}
              onChange={(event) => setDuracionTurnoMin(Number(event.target.value))}
            />
          </div>
          <div>
            <label htmlFor="medico-precio" className="label">
              Precio particular (ARS)
            </label>
            <input
              id="medico-precio"
              type="number"
              className="field"
              min={0}
              step="1"
              placeholder="Ej.: 25000"
              value={precioParticular}
              onChange={(event) => setPrecioParticular(event.target.value)}
            />
          </div>
          {editando && (
            <label className="flex items-center gap-2 self-end pb-3 text-sm font-semibold">
              <input
                type="checkbox"
                checked={activo}
                onChange={(event) => setActivo(event.target.checked)}
                className="accent-[#244b3d]"
              />
              Médico activo
            </label>
          )}
        </div>

        <div>
          <label htmlFor="medico-biografia" className="label">
            Biografía
          </label>
          <textarea
            id="medico-biografia"
            rows={3}
            className="field resize-none"
            maxLength={2000}
            value={biografia}
            onChange={(event) => setBiografia(event.target.value)}
          />
        </div>

        <div>
          <p className="label">Especialidades (al menos una)</p>
          <div className="flex flex-wrap gap-2">
            {especialidades.map((especialidad) => {
              const marcada = especialidadIds.includes(especialidad.id)
              return (
                <button
                  key={especialidad.id}
                  type="button"
                  onClick={() => alternarEspecialidad(especialidad.id)}
                  className={`rounded-full border px-4 py-2 text-sm font-semibold transition ${
                    marcada
                      ? 'border-forest bg-forest text-paper'
                      : 'border-ink/15 bg-white/60 text-ink/65 hover:border-forest/40'
                  }`}
                >
                  {especialidad.nombre}
                </button>
              )
            })}
          </div>
        </div>

        <div>
          <div className="flex items-center justify-between gap-3">
            <p className="label mb-0">Coberturas que acepta</p>
            <button
              type="button"
              className="btn-ghost px-3 py-1.5 text-xs"
              onClick={() => setCoberturas((actual) => [...actual, { obraSocialId: '', planId: null }])}
            >
              <Plus className="size-3.5" /> Agregar
            </button>
          </div>
          {coberturas.length === 0 ? (
            <p className="mt-2 rounded-2xl border border-dashed border-ink/15 px-4 py-4 text-center text-sm text-ink/50">
              Sin coberturas: todos los turnos serán particulares.
            </p>
          ) : (
            <div className="mt-2 space-y-2">
              {coberturas.map((cobertura, indice) => {
                const planes =
                  obrasSociales.find((obra) => obra.id === cobertura.obraSocialId)?.planes ?? []
                return (
                  <div key={indice} className="flex flex-wrap items-center gap-2">
                    <select
                      className="field flex-1"
                      required
                      value={cobertura.obraSocialId}
                      onChange={(event) =>
                        setCoberturas((actual) =>
                          actual.map((valor, i) =>
                            i === indice
                              ? { obraSocialId: event.target.value, planId: null }
                              : valor,
                          ),
                        )
                      }
                      aria-label="Obra social"
                    >
                      <option value="">Elegir obra social…</option>
                      {obrasSociales.map((obra) => (
                        <option key={obra.id} value={obra.id}>
                          {obra.nombre}
                        </option>
                      ))}
                    </select>
                    <select
                      className="field flex-1"
                      value={cobertura.planId ?? ''}
                      onChange={(event) =>
                        setCoberturas((actual) =>
                          actual.map((valor, i) =>
                            i === indice
                              ? { ...valor, planId: event.target.value || null }
                              : valor,
                          ),
                        )
                      }
                      aria-label="Plan"
                    >
                      <option value="">Todos los planes</option>
                      {planes.map((plan) => (
                        <option key={plan.id} value={plan.id}>
                          {plan.nombre}
                        </option>
                      ))}
                    </select>
                    <button
                      type="button"
                      className="grid size-10 shrink-0 place-items-center rounded-full border border-ink/10 text-clay-deep transition hover:bg-clay/10"
                      onClick={() =>
                        setCoberturas((actual) => actual.filter((_, i) => i !== indice))
                      }
                      aria-label="Quitar cobertura"
                    >
                      <Trash2 className="size-4" />
                    </button>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {guardar.isError && (
          <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
            {guardar.error instanceof ApiError
              ? guardar.error.message
              : 'No pudimos guardar el profesional.'}
          </p>
        )}
        {especialidadIds.length === 0 && (
          <p className="text-xs font-semibold text-clay-deep">
            Elegí al menos una especialidad para poder guardar.
          </p>
        )}

        <div className="flex gap-2">
          <button type="button" className="btn-ghost flex-1" onClick={onCerrar}>
            Cancelar
          </button>
          <button
            type="submit"
            disabled={guardar.isPending || especialidadIds.length === 0}
            className="btn-primary flex-1"
          >
            {guardar.isPending ? 'Guardando…' : editando ? 'Guardar cambios' : 'Crear médico'}
          </button>
        </div>
      </form>
    </Modal>
  )
}