import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { X } from 'lucide-react'

interface ModalProps {
  abierto: boolean
  onCerrar: () => void
  titulo: string
  descripcion?: string
  children: ReactNode
  ancho?: 'sm' | 'md' | 'lg'
}

export function Modal({ abierto, onCerrar, titulo, descripcion, children, ancho = 'md' }: ModalProps) {
  useEffect(() => {
    if (!abierto) return
    const manejar = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onCerrar()
    }
    document.addEventListener('keydown', manejar)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', manejar)
      document.body.style.overflow = ''
    }
  }, [abierto, onCerrar])

  if (!abierto) return null

  const maxAncho = ancho === 'sm' ? 'max-w-md' : ancho === 'lg' ? 'max-w-3xl' : 'max-w-xl'

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center p-0 sm:items-center sm:p-6">
      <button
        type="button"
        aria-label="Cerrar"
        onClick={onCerrar}
        className="absolute inset-0 bg-ink/45 backdrop-blur-sm"
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className={`relative z-10 w-full ${maxAncho} max-h-[90vh] overflow-y-auto rounded-t-[2rem] border border-ink/10 bg-paper p-6 shadow-2xl sm:rounded-[2rem] sm:p-8`}
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="font-display text-2xl">{titulo}</h2>
            {descripcion && <p className="mt-1 text-sm leading-relaxed text-ink/55">{descripcion}</p>}
          </div>
          <button
            type="button"
            onClick={onCerrar}
            className="grid size-9 shrink-0 place-items-center rounded-full border border-ink/10 text-ink/50 transition hover:bg-white hover:text-ink"
            aria-label="Cerrar"
          >
            <X className="size-4" />
          </button>
        </div>
        <div className="mt-5">{children}</div>
      </div>
    </div>
  )
}
