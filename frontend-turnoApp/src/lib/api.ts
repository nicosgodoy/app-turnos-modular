import type { AuthResponse, ProblemDetails } from './types'

const API_URL =
  (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '') ??
  'http://localhost:8080'

const TOKEN_KEY = 'sf_access_token'
const USER_KEY = 'sf_usuario'

export class ApiError extends Error {
  status: number
  codigo: string | null

  constructor(message: string, status: number, codigo: string | null = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.codigo = codigo
  }
}

let accessToken: string | null = localStorage.getItem(TOKEN_KEY)

export function getAccessToken(): string | null {
  return accessToken
}

export function setAccessToken(token: string | null): void {
  accessToken = token
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function getStoredUser(): AuthResponse['usuario'] | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthResponse['usuario']
  } catch {
    return null
  }
}

export function setStoredUser(usuario: AuthResponse['usuario'] | null): void {
  if (usuario) {
    localStorage.setItem(USER_KEY, JSON.stringify(usuario))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

let refreshInFlight: Promise<boolean> | null = null

async function refreshSession(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = fetch(`${API_URL}/auth/refresh`, {
      method: 'POST',
      credentials: 'include',
    })
      .then(async (response) => {
        if (!response.ok) return false
        const data = (await response.json()) as AuthResponse
        setAccessToken(data.accessToken)
        setStoredUser(data.usuario)
        return true
      })
      .catch(() => false)
      .finally(() => {
        refreshInFlight = null
      })
  }
  return refreshInFlight
}

interface RequestOptions {
  method?: string
  body?: unknown
  headers?: Record<string, string>
  skipAuth?: boolean
  retry?: boolean
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, headers = {}, skipAuth = false, retry = true } = options
  const finalHeaders: Record<string, string> = { Accept: 'application/json', ...headers }

  if (body !== undefined) {
    finalHeaders['Content-Type'] = 'application/json'
  }
  const token = getAccessToken()
  if (token && !skipAuth) {
    finalHeaders.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_URL}${path}`, {
    method,
    headers: finalHeaders,
    credentials: 'include',
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (response.status === 401 && retry && !skipAuth && !path.startsWith('/auth/')) {
    const refreshed = await refreshSession()
    if (refreshed) {
      return request<T>(path, { ...options, retry: false })
    }
  }

  if (!response.ok) {
    let problem: ProblemDetails = {}
    try {
      problem = (await response.json()) as ProblemDetails
    } catch {
      // Sin cuerpo JSON.
    }
    const message =
      problem.detail ??
      problem.title ??
      `No pudimos completar la operación (HTTP ${response.status}).`
    throw new ApiError(message, response.status, problem.codigo ?? null)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    return (await response.text()) as T
  }
  return (await response.json()) as T
}

export { API_URL, refreshSession }
