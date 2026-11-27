import { useEffect } from 'react'
import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { InicioPage } from './pages/InicioPage'
import { IngresarPage } from './pages/IngresarPage'
import { MedicoPage } from './pages/MedicoPage'
import { MisTurnosPage } from './pages/MisTurnosPage'
import { MedicoPanelPage } from './pages/MedicoPanelPage'
import { AdminPage } from './pages/admin/AdminPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
})

function ScrollAlCambiarRuta() {
  const { pathname, hash } = useLocation()

  useEffect(() => {
    if (hash) {
      document.getElementById(hash.slice(1))?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    } else {
      window.scrollTo({ top: 0 })
    }
  }, [pathname, hash])

  return null
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <ScrollAlCambiarRuta />
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<InicioPage />} />
              <Route path="/ingresar" element={<IngresarPage />} />
              <Route path="/medicos/:id" element={<MedicoPage />} />
              <Route
                path="/mis-turnos"
                element={
                  <ProtectedRoute roles={['PACIENTE']}>
                    <MisTurnosPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/medico"
                element={
                  <ProtectedRoute roles={['MEDICO']}>
                    <MedicoPanelPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin"
                element={
                  <ProtectedRoute roles={['ADMIN']}>
                    <AdminPage />
                  </ProtectedRoute>
                }
              />
              <Route path="*" element={<NoEncontradaPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App