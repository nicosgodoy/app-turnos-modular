import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  Activity,
  ArrowRight,
  Baby,
  BellRing,
  Bone,
  CalendarCheck,
  Clock3,
  Flower2,
  HeartPulse,
  Search,
  ShieldCheck,
  Sparkles,
  Stethoscope,
} from 'lucide-react'
import { catalogoApi } from '../lib/endpoints'
import { formatPrecio, iniciales } from '../lib/format'
import type { MedicoResumen } from '../lib/types'
import { Spinner } from '../components/Spinner'
import { EmptyState } from '../components/EmptyState'

const iconosEspecialidad: Record<string, typeof HeartPulse> = {
  'Clínica Médica': Stethoscope,
  Pediatría: Baby,
  Cardiología: HeartPulse,
  Dermatología: Sparkles,
  Ginecología: Flower2,
  Traumatología: Bone,
}

function irAResultados() {
  document.getElementById('resultados')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function MedicoCard({ medico }: { medico: MedicoResumen }) {
  return (
    <Link
      to={`/medicos/${medico.id}`}
      className="card group flex flex-col gap-5 p-6 transition duration-300 hover:-translate-y-1 hover:border-forest/30 hover:shadow-[0_30px_70px_-45px_rgba(27,42,37,0.75)]"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-4">
          <span className="grid size-14 place-items-center rounded-2xl bg-sage-light font-display text-lg font-semibold text-forest">
            {iniciales(medico.nombre, medico.apellido)}
          </span>
          <div>
            <h3 className="font-display text-xl leading-tight">
              {medico.nombre} {medico.apellido}
            </h3>
            <p className="mt-0.5 text-xs font-semibold uppercase tracking-[0.12em] text-ink/45">
              MP {medico.matricula}
            </p>
          </div>
        </div>
        <ArrowRight className="mt-1 size-5 shrink-0 text-ink/25 transition group-hover:translate-x-1 group-hover:text-clay" />
      </div>

      <div className="flex flex-wrap gap-1.5">
        {medico.especialidades.map((especialidad) => (
          <span key={especialidad.id} className="chip">
            {especialidad.nombre}
          </span>
        ))}
      </div>

      {medico.biografia && (
        <p className="line-clamp-2 text-sm leading-relaxed text-ink/55">{medico.biografia}</p>
      )}

      <div className="mt-auto flex items-end justify-between border-t border-ink/10 pt-4">
        <div>
          <p className="text-[0.65rem] font-bold uppercase tracking-[0.16em] text-ink/40">
            Turno particular
          </p>
          <p className="font-display text-lg text-ink">{formatPrecio(medico.precioParticular)}</p>
        </div>
        <div className="text-right">
          <p className="text-[0.65rem] font-bold uppercase tracking-[0.16em] text-ink/40">Duración</p>
          <p className="text-sm font-semibold text-ink/70">{medico.duracionTurnoMin} min</p>
        </div>
      </div>
    </Link>
  )
}

export function InicioPage() {
  const [especialidadId, setEspecialidadId] = useState('')
  const [obraSocialId, setObraSocialId] = useState('')
  const [planId, setPlanId] = useState('')

  const { data: especialidades = [] } = useQuery({
    queryKey: ['especialidades'],
    queryFn: catalogoApi.especialidades,
  })

  const { data: obrasSociales = [] } = useQuery({
    queryKey: ['obras-sociales'],
    queryFn: catalogoApi.obrasSociales,
  })

  const filtros = useMemo(
    () => ({
      especialidadId: especialidadId || undefined,
      obraSocialId: obraSocialId || undefined,
      planId: planId || undefined,
    }),
    [especialidadId, obraSocialId, planId],
  )

  const {
    data: medicos = [],
    isFetching,
    isError,
  } = useQuery({
    queryKey: ['medicos', filtros],
    queryFn: () => catalogoApi.medicos(filtros),
  })

  const planes = obrasSociales.find((obra) => obra.id === obraSocialId)?.planes ?? []

  const aplicarFiltros = (event: FormEvent) => {
    event.preventDefault()
    irAResultados()
  }

  const limpiar = () => {
    setEspecialidadId('')
    setObraSocialId('')
    setPlanId('')
  }

  const elegirEspecialidad = (id: string) => {
    setEspecialidadId(id)
    setTimeout(irAResultados, 50)
  }

  const elegirObraSocial = (id: string) => {
    setObraSocialId(id)
    setPlanId('')
    setTimeout(irAResultados, 50)
  }

  const hayFiltros = Boolean(especialidadId || obraSocialId || planId)

  return (
    <div>
      <section className="relative overflow-hidden">
        <div className="pointer-events-none absolute -right-40 -top-48 size-[34rem] rounded-full bg-sage/30 blur-3xl" />
        <div className="pointer-events-none absolute -left-24 top-40 size-72 rounded-full bg-gold/15 blur-3xl" />

        <div className="container-page relative grid items-center gap-14 py-14 lg:grid-cols-[1.05fr_0.95fr] lg:py-24">
          <div className="animate-rise">
            <p className="eyebrow flex items-center gap-2">
              <span className="inline-block h-px w-8 bg-clay" />
              Clínica Sagrada Familia · Gral. Paz y Libertad 3
            </p>
            <h1 className="mt-5 text-balance text-5xl leading-[0.92] tracking-[-0.03em] sm:text-6xl lg:text-7xl">
              Tu salud,
              <span className="block italic text-clay">en agenda.</span>
            </h1>
            <p className="mt-6 max-w-xl text-base leading-relaxed text-ink/65 sm:text-lg">
              Elegí especialidad y obra social, mirá los horarios reales de cada profesional y reservá
              en menos de dos minutos. Sin llamados, sin vueltas.
            </p>

            <div className="mt-7 flex flex-wrap gap-2">
              <span className="chip">
                <ShieldCheck className="size-3.5 text-forest" /> Datos protegidos
              </span>
              <span className="chip">
                <BellRing className="size-3.5 text-clay" /> Recordatorio por email
              </span>
              <span className="chip">
                <Clock3 className="size-3.5 text-forest" /> Cancelá hasta 24 h antes
              </span>
            </div>

            <form onSubmit={aplicarFiltros} className="card mt-9 grid gap-4 p-5 sm:p-6">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label htmlFor="especialidad" className="label">
                    Especialidad
                  </label>
                  <select
                    id="especialidad"
                    className="field"
                    value={especialidadId}
                    onChange={(event) => setEspecialidadId(event.target.value)}
                  >
                    <option value="">Todas las especialidades</option>
                    {especialidades.map((especialidad) => (
                      <option key={especialidad.id} value={especialidad.id}>
                        {especialidad.nombre}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label htmlFor="obra-social" className="label">
                    Obra social
                  </label>
                  <select
                    id="obra-social"
                    className="field"
                    value={obraSocialId}
                    onChange={(event) => {
                      setObraSocialId(event.target.value)
                      setPlanId('')
                    }}
                  >
                    <option value="">Todas las coberturas</option>
                    {obrasSociales.map((obra) => (
                      <option key={obra.id} value={obra.id}>
                        {obra.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {planes.length > 0 && (
                <div>
                  <label htmlFor="plan" className="label">
                    Plan
                  </label>
                  <select
                    id="plan"
                    className="field"
                    value={planId}
                    onChange={(event) => setPlanId(event.target.value)}
                  >
                    <option value="">Cualquier plan</option>
                    {planes.map((plan) => (
                      <option key={plan.id} value={plan.id}>
                        {plan.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div className="flex flex-col gap-2 sm:flex-row">
                <button type="submit" className="btn-primary flex-1">
                  <Search className="size-4" />
                  Buscar turnos
                </button>
                {hayFiltros && (
                  <button type="button" onClick={limpiar} className="btn-ghost">
                    Limpiar
                  </button>
                )}
              </div>
            </form>
          </div>

          <div className="relative mx-auto hidden w-full max-w-md lg:block">
            <div className="animate-rise-slow relative rotate-[-3deg] rounded-[2.2rem] border border-ink/10 bg-white/85 p-6 shadow-[0_40px_100px_-50px_rgba(27,42,37,0.8)] backdrop-blur">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-[0.65rem] font-bold uppercase tracking-[0.2em] text-ink/40">
                    Agenda de la semana
                  </p>
                  <p className="font-display text-2xl">Próximos turnos</p>
                </div>
                <span className="grid size-11 place-items-center rounded-2xl bg-clay/10 text-clay">
                  <CalendarCheck className="size-5" />
                </span>
              </div>

              <div className="mt-5 space-y-3">
                {(medicos.length > 0 ? medicos.slice(0, 3) : []).map((medico, indice) => (
                  <div
                    key={medico.id}
                    className="flex items-center gap-3 rounded-2xl border border-ink/8 bg-paper/70 px-3.5 py-3"
                  >
                    <span className="grid size-10 place-items-center rounded-xl bg-forest text-xs font-bold text-paper">
                      {iniciales(medico.nombre, medico.apellido)}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold">
                        {medico.nombre} {medico.apellido}
                      </p>
                      <p className="truncate text-xs text-ink/50">
                        {medico.especialidades[0]?.nombre ?? 'Consulta'}
                      </p>
                    </div>
                    <span className="rounded-full bg-white px-2.5 py-1 text-xs font-bold text-forest">
                      {['09:00', '09:30', '10:15'][indice]}
                    </span>
                  </div>
                ))}
                {medicos.length === 0 && (
                  <div className="rounded-2xl border border-dashed border-ink/15 px-4 py-8 text-center text-sm text-ink/45">
                    Cargando agenda…
                  </div>
                )}
              </div>

              <div className="mt-5 flex items-center justify-between rounded-2xl bg-forest px-4 py-3 text-paper">
                <div>
                  <p className="text-[0.62rem] font-bold uppercase tracking-[0.18em] text-paper/55">
                    Reserva online
                  </p>
                  <p className="text-sm font-semibold">Confirmación inmediata</p>
                </div>
                <span className="rounded-full bg-gold px-3 py-1 text-[0.65rem] font-bold uppercase tracking-[0.12em] text-ink">
                  Activa
                </span>
              </div>
            </div>

            <div className="animate-rise absolute -bottom-8 -left-6 w-56 rotate-[4deg] rounded-3xl border border-ink/10 bg-paper-deep p-4 shadow-xl">
              <div className="flex items-center gap-2 text-clay">
                <BellRing className="size-4" />
                <p className="text-[0.65rem] font-bold uppercase tracking-[0.14em]">Recordatorio</p>
              </div>
              <p className="mt-2 text-sm leading-snug text-ink/70">
                Te avisamos <strong>24 h antes</strong> de tu turno por email.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="border-y border-ink/10 bg-white/45">
        <div className="container-page grid grid-cols-2 gap-6 py-8 sm:grid-cols-4">
          {[
            ['6', 'especialidades'],
            ['90', 'días para reservar'],
            ['24 h', 'para cancelar'],
            ['100%', 'online'],
          ].map(([valor, etiqueta]) => (
            <div key={etiqueta}>
              <p className="font-display text-3xl text-forest">{valor}</p>
              <p className="text-xs font-semibold uppercase tracking-[0.14em] text-ink/45">{etiqueta}</p>
            </div>
          ))}
        </div>
      </section>

      <section id="especialidades" className="container-page scroll-mt-28 py-20">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="eyebrow">Especialidades</p>
            <h2 className="mt-2 text-4xl tracking-tight sm:text-5xl">Encontrá a tu profesional</h2>
          </div>
          <p className="max-w-sm text-sm leading-relaxed text-ink/55">
            Seis especialidades con agenda propia. Elegí una y te mostramos los médicos que atienden.
          </p>
        </div>

        <div className="mt-10 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {especialidades.map((especialidad) => {
            const Icono = iconosEspecialidad[especialidad.nombre] ?? Activity
            const activa = especialidadId === especialidad.id
            return (
              <button
                key={especialidad.id}
                type="button"
                onClick={() => elegirEspecialidad(especialidad.id)}
                className={`group flex items-center gap-4 rounded-3xl border p-5 text-left transition ${
                  activa
                    ? 'border-forest bg-forest text-paper'
                    : 'border-ink/10 bg-white/70 hover:-translate-y-0.5 hover:border-forest/30'
                }`}
              >
                <span
                  className={`grid size-12 shrink-0 place-items-center rounded-2xl ${
                    activa ? 'bg-paper/15 text-paper' : 'bg-sage-light text-forest'
                  }`}
                >
                  <Icono className="size-5" />
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block font-display text-lg leading-tight">{especialidad.nombre}</span>
                  <span
                    className={`mt-0.5 block text-xs ${activa ? 'text-paper/60' : 'text-ink/45'}`}
                  >
                    Turnos de {especialidad.duracionDefaultMin} min
                  </span>
                </span>
                <ArrowRight
                  className={`size-4 transition group-hover:translate-x-1 ${
                    activa ? 'text-paper/70' : 'text-ink/25'
                  }`}
                />
              </button>
            )
          })}
        </div>
      </section>

      <section id="obras-sociales" className="scroll-mt-28 bg-forest py-20 text-paper">
        <div className="container-page grid gap-10 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.22em] text-gold">Coberturas</p>
            <h2 className="mt-3 text-4xl tracking-tight sm:text-5xl">
              Tu obra social,
              <span className="block italic text-sage">sin sorpresas.</span>
            </h2>
            <p className="mt-5 max-w-md text-sm leading-relaxed text-paper/65">
              Cada médico declara qué obras sociales y planes acepta. Si no trabaja con tu cobertura,
              vas a ver el precio particular antes de confirmar.
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            {obrasSociales.map((obra) => (
              <button
                key={obra.id}
                type="button"
                onClick={() => elegirObraSocial(obra.id)}
                className={`group rounded-3xl border p-5 text-left transition ${
                  obraSocialId === obra.id
                    ? 'border-gold bg-paper text-ink'
                    : 'border-paper/15 bg-paper/5 text-paper hover:border-paper/40 hover:bg-paper/10'
                }`}
              >
                <div className="flex items-center justify-between gap-3">
                  <span className="font-display text-xl">{obra.nombre}</span>
                  <ArrowRight className="size-4 opacity-50 transition group-hover:translate-x-1 group-hover:opacity-100" />
                </div>
                <p
                  className={`mt-1 text-xs uppercase tracking-[0.14em] ${
                    obraSocialId === obra.id ? 'text-ink/45' : 'text-paper/45'
                  }`}
                >
                  {obra.planes.length > 0
                    ? `${obra.planes.length} plan${obra.planes.length === 1 ? '' : 'es'}`
                    : 'Sin planes cargados'}
                </p>
              </button>
            ))}
          </div>
        </div>
      </section>

      <section id="como-funciona" className="container-page scroll-mt-28 py-20">
        <div className="max-w-2xl">
          <p className="eyebrow">Cómo funciona</p>
          <h2 className="mt-2 text-4xl tracking-tight sm:text-5xl">Reservar es simple.</h2>
        </div>
        <div className="mt-10 grid gap-5 md:grid-cols-3">
          {[
            {
              numero: '01',
              titulo: 'Buscá',
              texto: 'Filtrá por especialidad u obra social y mirá los horarios libres de cada médico.',
              icono: Search,
            },
            {
              numero: '02',
              titulo: 'Verificá tu identidad',
              texto: 'Ingresá con DNI y apellido. Te enviamos un código de 6 dígitos al email.',
              icono: ShieldCheck,
            },
            {
              numero: '03',
              titulo: 'Confirmá',
              texto: 'Elegí el horario, contanos el motivo y listo. Te llega la confirmación por email.',
              icono: CalendarCheck,
            },
          ].map((paso) => (
            <div key={paso.numero} className="card relative overflow-hidden p-6">
              <span className="absolute right-4 top-3 font-display text-5xl text-ink/5">{paso.numero}</span>
              <span className="grid size-11 place-items-center rounded-2xl bg-sage-light text-forest">
                <paso.icono className="size-5" />
              </span>
              <h3 className="mt-5 font-display text-2xl">{paso.titulo}</h3>
              <p className="mt-2 text-sm leading-relaxed text-ink/60">{paso.texto}</p>
            </div>
          ))}
        </div>
      </section>

      <section id="resultados" className="container-page scroll-mt-28 pb-4">
        <div className="flex flex-wrap items-end justify-between gap-4 border-t border-ink/10 pt-12">
          <div>
            <p className="eyebrow">Agenda</p>
            <h2 className="mt-2 text-4xl tracking-tight sm:text-5xl">
              {hayFiltros ? 'Resultados para tu búsqueda' : 'Nuestros profesionales'}
            </h2>
            <p className="mt-2 text-sm text-ink/55">
              {isFetching ? 'Actualizando…' : `${medicos.length} profesional${medicos.length === 1 ? '' : 'es'} disponible${medicos.length === 1 ? '' : 's'}`}
            </p>
          </div>
          {hayFiltros && (
            <button type="button" onClick={limpiar} className="btn-ghost">
              Ver todos
            </button>
          )}
        </div>

        <div className="mt-8">
          {isFetching && medicos.length === 0 && <Spinner label="Buscando médicos…" />}

          {isError && (
            <EmptyState
              titulo="No pudimos cargar la agenda"
              descripcion="Revisá que el backend esté funcionando e intentá de nuevo en unos segundos."
            />
          )}

          {!isError && !isFetching && medicos.length === 0 && (
            <EmptyState
              titulo="No hay médicos con esos filtros"
              descripcion="Probá con otra especialidad u obra social. También podés ver todos los profesionales."
              icono={<Stethoscope className="size-6" />}
              accion={
                <button type="button" onClick={limpiar} className="btn-primary">
                  Ver todos los médicos
                </button>
              }
            />
          )}

          {medicos.length > 0 && (
            <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
              {medicos.map((medico) => (
                <MedicoCard key={medico.id} medico={medico} />
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
