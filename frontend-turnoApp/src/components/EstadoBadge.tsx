import type { EstadoTurno } from '../lib/types'
import { estadoLabels, estadoTone } from '../lib/format'

export function EstadoBadge({ estado }: { estado: EstadoTurno }) {
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[0.68rem] font-bold uppercase tracking-[0.08em] ${estadoTone(estado)}`}
    >
      {estadoLabels[estado]}
    </span>
  )
}
