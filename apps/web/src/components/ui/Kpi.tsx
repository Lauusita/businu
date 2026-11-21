import type { ReactNode } from 'react'
import { Tarjeta } from './Tarjeta'
import { CajaIcono, type TonoIcono } from './CajaIcono'

/** Tarjeta de indicador (plantilla 02): ícono, insignia opcional, cifra Huninn 26 px y descripción. */
export function Kpi({ icono, tono, cifra, etiqueta, nota, insignia, cargando }: {
  icono: ReactNode; tono?: TonoIcono; cifra: ReactNode; etiqueta: string; nota?: ReactNode; insignia?: ReactNode; cargando?: boolean
}) {
  return (
    <Tarjeta>
      <div className="flex flex-col gap-2 min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <CajaIcono tono={tono}>{icono}</CajaIcono>
          <div className="flex-1" />
          {insignia && <span className="max-w-full [&>span]:whitespace-normal">{insignia}</span>}
        </div>
        {cargando ? (
          <span className="h-[34px] w-24 rounded-lg bg-fondo animate-pulse" aria-label="Cargando" />
        ) : (
          <span className="font-titulo text-[26px] text-noche leading-tight break-words">{cifra}</span>
        )}
        <p className="m-0 text-[12.5px] font-semibold leading-snug text-texto-2">{etiqueta}</p>
        {nota && <p className="m-0 text-[12px] text-texto-2">{nota}</p>}
      </div>
    </Tarjeta>
  )
}
