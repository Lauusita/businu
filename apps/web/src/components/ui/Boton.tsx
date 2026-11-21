import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router'
import { Loader2 } from 'lucide-react'
import { cn } from './cn'

/**
 * Botones de la plantilla 00 (radio 15 px, Huninn 14 px).
 * «institucional» (azul noche + dorado) se reserva para la acción final de un flujo.
 */
export type VarianteBoton = 'institucional' | 'primario' | 'secundario' | 'suave' | 'peligro' | 'fantasma'

const VARIANTES: Record<VarianteBoton, string> = {
  institucional: 'bg-noche text-dorado border-transparent hover:bg-[#0a1d85]',
  primario: 'bg-azul text-white border-transparent hover:bg-noche',
  secundario: 'bg-white text-azul border-borde hover:bg-azul-suave',
  suave: 'bg-azul-suave text-azul border-transparent hover:bg-[#dbe9fb]',
  peligro: 'bg-error-suave text-error border-transparent hover:bg-[#f8d6d6]',
  fantasma: 'bg-transparent text-texto-2 border-transparent hover:bg-fondo',
}

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBoton
  pequeno?: boolean
  icono?: ReactNode
  cargando?: boolean
  a?: string
  soloIcono?: boolean
}

export function Boton({ variante = 'primario', pequeno, icono, cargando, a, soloIcono, className, children, disabled, ...resto }: Props) {
  const clases = cn(
    'inline-flex items-center justify-center gap-2 border font-titulo whitespace-nowrap transition-colors',
    'disabled:opacity-55 disabled:cursor-not-allowed cursor-pointer select-none',
    pequeno ? 'h-[34px] px-3.5 rounded-[999px] text-[13px]' : 'h-[42px] px-[18px] rounded-campo text-[14px]',
    soloIcono && (pequeno ? 'w-[34px] px-0' : 'w-[42px] px-0'),
    VARIANTES[variante],
    className,
  )
  const contenido = (
    <>
      {cargando ? <Loader2 className="size-4 animate-spin" aria-hidden /> : icono}
      {children}
    </>
  )
  if (a && !disabled) {
    return (
      <Link to={a} className={cn(clases, 'no-underline hover:no-underline', variante === 'institucional' ? 'hover:text-dorado' : variante === 'primario' ? 'hover:text-white' : '')}>
        {contenido}
      </Link>
    )
  }
  return (
    <button type="button" className={clases} disabled={disabled || cargando} aria-busy={cargando || undefined} {...resto}>
      {contenido}
    </button>
  )
}
