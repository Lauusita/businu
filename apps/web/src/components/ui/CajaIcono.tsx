import type { ReactNode } from 'react'
import { cn } from './cn'

export type TonoIcono = 'azul' | 'dorado' | 'exito' | 'advertencia' | 'error' | 'noche'
const TONOS: Record<TonoIcono, string> = {
  azul: 'bg-azul-suave text-azul',
  dorado: 'bg-dorado-suave text-advertencia',
  exito: 'bg-exito-suave text-exito',
  advertencia: 'bg-advertencia-suave text-advertencia',
  error: 'bg-error-suave text-error',
  noche: 'bg-noche-suave text-noche',
}

/** Caja de 34 px con radio 10 px para íconos de tarjetas e indicadores. */
export function CajaIcono({ children, tono = 'azul', className }: { children: ReactNode; tono?: TonoIcono; className?: string }) {
  return (
    <div className={cn('flex-none size-[34px] rounded-[10px] flex items-center justify-center [&_svg]:size-[18px]', TONOS[tono], className)} aria-hidden>
      {children}
    </div>
  )
}
