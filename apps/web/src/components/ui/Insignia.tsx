import type { ReactNode } from 'react'
import { cn } from './cn'

/** Componente Badge con 7 tonos (plantilla 00). */
export type Tono = 'ok' | 'warn' | 'err' | 'info' | 'neutral' | 'gold' | 'noche'

const TONOS: Record<Tono, { fondo: string; texto: string; punto: string }> = {
  ok: { fondo: 'bg-exito-suave', texto: 'text-exito', punto: 'bg-exito' },
  warn: { fondo: 'bg-advertencia-suave', texto: 'text-advertencia', punto: 'bg-advertencia' },
  err: { fondo: 'bg-error-suave', texto: 'text-error', punto: 'bg-error' },
  info: { fondo: 'bg-info-suave', texto: 'text-azul', punto: 'bg-azul' },
  neutral: { fondo: 'bg-fondo', texto: 'text-texto-2', punto: 'bg-texto-2' },
  gold: { fondo: 'bg-dorado-suave', texto: 'text-noche', punto: 'bg-noche' },
  noche: { fondo: 'bg-noche-suave', texto: 'text-noche', punto: 'bg-noche' },
}

export function Insignia({ tono = 'neutral', children, sinPunto, className }: { tono?: Tono; children: ReactNode; sinPunto?: boolean; className?: string }) {
  const t = TONOS[tono]
  return (
    <span className={cn('inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[12px] font-bold whitespace-nowrap', t.fondo, t.texto, className)}>
      {!sinPunto && <span className={cn('size-1.5 rounded-full', t.punto)} aria-hidden />}
      {children}
    </span>
  )
}
