import { Link } from 'react-router-dom'
import { Compass } from 'lucide-react'
import { EmptyState } from '../components/EmptyState'

export function NoEncontradaPage() {
  return (
    <div className="container-page py-20">
      <EmptyState
        titulo="Página no encontrada"
        descripcion="La dirección que buscás no existe o cambió. Volvé al inicio para seguir reservando turnos."
        icono={<Compass className="size-6" />}
        accion={
          <Link to="/" className="btn-primary">
            Volver al inicio
          </Link>
        }
      />
    </div>
  )
}