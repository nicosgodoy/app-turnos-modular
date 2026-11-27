import { useSearchParams } from 'react-router-dom'
import { CalendarDays, LayoutGrid, Stethoscope, Users } from 'lucide-react'
import { AdminTurnos } from './AdminTurnos'
import { AdminMedicos } from './AdminMedicos'
import { AdminCatalogos } from './AdminCatalogos'
import { AdminPacientes } from './AdminPacientes'

const tabs = [
  { id: 'turnos', etiqueta: 'Turnos', icono: CalendarDays },
  { id: 'medicos', etiqueta: 'Médicos', icono: Stethoscope },
  { id: 'catalogos', etiqueta: 'Catálogos', icono: LayoutGrid },
  { id: 'pacientes', etiqueta: 'Pacientes', icono: Users },
] as const

type TabId = (typeof tabs)[number]['id']

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const tabParam = searchParams.get('tab')
  const tab: TabId = tabs.some((t) => t.id === tabParam) ? (tabParam as TabId) : 'turnos'

  const cambiarTab = (nuevo: TabId) => {
    setSearchParams(nuevo === 'turnos' ? {} : { tab: nuevo }, { replace: true })
  }

  return (
    <div className="container-page py-10 sm:py-14">
      <p className="eyebrow">Administración</p>
      <h1 className="mt-2 text-4xl tracking-tight sm:text-5xl">Panel de la clínica</h1>
      <p className="mt-2 text-sm text-ink/55">
        Gestioná turnos, profesionales, catálogos y pacientes desde un solo lugar.
      </p>

      <div className="mt-7 flex flex-wrap gap-2">
        {tabs.map((item) => (
          <button
            key={item.id}
            type="button"
            onClick={() => cambiarTab(item.id)}
            className={`inline-flex items-center gap-2 rounded-full border px-5 py-2.5 text-sm font-semibold transition ${
              tab === item.id
                ? 'border-forest bg-forest text-paper shadow'
                : 'border-ink/15 bg-white/60 text-ink/65 hover:border-ink/35 hover:text-ink'
            }`}
          >
            <item.icono className="size-4" />
            {item.etiqueta}
          </button>
        ))}
      </div>

      <div className="mt-8">
        {tab === 'turnos' && <AdminTurnos />}
        {tab === 'medicos' && <AdminMedicos />}
        {tab === 'catalogos' && <AdminCatalogos />}
        {tab === 'pacientes' && <AdminPacientes />}
      </div>
    </div>
  )
}