import { useState } from 'react'
import type { FormEvent } from 'react'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight, Pencil, Search, Users } from 'lucide-react'
import { adminApi, catalogoApi } from '../../lib/endpoints'
import { ApiError } from '../../lib/api'
import type { Paciente } from '../../lib/types'
import { Spinner } from '../../components/Spinner'
import { EmptyState } from '../../components/EmptyState'
import { Modal } from '../../components/Modal'

const TAMANIO = 10

export function AdminPacientes() {
  const [busqueda, setBusqueda] = useState('')
  const [q, setQ] = useState('')
  const [pagina, setPagina] = useState(0)
  const [pacienteAEditar, setPacienteAEditar] = useState<Paciente | null>(null)

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['admin', 'pacientes', q, pagina],
    queryFn: () => adminApi.pacientes({ q: q || undefined, page: pagina, size: TAMANIO }),
    placeholderData: keepPreviousData,
  })

  const buscar = (event: FormEvent) => {
    event.preventDefault()
    setQ(busqueda.trim())
    setPagina(0)
  }

  const pacientes = data?.contenido ?? []
  const totalPaginas = data?.totalPaginas ?? 0

  return (
    <div>
      <form onSubmit={buscar} className="card flex flex-col gap-3 p-5 sm:flex-row">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-ink/35" />
          <input
            className="field pl-11"
            placeholder="Buscar por nombre, apellido, DNI o email…"
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            aria-label="Buscar pacientes"
          />
        </div>
        <button type="submit" className="btn-primary">
          <Search className="size-4" /> Buscar
        </button>
        {q && (
          <button
            type="button"
            className="btn-ghost"
            onClick={() => {
              setBusqueda('')
              setQ('')
              setPagina(0)
            }}
          >
            Limpiar
          </button>
        )}
      </form>

      <div className="mt-6 space-y-3">
        {isLoading ? (
          <Spinner label="Cargando pacientes…" className="py-8" />
        ) : pacientes.length === 0 ? (
          <EmptyState
            titulo="No encontramos pacientes"
            descripcion={
              q
                ? `Sin resultados para “${q}”. Probá con otro término.`
                : 'Todavía no hay pacientes registrados. Se crean al ingresar por primera vez con OTP.'
            }
            icono={<Users className="size-6" />}
          />
        ) : (
          pacientes.map((paciente) => (
            <article
              key={paciente.id}
              className="card flex flex-col gap-3 p-5 sm:flex-row sm:items-center sm:justify-between"
            >
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <h3 className="font-display text-lg leading-tight">
                    {paciente.apellido}, {paciente.nombre}
                  </h3>
                  <span className="chip">DNI {paciente.dni}</span>
                  {paciente.consentimientoDatos ? (
                    <span className="chip border-forest/20 bg-forest/5 text-forest">
                      Consentimiento OK
                    </span>
                  ) : (
                    <span className="chip border-clay/25 bg-clay/10 text-clay-deep">
                      Sin consentimiento
                    </span>
                  )}
                </div>
                <p className="mt-1 break-all text-sm text-ink/55">
                  {paciente.email} · {paciente.telefono || 'sin teléfono'}
                </p>
                <p className="mt-1 text-sm text-ink/55">
                  {paciente.obraSocial
                    ? `${paciente.obraSocial.nombre}${paciente.plan ? ` · ${paciente.plan.nombre}` : ''}`
                    : 'Sin cobertura (particular)'}
                  {paciente.numeroAfiliado ? ` · Afiliado ${paciente.numeroAfiliado}` : ''}
                </p>
              </div>
              <button
                type="button"
                className="btn-ghost shrink-0 px-4 py-2"
                onClick={() => setPacienteAEditar(paciente)}
              >
                <Pencil className="size-4" /> Editar
              </button>
            </article>
          ))
        )}
      </div>

      {data && data.total > 0 && (
        <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm text-ink/55">
            {data.total} paciente{data.total === 1 ? '' : 's'} · página {data.pagina + 1} de{' '}
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

      {pacienteAEditar && (
        <PacienteFormModal
          key={pacienteAEditar.id}
          paciente={pacienteAEditar}
          onCerrar={() => setPacienteAEditar(null)}
        />
      )}
    </div>
  )
}

function PacienteFormModal({
  paciente,
  onCerrar,
}: {
  paciente: Paciente
  onCerrar: () => void
}) {
  const queryClient = useQueryClient()

  const [nombre, setNombre] = useState(paciente.nombre)
  const [apellido, setApellido] = useState(paciente.apellido)
  const [fechaNacimiento, setFechaNacimiento] = useState(paciente.fechaNacimiento ?? '')
  const [telefono, setTelefono] = useState(paciente.telefono ?? '')
  const [email, setEmail] = useState(paciente.email)
  const [obraSocialId, setObraSocialId] = useState(paciente.obraSocial?.id ?? '')
  const [planId, setPlanId] = useState(paciente.plan?.id ?? '')
  const [numeroAfiliado, setNumeroAfiliado] = useState(paciente.numeroAfiliado ?? '')
  const [consentimiento, setConsentimiento] = useState(paciente.consentimientoDatos)

  const { data: obrasSociales = [] } = useQuery({
    queryKey: ['obras-sociales'],
    queryFn: catalogoApi.obrasSociales,
  })

  const planes = obrasSociales.find((obra) => obra.id === obraSocialId)?.planes ?? []

  const guardar = useMutation({
    mutationFn: () =>
      adminApi.actualizarPaciente(paciente.id, {
        nombre: nombre.trim(),
        apellido: apellido.trim(),
        fechaNacimiento: fechaNacimiento || null,
        telefono: telefono.trim(),
        email: email.trim(),
        obraSocialId: obraSocialId || null,
        planId: planId || null,
        numeroAfiliado: numeroAfiliado.trim() || null,
        consentimientoDatos: consentimiento,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'pacientes'] })
      onCerrar()
    },
  })

  return (
    <Modal
      abierto
      onCerrar={onCerrar}
      titulo={`Editar a ${paciente.nombre} ${paciente.apellido}`}
      descripcion={`DNI ${paciente.dni}`}
      ancho="lg"
    >
      <form
        onSubmit={(event: FormEvent) => {
          event.preventDefault()
          guardar.mutate()
        }}
        className="space-y-4"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="paciente-nombre" className="label">
              Nombre
            </label>
            <input
              id="paciente-nombre"
              className="field"
              required
              maxLength={120}
              value={nombre}
              onChange={(event) => setNombre(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="paciente-apellido" className="label">
              Apellido
            </label>
            <input
              id="paciente-apellido"
              className="field"
              required
              maxLength={120}
              value={apellido}
              onChange={(event) => setApellido(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="paciente-nacimiento" className="label">
              Fecha de nacimiento
            </label>
            <input
              id="paciente-nacimiento"
              type="date"
              className="field"
              value={fechaNacimiento}
              onChange={(event) => setFechaNacimiento(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="paciente-telefono" className="label">
              Teléfono
            </label>
            <input
              id="paciente-telefono"
              className="field"
              required
              maxLength={30}
              value={telefono}
              onChange={(event) => setTelefono(event.target.value)}
            />
          </div>
          <div className="sm:col-span-2">
            <label htmlFor="paciente-email" className="label">
              Email
            </label>
            <input
              id="paciente-email"
              type="email"
              className="field"
              required
              maxLength={255}
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>
          <div>
            <label htmlFor="paciente-obra" className="label">
              Obra social
            </label>
            <select
              id="paciente-obra"
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
            <label htmlFor="paciente-plan" className="label">
              Plan
            </label>
            <select
              id="paciente-plan"
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
            <label htmlFor="paciente-afiliado" className="label">
              N° de afiliado
            </label>
            <input
              id="paciente-afiliado"
              className="field"
              maxLength={50}
              value={numeroAfiliado}
              onChange={(event) => setNumeroAfiliado(event.target.value)}
            />
          </div>
          <label className="flex items-center gap-2 self-end pb-3 text-sm font-semibold">
            <input
              type="checkbox"
              checked={consentimiento}
              onChange={(event) => setConsentimiento(event.target.checked)}
              className="accent-[#244b3d]"
            />
            Consentimiento de datos (Ley 25.326)
          </label>
        </div>

        {guardar.isError && (
          <p className="rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
            {guardar.error instanceof ApiError
              ? guardar.error.message
              : 'No pudimos guardar los cambios.'}
          </p>
        )}

        <div className="flex gap-2">
          <button type="button" className="btn-ghost flex-1" onClick={onCerrar}>
            Cancelar
          </button>
          <button type="submit" disabled={guardar.isPending} className="btn-primary flex-1">
            {guardar.isPending ? 'Guardando…' : 'Guardar cambios'}
          </button>
        </div>
      </form>
    </Modal>
  )
}