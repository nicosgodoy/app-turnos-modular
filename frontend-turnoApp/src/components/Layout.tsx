import { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { CalendarDays, LogOut, Menu, X } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'

const roleHome: Record<string, string> = {
  ADMIN: '/admin',
  MEDICO: '/medico',
  PACIENTE: '/mis-turnos',
}

const roleLabel: Record<string, string> = {
  ADMIN: 'Administración',
  MEDICO: 'Mi agenda',
  PACIENTE: 'Mis turnos',
}

function Marca({ compacto = false }: { compacto?: boolean }) {
  return (
    <Link to="/" className="group flex items-center gap-3" aria-label="Clínica Sagrada Familia, inicio">
      <span className="relative grid size-11 place-items-center rounded-2xl bg-forest text-paper shadow-[0_10px_25px_-12px_rgba(36,75,61,0.9)]">
        <svg viewBox="0 0 40 40" className="size-7" aria-hidden="true">
          <path
            d="M20 8v24M8 20h24"
            stroke="currentColor"
            strokeWidth="3.4"
            strokeLinecap="round"
          />
        </svg>
        <span className="absolute -right-1 -top-1 size-3 rounded-full border-2 border-paper bg-gold" />
      </span>
      <span className={compacto ? 'hidden sm:block' : 'block'}>
        <span className="block font-display text-lg leading-tight font-semibold tracking-tight">
          Sagrada Familia
        </span>
        <span className="block text-[0.62rem] font-bold uppercase tracking-[0.22em] text-ink/50">
          Clínica · Turnos
        </span>
      </span>
    </Link>
  )
}

export function Layout() {
  const { usuario, cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const [menuAbierto, setMenuAbierto] = useState(false)

  const cerrar = () => setMenuAbierto(false)

  const salir = async () => {
    await cerrarSesion()
    cerrar()
    navigate('/')
  }

  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-full focus:bg-forest focus:px-4 focus:py-2 focus:text-paper"
      >
        Ir al contenido
      </a>

      <header className="sticky top-0 z-40 border-b border-ink/10 bg-paper/85 backdrop-blur-xl">
        <div className="container-page flex h-20 items-center justify-between gap-4">
          <Marca />

          <nav className="hidden items-center gap-7 text-sm font-semibold text-ink/70 lg:flex">
            <NavLink to="/" className={({ isActive }) => (isActive ? 'text-ink' : 'hover:text-ink')}>
              Buscar turnos
            </NavLink>
            <Link to="/#especialidades" className="hover:text-ink">
              Especialidades
            </Link>
            <Link to="/#obras-sociales" className="hover:text-ink">
              Obras sociales
            </Link>
            <Link to="/#como-funciona" className="hover:text-ink">
              Cómo funciona
            </Link>
          </nav>

          <div className="hidden items-center gap-3 sm:flex">
            {usuario ? (
              <>
                <Link
                  to={roleHome[usuario.rol] ?? '/'}
                  className="inline-flex items-center gap-2 rounded-full border border-ink/15 bg-white/60 px-4 py-2 text-sm font-semibold transition hover:border-ink/35"
                >
                  <CalendarDays className="size-4 text-forest" />
                  {roleLabel[usuario.rol] ?? 'Mi cuenta'}
                </Link>
                <button
                  type="button"
                  onClick={salir}
                  className="grid size-10 place-items-center rounded-full text-ink/60 transition hover:bg-white hover:text-ink"
                  aria-label="Cerrar sesión"
                  title={`Cerrar sesión de ${usuario.nombre}`}
                >
                  <LogOut className="size-4" />
                </button>
              </>
            ) : (
              <Link to="/ingresar" className="btn-primary">
                Ingresar
              </Link>
            )}
          </div>

          <button
            type="button"
            className="grid size-11 place-items-center rounded-2xl border border-ink/15 sm:hidden"
            onClick={() => setMenuAbierto((v) => !v)}
            aria-expanded={menuAbierto}
            aria-label="Abrir menú"
          >
            {menuAbierto ? <X className="size-5" /> : <Menu className="size-5" />}
          </button>
        </div>

        {menuAbierto && (
          <div className="border-t border-ink/10 bg-paper px-5 py-5 sm:hidden">
            <nav className="flex flex-col gap-1 text-sm font-semibold">
              <Link to="/" onClick={cerrar} className="rounded-xl px-3 py-2.5 hover:bg-white/70">
                Buscar turnos
              </Link>
              <Link to="/#especialidades" onClick={cerrar} className="rounded-xl px-3 py-2.5 hover:bg-white/70">
                Especialidades
              </Link>
              <Link to="/#obras-sociales" onClick={cerrar} className="rounded-xl px-3 py-2.5 hover:bg-white/70">
                Obras sociales
              </Link>
              <Link to="/#como-funciona" onClick={cerrar} className="rounded-xl px-3 py-2.5 hover:bg-white/70">
                Cómo funciona
              </Link>
            </nav>
            <div className="mt-4 flex flex-col gap-2 border-t border-ink/10 pt-4">
              {usuario ? (
                <>
                  <Link
                    to={roleHome[usuario.rol] ?? '/'}
                    onClick={cerrar}
                    className="btn-primary w-full"
                  >
                    {roleLabel[usuario.rol] ?? 'Mi cuenta'}
                  </Link>
                  <button type="button" onClick={salir} className="btn-ghost w-full">
                    Cerrar sesión
                  </button>
                </>
              ) : (
                <Link to="/ingresar" onClick={cerrar} className="btn-primary w-full">
                  Ingresar
                </Link>
              )}
            </div>
          </div>
        )}
      </header>

      <main id="contenido" className="flex-1">
        <Outlet />
      </main>

      <footer className="mt-24 border-t border-ink/10 bg-forest text-paper">
        <div className="container-page grid gap-10 py-14 md:grid-cols-[1.4fr_1fr_1fr]">
          <div>
            <div className="flex items-center gap-3">
              <span className="grid size-11 place-items-center rounded-2xl bg-paper/10">
                <svg viewBox="0 0 40 40" className="size-6" aria-hidden="true">
                  <path
                    d="M20 8v24M8 20h24"
                    stroke="currentColor"
                    strokeWidth="3.4"
                    strokeLinecap="round"
                  />
                </svg>
              </span>
              <div>
                <p className="font-display text-xl">Clínica Sagrada Familia</p>
                <p className="text-xs uppercase tracking-[0.2em] text-paper/50">Cuidado cercano</p>
              </div>
            </div>
            <p className="mt-5 max-w-sm text-sm leading-relaxed text-paper/65">
              Una clínica de barrio con agenda digital. Reservá, reprogramá y recibí recordatorios sin
              llamadas ni esperas.
            </p>
          </div>
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-paper/45">Contacto</p>
            <ul className="mt-4 space-y-2 text-sm text-paper/75">
              <li>Gral. Paz y Libertad 3</li>
              <li>Lunes a viernes · 8 a 20 h</li>
              <li>turnos@sagradafamilia.local</li>
            </ul>
          </div>
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-paper/45">Accesos</p>
            <ul className="mt-4 space-y-2 text-sm text-paper/75">
              <li>
                <Link to="/" className="hover:text-paper">
                  Buscar turnos
                </Link>
              </li>
              <li>
                <Link to="/ingresar" className="hover:text-paper">
                  Ingresar
                </Link>
              </li>
              <li>
                <Link to="/mis-turnos" className="hover:text-paper">
                  Mis turnos
                </Link>
              </li>
            </ul>
          </div>
        </div>
        <div className="border-t border-paper/10 py-5 text-center text-xs text-paper/45">
          Datos protegidos según la Ley 25.326. Clínica Sagrada Familia · {new Date().getFullYear()}
        </div>
      </footer>
    </div>
  )
}
