import { useState } from 'react'
import type { FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { DoorOpen, HeartPulse, Plus, ShieldCheck } from 'lucide-react'
import { adminApi } from '../../lib/endpoints'
import { ApiError } from '../../lib/api'
import { Spinner } from '../../components/Spinner'

function ErrorInline({ error }: { error: unknown }) {
  if (!error) return null
  return (
    <p className="mt-3 rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
      {error instanceof ApiError ? error.message : 'No pudimos completar la operación.'}
    </p>
  )
}

export function AdminCatalogos() {
  return (
    <div className="grid gap-6 lg:grid-cols-3 lg:items-start">
      <Especialidades />
      <ObrasSociales />
      <Consultorios />
    </div>
  )
}

function Especialidades() {
  const queryClient = useQueryClient()
  const [nombre, setNombre] = useState('')
  const [duracion, setDuracion] = useState(30)

  const { data: especialidades = [], isLoading } = useQuery({
    queryKey: ['admin', 'especialidades'],
    queryFn: adminApi.especialidades,
  })

  const crear = useMutation({
    mutationFn: () => adminApi.crearEspecialidad({ nombre: nombre.trim(), duracionDefaultMin: duracion }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'especialidades'] })
      queryClient.invalidateQueries({ queryKey: ['especialidades'] })
      setNombre('')
    },
  })

  return (
    <section className="card p-6">
      <p className="eyebrow flex items-center gap-2">
        <HeartPulse className="size-4" /> Especialidades
      </p>
      {isLoading ? (
        <Spinner label="Cargando…" className="py-6" />
      ) : (
        <ul className="mt-4 space-y-2">
          {especialidades.map((especialidad) => (
            <li
              key={especialidad.id}
              className="flex items-center justify-between gap-3 rounded-2xl bg-paper/70 px-4 py-3"
            >
              <p className="text-sm font-semibold">{especialidad.nombre}</p>
              <span className="chip">{especialidad.duracionDefaultMin} min</span>
            </li>
          ))}
        </ul>
      )}

      <form
        onSubmit={(event: FormEvent) => {
          event.preventDefault()
          crear.mutate()
        }}
        className="mt-4 space-y-3 border-t border-ink/10 pt-4"
      >
        <div>
          <label htmlFor="especialidad-nombre" className="label">
            Nueva especialidad
          </label>
          <input
            id="especialidad-nombre"
            className="field"
            required
            maxLength={120}
            placeholder="Ej.: Neurología"
            value={nombre}
            onChange={(event) => setNombre(event.target.value)}
          />
        </div>
        <div>
          <label htmlFor="especialidad-duracion" className="label">
            Duración por defecto (min)
          </label>
          <input
            id="especialidad-duracion"
            type="number"
            className="field"
            required
            min={5}
            max={240}
            value={duracion}
            onChange={(event) => setDuracion(Number(event.target.value))}
          />
        </div>
        <ErrorInline error={crear.error} />
        <button type="submit" disabled={crear.isPending} className="btn-primary w-full">
          <Plus className="size-4" /> {crear.isPending ? 'Creando…' : 'Crear especialidad'}
        </button>
      </form>
    </section>
  )
}

function ObrasSociales() {
  const queryClient = useQueryClient()
  const [nombre, setNombre] = useState('')
  const [codigo, setCodigo] = useState('')
  const [planPara, setPlanPara] = useState<string | null>(null)
  const [planNombre, setPlanNombre] = useState('')
  const [planCodigo, setPlanCodigo] = useState('')

  const { data: obrasSociales = [], isLoading } = useQuery({
    queryKey: ['admin', 'obras-sociales'],
    queryFn: adminApi.obrasSociales,
  })

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey: ['admin', 'obras-sociales'] })
    queryClient.invalidateQueries({ queryKey: ['obras-sociales'] })
  }

  const crear = useMutation({
    mutationFn: () => adminApi.crearObraSocial({ nombre: nombre.trim(), codigo: codigo.trim() }),
    onSuccess: () => {
      invalidar()
      setNombre('')
      setCodigo('')
    },
  })

  const crearPlan = useMutation({
    mutationFn: () =>
      adminApi.crearPlan(planPara!, { nombre: planNombre.trim(), codigo: planCodigo.trim() }),
    onSuccess: () => {
      invalidar()
      setPlanPara(null)
      setPlanNombre('')
      setPlanCodigo('')
    },
  })

  return (
    <section className="card p-6">
      <p className="eyebrow flex items-center gap-2">
        <ShieldCheck className="size-4" /> Obras sociales y planes
      </p>
      {isLoading ? (
        <Spinner label="Cargando…" className="py-6" />
      ) : (
        <ul className="mt-4 space-y-3">
          {obrasSociales.map((obra) => (
            <li key={obra.id} className="rounded-2xl bg-paper/70 px-4 py-3">
              <div className="flex items-center justify-between gap-3">
                <p className="text-sm font-semibold">{obra.nombre}</p>
                <span className="chip">{obra.codigo}</span>
              </div>
              {obra.planes.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {obra.planes.map((plan) => (
                    <span key={plan.id} className="chip">
                      {plan.nombre}
                    </span>
                  ))}
                </div>
              )}
              {planPara === obra.id ? (
                <form
                  onSubmit={(event: FormEvent) => {
                    event.preventDefault()
                    crearPlan.mutate()
                  }}
                  className="mt-3 space-y-2"
                >
                  <input
                    className="field py-2"
                    required
                    maxLength={120}
                    placeholder="Nombre del plan"
                    value={planNombre}
                    onChange={(event) => setPlanNombre(event.target.value)}
                    aria-label="Nombre del plan"
                  />
                  <input
                    className="field py-2"
                    required
                    maxLength={40}
                    placeholder="Código (ej.: 210)"
                    value={planCodigo}
                    onChange={(event) => setPlanCodigo(event.target.value)}
                    aria-label="Código del plan"
                  />
                  <ErrorInline error={crearPlan.error} />
                  <div className="flex gap-2">
                    <button
                      type="button"
                      className="btn-ghost flex-1 px-3 py-2 text-xs"
                      onClick={() => setPlanPara(null)}
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={crearPlan.isPending}
                      className="btn-primary flex-1 px-3 py-2 text-xs"
                    >
                      {crearPlan.isPending ? 'Creando…' : 'Crear plan'}
                    </button>
                  </div>
                </form>
              ) : (
                <button
                  type="button"
                  className="mt-2 text-xs font-semibold text-forest underline decoration-forest/30 underline-offset-4 hover:decoration-forest"
                  onClick={() => {
                    setPlanPara(obra.id)
                    setPlanNombre('')
                    setPlanCodigo('')
                    crearPlan.reset()
                  }}
                >
                  + Agregar plan
                </button>
              )}
            </li>
          ))}
        </ul>
      )}

      <form
        onSubmit={(event: FormEvent) => {
          event.preventDefault()
          crear.mutate()
        }}
        className="mt-4 space-y-3 border-t border-ink/10 pt-4"
      >
        <div>
          <label htmlFor="obra-nombre" className="label">
            Nueva obra social
          </label>
          <input
            id="obra-nombre"
            className="field"
            required
            maxLength={120}
            placeholder="Ej.: OSDE"
            value={nombre}
            onChange={(event) => setNombre(event.target.value)}
          />
        </div>
        <div>
          <label htmlFor="obra-codigo" className="label">
            Código
          </label>
          <input
            id="obra-codigo"
            className="field"
            required
            maxLength={40}
            placeholder="Ej.: OSDE"
            value={codigo}
            onChange={(event) => setCodigo(event.target.value)}
          />
        </div>
        <ErrorInline error={crear.error} />
        <button type="submit" disabled={crear.isPending} className="btn-primary w-full">
          <Plus className="size-4" /> {crear.isPending ? 'Creando…' : 'Crear obra social'}
        </button>
      </form>
    </section>
  )
}

function Consultorios() {
  const queryClient = useQueryClient()
  const [nombre, setNombre] = useState('')
  const [ubicacion, setUbicacion] = useState('')

  const { data: consultorios = [], isLoading } = useQuery({
    queryKey: ['admin', 'consultorios'],
    queryFn: adminApi.consultorios,
  })

  const crear = useMutation({
    mutationFn: () =>
      adminApi.crearConsultorio({ nombre: nombre.trim(), ubicacion: ubicacion.trim() || null }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'consultorios'] })
      setNombre('')
      setUbicacion('')
    },
  })

  return (
    <section className="card p-6">
      <p className="eyebrow flex items-center gap-2">
        <DoorOpen className="size-4" /> Consultorios
      </p>
      {isLoading ? (
        <Spinner label="Cargando…" className="py-6" />
      ) : (
        <ul className="mt-4 space-y-2">
          {consultorios.map((consultorio) => (
            <li
              key={consultorio.id}
              className="flex items-center justify-between gap-3 rounded-2xl bg-paper/70 px-4 py-3"
            >
              <div>
                <p className="text-sm font-semibold">{consultorio.nombre}</p>
                {consultorio.ubicacion && (
                  <p className="text-xs text-ink/50">{consultorio.ubicacion}</p>
                )}
              </div>
              <span
                className={`rounded-full border px-2.5 py-1 text-[0.65rem] font-bold uppercase tracking-[0.08em] ${
                  consultorio.activo
                    ? 'border-forest/25 bg-forest/10 text-forest'
                    : 'border-ink/15 bg-ink/5 text-ink/50'
                }`}
              >
                {consultorio.activo ? 'Activo' : 'Inactivo'}
              </span>
            </li>
          ))}
        </ul>
      )}

      <form
        onSubmit={(event: FormEvent) => {
          event.preventDefault()
          crear.mutate()
        }}
        className="mt-4 space-y-3 border-t border-ink/10 pt-4"
      >
        <div>
          <label htmlFor="consultorio-nombre" className="label">
            Nuevo consultorio
          </label>
          <input
            id="consultorio-nombre"
            className="field"
            required
            maxLength={120}
            placeholder="Ej.: Consultorio 4"
            value={nombre}
            onChange={(event) => setNombre(event.target.value)}
          />
        </div>
        <div>
          <label htmlFor="consultorio-ubicacion" className="label">
            Ubicación (opcional)
          </label>
          <input
            id="consultorio-ubicacion"
            className="field"
            maxLength={200}
            placeholder="Ej.: Planta baja, ala este"
            value={ubicacion}
            onChange={(event) => setUbicacion(event.target.value)}
          />
        </div>
        <ErrorInline error={crear.error} />
        <button type="submit" disabled={crear.isPending} className="btn-primary w-full">
          <Plus className="size-4" /> {crear.isPending ? 'Creando…' : 'Crear consultorio'}
        </button>
      </form>
    </section>
  )
}