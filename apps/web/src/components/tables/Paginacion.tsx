import { ChevronLeft, ChevronRight } from 'lucide-react'
import { entero } from '../../utils/formato'
import { cn } from '../ui/cn'

/** Paginación de la plantilla 03: «Mostrando 1–10 de 412» y páginas numeradas. Página base 0 (Spring). */
export function Paginacion({ pagina, totalPaginas, totalElementos, tamano, onCambio, nombre = 'registros' }: {
  pagina: number; totalPaginas: number; totalElementos: number; tamano: number; onCambio: (p: number) => void; nombre?: string
}) {
  if (totalElementos === 0) return null
  const desde = pagina * tamano + 1
  const hasta = Math.min(totalElementos, (pagina + 1) * tamano)
  const paginas = rango(pagina, totalPaginas)
  return (
    <nav className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 border-t border-borde" aria-label="Paginación">
      <span className="text-[13px] text-texto-2">
        Mostrando {entero(desde)}–{entero(hasta)} de {entero(totalElementos)} {nombre}
      </span>
      {totalPaginas > 1 && (
        <div className="flex items-center gap-1">
          <BotonPagina etiqueta="Página anterior" disabled={pagina === 0} onClick={() => onCambio(pagina - 1)}>
            <ChevronLeft className="size-4" />
          </BotonPagina>
          {paginas.map((p, i) =>
            p === null ? (
              <span key={`e${i}`} className="px-2 text-texto-2">…</span>
            ) : (
              <BotonPagina key={p} activo={p === pagina} etiqueta={`Página ${p + 1}`} onClick={() => onCambio(p)}>
                {p + 1}
              </BotonPagina>
            ),
          )}
          <BotonPagina etiqueta="Página siguiente" disabled={pagina >= totalPaginas - 1} onClick={() => onCambio(pagina + 1)}>
            <ChevronRight className="size-4" />
          </BotonPagina>
        </div>
      )}
    </nav>
  )
}

function BotonPagina({ children, activo, disabled, onClick, etiqueta }: { children: React.ReactNode; activo?: boolean; disabled?: boolean; onClick: () => void; etiqueta: string }) {
  return (
    <button
      type="button"
      aria-label={etiqueta}
      aria-current={activo ? 'page' : undefined}
      disabled={disabled}
      onClick={onClick}
      className={cn(
        'min-w-[34px] h-[34px] px-2 rounded-[10px] text-[13px] font-bold flex items-center justify-center cursor-pointer disabled:opacity-35 disabled:cursor-not-allowed',
        activo ? 'bg-azul text-white' : 'text-texto-2 hover:bg-fondo',
      )}
    >
      {children}
    </button>
  )
}

function rango(actual: number, total: number): (number | null)[] {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)
  const set = new Set([0, total - 1, actual - 1, actual, actual + 1].filter((p) => p >= 0 && p < total))
  const orden = [...set].sort((a, b) => a - b)
  const r: (number | null)[] = []
  orden.forEach((p, i) => {
    if (i > 0 && p - orden[i - 1]! > 1) r.push(null)
    r.push(p)
  })
  return r
}
