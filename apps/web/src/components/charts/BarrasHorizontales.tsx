import { entero } from '../../utils/formato'

/** Barras horizontales (por embarcadero de origen, calificación por aspecto). */
export function BarrasHorizontales({ items, formato = entero }: { items: { nombre: string; valor: number; color?: string }[]; formato?: (n: number) => string }) {
  const max = Math.max(1, ...items.map((i) => i.valor))
  return (
    <ul className="m-0 p-0 list-none flex flex-col gap-3">
      {items.map((i) => (
        <li key={i.nombre} className="grid grid-cols-[minmax(90px,1fr)_2fr_auto] items-center gap-3 text-[13px]">
          <span className="truncate">{i.nombre}</span>
          <span className="h-2 rounded-full bg-fondo overflow-hidden" aria-hidden>
            <span className="block h-full rounded-full" style={{ width: `${(i.valor / max) * 100}%`, background: i.color ?? '#013996' }} />
          </span>
          <b className="text-right min-w-[48px]">{formato(i.valor)}</b>
        </li>
      ))}
    </ul>
  )
}
