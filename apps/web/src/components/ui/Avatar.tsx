import { iniciales } from '../../utils/formato'
import { cn } from './cn'

const FONDOS = ['bg-noche text-dorado', 'bg-azul text-white', 'bg-azul-claro text-white']

/** Avatar circular con iniciales (encabezado, tripulación, personal). */
export function Avatar({ nombre, tamano = 38, className }: { nombre: string; tamano?: number; className?: string }) {
  const indice = [...nombre].reduce((s, c) => s + c.charCodeAt(0), 0) % FONDOS.length
  return (
    <span
      aria-hidden
      className={cn('flex-none rounded-full flex items-center justify-center font-titulo', FONDOS[indice], className)}
      style={{ width: tamano, height: tamano, fontSize: Math.round(tamano * 0.37) }}
    >
      {iniciales(nombre)}
    </span>
  )
}
