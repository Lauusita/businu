import type { ReactNode } from 'react'
import { CircleAlert, CircleCheck, Info, TriangleAlert } from 'lucide-react'
import { cn } from './cn'

type TonoAlerta = 'info' | 'aviso' | 'error' | 'exito' | 'dorado'
const TONOS: Record<TonoAlerta, { caja: string; icono: ReactNode }> = {
  info: { caja: 'bg-info-suave text-noche', icono: <Info className="size-4 text-azul-claro" /> },
  aviso: { caja: 'bg-advertencia-suave text-noche', icono: <TriangleAlert className="size-4 text-advertencia" /> },
  error: { caja: 'bg-error-suave text-noche', icono: <CircleAlert className="size-4 text-error" /> },
  exito: { caja: 'bg-exito-suave text-noche', icono: <CircleCheck className="size-4 text-exito" /> },
  dorado: { caja: 'bg-dorado-suave text-noche', icono: <Info className="size-4 text-noche" /> },
}

/** Aviso en línea (notas del anexo técnico, advertencias y resultados). */
export function Alerta({ tono = 'info', titulo, children, accion, className }: {
  tono?: TonoAlerta; titulo?: ReactNode; children?: ReactNode; accion?: ReactNode; className?: string
}) {
  const t = TONOS[tono]
  return (
    <div role={tono === 'error' ? 'alert' : 'status'} className={cn('flex items-start gap-2.5 rounded-[12px] px-3.5 py-3 text-[13px]', t.caja, className)}>
      <span className="mt-0.5 flex-none" aria-hidden>{t.icono}</span>
      <div className="flex-1 min-w-0">
        {titulo && <div className="font-bold text-[14px]">{titulo}</div>}
        {children && <div className={cn(!!titulo && 'mt-0.5', 'text-texto-2 [&_b]:text-negro')}>{children}</div>}
      </div>
      {accion}
    </div>
  )
}
