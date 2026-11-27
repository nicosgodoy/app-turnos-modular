import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { getStoredUser, refreshSession, setAccessToken, setStoredUser } from '../lib/api'
import { authApi } from '../lib/endpoints'
import type { AuthResponse, Usuario } from '../lib/types'

interface AuthContextValue {
  usuario: Usuario | null
  cargando: boolean
  iniciarStaff: (email: string, password: string) => Promise<AuthResponse>
  verificarOtp: (dni: string, codigo: string) => Promise<AuthResponse>
  cerrarSesion: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => getStoredUser())
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    let activo = true
    refreshSession().then((ok) => {
      if (!activo) return
      if (ok) {
        setUsuario(getStoredUser())
      } else {
        setAccessToken(null)
        setStoredUser(null)
        setUsuario(null)
      }
      setCargando(false)
    })
    return () => {
      activo = false
    }
  }, [])

  const iniciarStaff = useCallback(async (email: string, password: string) => {
    const data = await authApi.loginStaff({ email, password })
    setAccessToken(data.accessToken)
    setStoredUser(data.usuario)
    setUsuario(data.usuario)
    return data
  }, [])

  const verificarOtp = useCallback(async (dni: string, codigo: string) => {
    const data = await authApi.verificarOtp(dni, codigo)
    setAccessToken(data.accessToken)
    setStoredUser(data.usuario)
    setUsuario(data.usuario)
    return data
  }, [])

  const cerrarSesion = useCallback(async () => {
    try {
      await authApi.logout()
    } catch {
      // La sesión local se cierra igual.
    }
    setAccessToken(null)
    setStoredUser(null)
    setUsuario(null)
  }, [])

  const value = useMemo(
    () => ({ usuario, cargando, iniciarStaff, verificarOtp, cerrarSesion }),
    [usuario, cargando, iniciarStaff, verificarOtp, cerrarSesion],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return context
}
