import type { ReactNode } from 'react'
import { cn } from './cn'
import { CajaIcono, type TonoIcono } from './CajaIcono'

/** Tarjeta blanca de 16 px de radio con borde sutil (plantilla 00). */
export function Tarjeta({ children, className, sinRelleno, id }: { children: ReactNode; className?: string; sinRelleno?: boolean; id?: string }) {
  return (
    <section
      id={id}
      className={cn(
        'bg-white border border-borde rounded-tarjeta shadow-tarjeta min-w-0',
        sinRelleno ? 'overflow-hidden' : 'p-4 sm:p-5 flex flex-col gap-4',
        className,
      )}
    >
      {children}
    </section>
  )
}

/** Encabezado de tarjeta: ícono, título Huninn 16 px, subtítulo y acciones. */
export function EncabezadoTarjeta({
  titulo, subtitulo, icono, tono = 'azul', acciones, className,
}: { titulo: ReactNode; subtitulo?: ReactNode; icono?: ReactNode; tono?: TonoIcono; acciones?: ReactNode; className?: string }) {
  return (
    <div className={cn('flex items-start gap-3 min-w-0', className)}>
      {icono && <CajaIcono tono={tono}>{icono}</CajaIcono>}
      <div className="flex-1 min-w-0">
        <h2 className="m-0 font-titulo text-[16px] font-normal text-noche leading-tight">{titulo}</h2>
        {subtitulo && <p className="m-0 mt-0.5 text-[12px] text-texto-2">{subtitulo}</p>}
      </div>
      {acciones && <div className="flex items-center gap-2 flex-wrap justify-end">{acciones}</div>}
    </div>
  )
}

/** Encabezado numerado de los formularios por pasos (bitácora, mantenimiento, combustible). */
export function PasoTarjeta({ numero, titulo, subtitulo, acciones }: { numero: number; titulo: string; subtitulo?: string; acciones?: ReactNode }) {
  return (
    <div className="flex items-center gap-3">
      <span className="flex-none size-[26px] rounded-full bg-azul text-white font-titulo text-[13px] flex items-center justify-center" aria-hidden>
        {numero}
      </span>
      <div className="flex-1 min-w-0">
        <h2 className="m-0 font-titulo text-[16px] font-normal text-noche leading-tight">{titulo}</h2>
        {subtitulo && <p className="m-0 mt-0.5 text-[12px] text-texto-2">{subtitulo}</p>}
      </div>
      {acciones}
    </div>
  )
}

/** Par etiqueta / valor de las fichas de detalle. */
export function Dato({ etiqueta, children, className }: { etiqueta: string; children: ReactNode; className?: string }) {
  return (
    <div className={cn('min-w-0', className)}>
      <div className="text-[12px] text-texto-2">{etiqueta}</div>
      <div className="font-bold text-[14px] text-negro break-words">{children}</div>
    </div>
  )
}
