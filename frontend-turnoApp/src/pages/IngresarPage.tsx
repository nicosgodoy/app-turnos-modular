import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import {
  ArrowLeft,
  ArrowRight,
  CalendarDays,
  CheckCircle2,
  KeyRound,
  Mail,
  ShieldCheck,
  Sparkles,
  Stethoscope,
  UserRound,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { catalogoApi } from '../lib/endpoints'
import type { OtpPayload } from '../lib/endpoints'
import { authApi } from '../lib/endpoints'
import { ApiError } from '../lib/api'
import type { OtpSolicitado, Rol } from '../lib/types'

const roleHome: Record<Rol, string> = {
  ADMIN: '/admin',
  MEDICO: '/medico',
  PACIENTE: '/mis-turnos',
}

function destinoSeguro(redirect: string | null, rol: Rol): string {
  if (redirect && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return redirect
  }
  return roleHome[rol]
}

export function IngresarPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { iniciarStaff, verificarOtp } = useAuth()
  const redirect = searchParams.get('redirect')

  const [tab, setTab] = useState<'paciente' | 'staff'>(
    searchParams.get('tab') === 'staff' ? 'staff' : 'paciente',
  )

  const [paso, setPaso] = useState<'datos' | 'codigo'>('datos')
  const [primeraVez, setPrimeraVez] = useState(false)
  const [dni, setDni] = useState('')
  const [apellido, setApellido] = useState('')
  const [nombre, setNombre] = useState('')
  const [email, setEmail] = useState('')
  const [telefono, setTelefono] = useState('')
  const [fechaNacimiento, setFechaNacimiento] = useState('')
  const [obraSocialId, setObraSocialId] = useState('')
  const [planId, setPlanId] = useState('')
  const [numeroAfiliado, setNumeroAfiliado] = useState('')
  const [consentimiento, setConsentimiento] = useState(false)
  const [otpInfo, setOtpInfo] = useState<OtpSolicitado | null>(null)
  const [codigo, setCodigo] = useState('')

  const [staffEmail, setStaffEmail] = useState('')
  const [staffPassword, setStaffPassword] = useState('')

  const { data: obrasSociales = [] } = useQuery({
    queryKey: ['obras-sociales'],
    queryFn: catalogoApi.obrasSociales,
    enabled: primeraVez,
  })

  const planes = useMemo(
    () => obrasSociales.find((obra) => obra.id === obraSocialId)?.planes ?? [],
    [obrasSociales, obraSocialId],
  )

  const solicitar = useMutation({
    mutationFn: (payload: OtpPayload) => authApi.solicitarOtp(payload),
    onSuccess: (data) => {
      setOtpInfo(data)
      setPaso('codigo')
      setCodigo('')
    },
  })

  const verificar = useMutation({
    mutationFn: () => verificarOtp(dni, codigo),
    onSuccess: (data) => {
      navigate(destinoSeguro(redirect, data.usuario.rol), { replace: true })
    },
  })

  const staffLogin = useMutation({
    mutationFn: () => iniciarStaff(staffEmail, staffPassword),
    onSuccess: (data) => {
      navigate(destinoSeguro(redirect, data.usuario.rol), { replace: true })
    },
  })

  const construirPayload = (): OtpPayload => {
    const base: OtpPayload = { dni: dni.replace(/\D/g, ''), apellido: apellido.trim() }
    if (!primeraVez) return base
    return {
      ...base,
      nombre: nombre.trim(),
      email: email.trim(),
      telefono: telefono.trim(),
      fechaNacimiento: fechaNacimiento || null,
      obraSocialId: obraSocialId || null,
      planId: planId || null,
      numeroAfiliado: numeroAfiliado.trim() || null,
      consentimiento,
    }
  }

  const pedirCodigo = (event: FormEvent) => {
    event.preventDefault()
    solicitar.mutate(construirPayload())
  }

  const confirmarCodigo = (event: FormEvent) => {
    event.preventDefault()
    verificar.mutate()
  }

  const ingresarStaff = (event: FormEvent) => {
    event.preventDefault()
    staffLogin.mutate()
  }

  const errorPaciente =
    solicitar.error instanceof ApiError
      ? solicitar.error.message
      : verificar.error instanceof ApiError
        ? verificar.error.message
        : null

  const errorStaff = staffLogin.error instanceof ApiError ? staffLogin.error.message : null

  return (
    <div className="container-page py-10 sm:py-16">
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-sm font-semibold text-ink/55 transition hover:text-ink"
      >
        <ArrowLeft className="size-4" /> Volver al inicio
      </Link>

      <div className="mt-6 grid gap-8 lg:grid-cols-[0.95fr_1.05fr] lg:items-stretch">
        <aside className="relative hidden overflow-hidden rounded-[2rem] bg-forest p-8 text-paper lg:flex lg:flex-col lg:justify-between">
          <div className="pointer-events-none absolute -right-24 -top-24 size-72 rounded-full bg-sage/20 blur-2xl" />
          <div className="pointer-events-none absolute -bottom-20 -left-16 size-64 rounded-full bg-clay/25 blur-3xl" />

          <div className="relative">
            <p className="text-xs font-bold uppercase tracking-[0.22em] text-gold">Acceso</p>
            <h1 className="mt-4 text-balance font-display text-4xl leading-[1.02]">
              Tu historia clínica empieza con
              <span className="block italic text-sage">un código.</span>
            </h1>
            <p className="mt-5 max-w-sm text-sm leading-relaxed text-paper/65">
              Los pacientes ingresan con DNI y un código de 6 dígitos. El equipo de la clínica usa su
              email y contraseña.
            </p>

            <div className="mt-8 space-y-4">
              {[
                {
                  icono: ShieldCheck,
                  titulo: 'Sin contraseñas para pacientes',
                  texto: 'El código vence a los 10 minutos y se envía a tu email.',
                },
                {
                  icono: CalendarDays,
                  titulo: 'Tu agenda, siempre a mano',
                  texto: 'Reservá, cancelá o reprogramá desde cualquier dispositivo.',
                },
                {
                  icono: Mail,
                  titulo: 'Avisos por email',
                  texto: 'Confirmación al reservar y recordatorio 24 horas antes.',
                },
              ].map((item) => (
                <div key={item.titulo} className="flex items-start gap-3">
                  <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-paper/10">
                    <item.icono className="size-4" />
                  </span>
                  <div>
                    <p className="text-sm font-semibold">{item.titulo}</p>
                    <p className="text-xs leading-relaxed text-paper/55">{item.texto}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="relative mt-10 rounded-2xl border border-paper/10 bg-paper/5 p-4">
            <p className="text-xs leading-relaxed text-paper/60">
              “La atención cercana también es digital: menos espera, más tiempo para lo importante.”
            </p>
            <p className="mt-2 text-[0.65rem] font-bold uppercase tracking-[0.16em] text-paper/40">
              Clínica Sagrada Familia
            </p>
          </div>
        </aside>

        <section className="card p-6 sm:p-8">
          <div className="flex rounded-full bg-paper-deep/70 p-1">
            <button
              type="button"
              onClick={() => setTab('paciente')}
              className={`flex-1 rounded-full px-4 py-2.5 text-sm font-semibold transition ${
                tab === 'paciente' ? 'bg-forest text-paper shadow' : 'text-ink/55 hover:text-ink'
              }`}
            >
              Soy paciente
            </button>
            <button
              type="button"
              onClick={() => setTab('staff')}
              className={`flex-1 rounded-full px-4 py-2.5 text-sm font-semibold transition ${
                tab === 'staff' ? 'bg-forest text-paper shadow' : 'text-ink/55 hover:text-ink'
              }`}
            >
              Soy del equipo
            </button>
          </div>

          {tab === 'paciente' && paso === 'datos' && (
            <form onSubmit={pedirCodigo} className="mt-7">
              <p className="eyebrow">Pacientes</p>
              <h2 className="mt-1 font-display text-3xl">Ingresá con tu DNI</h2>
              <p className="mt-2 text-sm leading-relaxed text-ink/55">
                Te enviamos un código de 6 dígitos al email que tenemos registrado.
              </p>

              <div className="mt-6 grid gap-4 sm:grid-cols-2">
                <div>
                  <label htmlFor="dni" className="label">
                    DNI
                  </label>
                  <input
                    id="dni"
                    className="field"
                    inputMode="numeric"
                    autoComplete="off"
                    placeholder="30111222"
                    value={dni}
                    onChange={(event) => setDni(event.target.value)}
                    required
                  />
                </div>
                <div>
                  <label htmlFor="apellido" className="label">
                    Apellido
                  </label>
                  <input
                    id="apellido"
                    className="field"
                    autoComplete="family-name"
                    placeholder="Paciente"
                    value={apellido}
                    onChange={(event) => setApellido(event.target.value)}
                    required
                  />
                </div>
              </div>

              <button
                type="button"
                onClick={() => setPrimeraVez((valor) => !valor)}
                className="mt-4 flex w-full items-center justify-between rounded-2xl border border-ink/10 bg-paper/60 px-4 py-3 text-left transition hover:border-forest/30"
              >
                <span className="flex items-center gap-3">
                  <span className="grid size-9 place-items-center rounded-xl bg-sage-light text-forest">
                    <UserRound className="size-4" />
                  </span>
                  <span>
                    <span className="block text-sm font-semibold">Es mi primera vez</span>
                    <span className="block text-xs text-ink/50">Necesito completar mis datos</span>
                  </span>
                </span>
                <span
                  className={`relative h-6 w-11 rounded-full transition ${
                    primeraVez ? 'bg-forest' : 'bg-ink/15'
                  }`}
                >
                  <span
                    className={`absolute top-0.5 size-5 rounded-full bg-white shadow transition ${
                      primeraVez ? 'left-[1.35rem]' : 'left-0.5'
                    }`}
                  />
                </span>
              </button>

              {primeraVez && (
                <div className="mt-4 space-y-4 rounded-2xl border border-ink/10 bg-white/50 p-4">
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label htmlFor="nombre" className="label">
                        Nombre
                      </label>
                      <input
                        id="nombre"
                        className="field"
                        autoComplete="given-name"
                        value={nombre}
                        onChange={(event) => setNombre(event.target.value)}
                        required={primeraVez}
                      />
                    </div>
                    <div>
                      <label htmlFor="telefono" className="label">
                        Teléfono
                      </label>
                      <input
                        id="telefono"
                        className="field"
                        autoComplete="tel"
                        value={telefono}
                        onChange={(event) => setTelefono(event.target.value)}
                        required={primeraVez}
                      />
                    </div>
                  </div>
                  <div>
                    <label htmlFor="email" className="label">
                      Email
                    </label>
                    <input
                      id="email"
                      type="email"
                      className="field"
                      autoComplete="email"
                      value={email}
                      onChange={(event) => setEmail(event.target.value)}
                      required={primeraVez}
                    />
                  </div>
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label htmlFor="fecha-nacimiento" className="label">
                        Fecha de nacimiento
                      </label>
                      <input
                        id="fecha-nacimiento"
                        type="date"
                        className="field"
                        value={fechaNacimiento}
                        onChange={(event) => setFechaNacimiento(event.target.value)}
                      />
                    </div>
                    <div>
                      <label htmlFor="afiliado" className="label">
                        N° de afiliado
                      </label>
                      <input
                        id="afiliado"
                        className="field"
                        value={numeroAfiliado}
                        onChange={(event) => setNumeroAfiliado(event.target.value)}
                      />
                    </div>
                  </div>
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label htmlFor="obra" className="label">
                        Obra social
                      </label>
                      <select
                        id="obra"
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
                      <label htmlFor="plan" className="label">
                        Plan
                      </label>
                      <select
                        id="plan"
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
                  </div>
                  <label className="flex items-start gap-3 text-xs leading-relaxed text-ink/60">
                    <input
                      type="checkbox"
                      checked={consentimiento}
                      onChange={(event) => setConsentimiento(event.target.checked)}
                      className="mt-0.5 accent-[#244b3d]"
                      required={primeraVez}
                    />
                    <span>
                      Doy mi consentimiento para el tratamiento de mis datos personales con fines de
                      atención médica, conforme a la Ley 25.326.
                    </span>
                  </label>
                </div>
              )}

              {errorPaciente && (
                <p className="mt-4 rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
                  {errorPaciente}
                </p>
              )}

              <button type="submit" disabled={solicitar.isPending} className="btn-primary mt-6 w-full">
                {solicitar.isPending ? 'Enviando código…' : 'Enviarme el código'}
                {!solicitar.isPending && <ArrowRight className="size-4" />}
              </button>

              <p className="mt-4 text-center text-xs leading-relaxed text-ink/45">
                Al ingresar aceptás los términos de uso y la política de privacidad.
              </p>
            </form>
          )}

          {tab === 'paciente' && paso === 'codigo' && (
            <form onSubmit={confirmarCodigo} className="mt-7">
              <div className="flex items-center gap-3">
                <span className="grid size-11 place-items-center rounded-2xl bg-sage-light text-forest">
                  <KeyRound className="size-5" />
                </span>
                <div>
                  <p className="eyebrow">Último paso</p>
                  <h2 className="font-display text-2xl">Ingresá el código</h2>
                </div>
              </div>

              <p className="mt-4 rounded-2xl bg-paper/70 px-4 py-3 text-sm leading-relaxed text-ink/60">
                {otpInfo?.mensaje ?? 'Te enviamos un código de 6 dígitos.'} Vence en{' '}
                <strong>{otpInfo?.expiraEnMinutos ?? 10} minutos</strong>.
              </p>

              <div className="mt-6">
                <label htmlFor="codigo" className="label">
                  Código de verificación
                </label>
                <input
                  id="codigo"
                  className="field text-center font-display text-3xl tracking-[0.45em]"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  placeholder="000000"
                  value={codigo}
                  onChange={(event) => setCodigo(event.target.value.replace(/\D/g, '').slice(0, 6))}
                  required
                />
              </div>

              {errorPaciente && (
                <p className="mt-4 rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
                  {errorPaciente}
                </p>
              )}

              <button
                type="submit"
                disabled={verificar.isPending || codigo.length !== 6}
                className="btn-primary mt-5 w-full"
              >
                {verificar.isPending ? 'Verificando…' : 'Ingresar'}
                {!verificar.isPending && <CheckCircle2 className="size-4" />}
              </button>

              <div className="mt-4 flex flex-col gap-2 text-center">
                <button
                  type="button"
                  onClick={() => solicitar.mutate(construirPayload())}
                  disabled={solicitar.isPending}
                  className="text-sm font-semibold text-forest underline decoration-forest/30 underline-offset-4 hover:decoration-forest"
                >
                  {solicitar.isPending ? 'Reenviando…' : 'Reenviar código'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setPaso('datos')
                    setOtpInfo(null)
                    setCodigo('')
                  }}
                  className="text-xs font-semibold text-ink/45 hover:text-ink"
                >
                  Cambiar DNI o apellido
                </button>
              </div>
            </form>
          )}

          {tab === 'staff' && (
            <form onSubmit={ingresarStaff} className="mt-7">
              <p className="eyebrow">Equipo de la clínica</p>
              <h2 className="mt-1 font-display text-3xl">Acceso al panel</h2>
              <p className="mt-2 text-sm leading-relaxed text-ink/55">
                Administradores y médicos ingresan con el email y la contraseña asignados.
              </p>

              <div className="mt-6 space-y-4">
                <div>
                  <label htmlFor="staff-email" className="label">
                    Email
                  </label>
                  <input
                    id="staff-email"
                    type="email"
                    className="field"
                    autoComplete="username"
                    placeholder="nombre@clinica.local"
                    value={staffEmail}
                    onChange={(event) => setStaffEmail(event.target.value)}
                    required
                  />
                </div>
                <div>
                  <label htmlFor="staff-password" className="label">
                    Contraseña
                  </label>
                  <input
                    id="staff-password"
                    type="password"
                    className="field"
                    autoComplete="current-password"
                    placeholder="••••••••"
                    value={staffPassword}
                    onChange={(event) => setStaffPassword(event.target.value)}
                    required
                  />
                </div>
              </div>

              {errorStaff && (
                <p className="mt-4 rounded-xl bg-clay/10 px-3 py-2 text-sm font-medium text-clay-deep">
                  {errorStaff}
                </p>
              )}

              <button type="submit" disabled={staffLogin.isPending} className="btn-primary mt-6 w-full">
                {staffLogin.isPending ? 'Ingresando…' : 'Ingresar al panel'}
                {!staffLogin.isPending && <ArrowRight className="size-4" />}
              </button>

              <div className="mt-6 rounded-2xl border border-dashed border-ink/15 bg-paper/50 p-4 text-xs leading-relaxed text-ink/50">
                <p className="flex items-center gap-2 font-semibold text-ink/65">
                  <Sparkles className="size-3.5 text-gold" /> Acceso de demostración
                </p>
                <p className="mt-2">
                  Admin: <strong>nicodepor3.1@gmail.com</strong> / <strong>admin1234</strong>
                </p>
                <p>
                  Médica: <strong>laura.gomez@sagradafamilia.local</strong> / <strong>demo1234</strong>
                </p>
              </div>
            </form>
          )}

          {tab === 'paciente' && paso === 'datos' && (
            <div className="mt-6 flex items-center justify-center gap-2 text-xs text-ink/45">
              <Stethoscope className="size-3.5" />
              ¿Sos del equipo? Usá la pestaña “Soy del equipo”.
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
