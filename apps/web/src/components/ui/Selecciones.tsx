import type { ReactNode } from 'react'
import { Check } from 'lucide-react'
import { cn } from './cn'

interface Opcion<T extends string> { valor: T; etiqueta: ReactNode; deshabilitada?: boolean }

/** Chips de selección (plantilla 00): seleccionado en azul con check. */
export function Chips<T extends string>({ opciones, valor, onCambio, multiple, etiqueta }: {
  opciones: Opcion<T>[]
  valor: T | T[] | null | undefined
  onCambio: (v: T) => void
  multiple?: boolean
  etiqueta?: string
}) {
  const activos = Array.isArray(valor) ? valor : valor ? [valor] : []
  return (
    <div className="flex flex-wrap gap-2" role={multiple ? 'group' : 'radiogroup'} aria-label={etiqueta}>
      {opciones.map((o) => {
        const sel = activos.includes(o.valor)
        return (
          <button
            key={o.valor}
            type="button"
            role={multiple ? 'checkbox' : 'radio'}
            aria-checked={sel}
            disabled={o.deshabilitada}
            onClick={() => onCambio(o.valor)}
            className={cn(
              'inline-flex items-center gap-1.5 px-3 py-[7px] rounded-full border text-[13px] font-semibold whitespace-nowrap cursor-pointer transition-colors',
              'disabled:opacity-50 disabled:cursor-not-allowed',
              sel ? 'bg-azul border-azul text-white' : 'bg-white border-borde text-texto-2 hover:border-azul-claro',
            )}
          >
            {sel && <Check className="size-3.5" aria-hidden />}
            {o.etiqueta}
          </button>
        )
      })}
    </div>
  )
}

/** Control segmentado (Día / Semana / Mes, Betancí / Yapel). */
export function Segmentado<T extends string>({ opciones, valor, onCambio, etiqueta, className }: {
  opciones: Opcion<T>[]; valor: T; onCambio: (v: T) => void; etiqueta?: string; className?: string
}) {
  return (
    <div className={cn('inline-flex gap-1 p-1 rounded-[13px] bg-fondo border border-borde', className)} role="radiogroup" aria-label={etiqueta}>
      {opciones.map((o) => (
        <button
          key={o.valor}
          type="button"
          role="radio"
          aria-checked={o.valor === valor}
          disabled={o.deshabilitada}
          onClick={() => onCambio(o.valor)}
          className={cn(
            'px-3.5 py-[7px] rounded-[10px] text-[13px] font-bold cursor-pointer whitespace-nowrap disabled:opacity-50',
            o.valor === valor ? 'bg-white text-azul shadow-tarjeta' : 'text-texto-2 hover:text-azul',
          )}
        >
          {o.etiqueta}
        </button>
      ))}
    </div>
  )
}

/** Pestañas con subrayado azul (General · Hoja de vida · Mantenimientos). */
export function Pestanas<T extends string>({ opciones, valor, onCambio, className }: {
  opciones: Opcion<T>[]; valor: T; onCambio: (v: T) => void; className?: string
}) {
  return (
    <div className={cn('flex gap-5 border-b border-borde overflow-x-auto', className)} role="tablist">
      {opciones.map((o) => (
        <button
          key={o.valor}
          type="button"
          role="tab"
          aria-selected={o.valor === valor}
          onClick={() => onCambio(o.valor)}
          className={cn(
            'pb-2.5 -mb-px text-[14px] whitespace-nowrap cursor-pointer border-b-[3px] transition-colors',
            o.valor === valor ? 'border-azul text-azul font-bold' : 'border-transparent text-texto-2 hover:text-azul',
          )}
        >
          {o.etiqueta}
        </button>
      ))}
    </div>
  )
}

/** Opción grande tipo tarjeta con radio (tipo de mantenimiento, tipo de novedad). */
export function OpcionTarjeta({ seleccionada, onClick, titulo, descripcion, icono }: {
  seleccionada: boolean; onClick: () => void; titulo: string; descripcion?: string; icono?: ReactNode
}) {
  return (
    <button
      type="button"
      role="radio"
      aria-checked={seleccionada}
      onClick={onClick}
      className={cn(
        'flex items-start gap-3 text-left p-3.5 rounded-[14px] border-2 cursor-pointer transition-colors w-full',
        seleccionada ? 'border-azul bg-azul-suave' : 'border-borde bg-white hover:border-azul-claro',
      )}
    >
      {icono}
      <span className="flex-1 min-w-0">
        <span className="block font-titulo text-[14px] text-noche">{titulo}</span>
        {descripcion && <span className="block text-[12px] text-texto-2 mt-0.5">{descripcion}</span>}
      </span>
      <span className={cn('flex-none size-[18px] rounded-full border-2 mt-0.5', seleccionada ? 'border-azul bg-[radial-gradient(circle,#013996_45%,#fff_50%)]' : 'border-borde')} aria-hidden />
    </button>
  )
}
