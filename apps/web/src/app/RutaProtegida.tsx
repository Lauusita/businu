import type { ReactNode } from 'react'
import { ShieldOff } from 'lucide-react'
import { Vacio } from '../components/feedback/Estados'
import { Boton } from '../components/ui/Boton'
import { Tarjeta } from '../components/ui/Tarjeta'
import { usePermiso } from '../hooks/usePermiso'
import type { Permiso } from './permisos'
import { Pagina } from './layouts/Pagina'

/** Muestra la página solo si el rol tiene el permiso. La API valida de todos modos. */
export function RutaProtegida({ permiso, children }: { permiso: Permiso; children: ReactNode }) {
  const permitido = usePermiso(permiso)
  if (permitido) return <>{children}</>
  return (
    <Pagina titulo="Sin permisos">
      <Tarjeta>
        <Vacio titulo="Su rol no tiene acceso a esta sección" icono={<ShieldOff className="size-5" />} accion={<Boton a="/" variante="secundario">Ir al panel</Boton>}>
          Si necesita acceder, solicítelo al supervisor operativo.
        </Vacio>
      </Tarjeta>
    </Pagina>
  )
}
