import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../auth/AuthContext'
import type { Rol } from '../lib/types'
import { Spinner } from './Spinner'

interface ProtectedRouteProps {
  roles?: Rol[]
  children: ReactNode
}

export function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
  const { usuario, cargando } = useAuth()
  const location = useLocation()

  if (cargando) {
    return (
      <div className="container-page grid min-h-[50vh] place-items-center">
        <Spinner label="Verificando tu sesión…" />
      </div>
    )
  }

  if (!usuario) {
    const redirect = encodeURIComponent(`${location.pathname}${location.search}`)
    return <Navigate to={`/ingresar?redirect=${redirect}`} replace />
  }

  if (roles && !roles.includes(usuario.rol)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}
