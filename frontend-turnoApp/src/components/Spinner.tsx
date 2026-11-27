import { Loader2 } from 'lucide-react'

export function Spinner({ label = 'Cargando…', className = '' }: { label?: string; className?: string }) {
  return (
    <div className={`flex items-center gap-3 text-sm font-semibold text-ink/60 ${className}`}>
      <Loader2 className="size-5 animate-spin text-clay" />
      <span>{label}</span>
    </div>
  )
}
