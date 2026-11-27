import type { ReactNode } from 'react'

interface EmptyStateProps {
  titulo: string
  descripcion: string
  icono?: ReactNode
  accion?: ReactNode
}

export function EmptyState({ titulo, descripcion, icono, accion }: EmptyStateProps) {
  return (
    <div className="card flex flex-col items-center gap-4 px-6 py-14 text-center">
      {icono && (
        <div className="grid size-14 place-items-center rounded-2xl bg-sage-light text-forest">
          {icono}
        </div>
      )}
      <div>
        <h3 className="font-display text-2xl">{titulo}</h3>
        <p className="mx-auto mt-2 max-w-md text-sm leading-relaxed text-ink/60">{descripcion}</p>
      </div>
      {accion}
    </div>
  )
}
