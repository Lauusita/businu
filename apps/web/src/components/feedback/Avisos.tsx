import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { CircleAlert, CircleCheck, Info, X } from 'lucide-react'
import { AvisosCtx, type TipoAviso } from './avisos-context'

interface Aviso { id: number; mensaje: string; tipo: TipoAviso }

/** Avisos flotantes. Solo se muestran tras la respuesta real del backend. */
export function ProveedorAvisos({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([])
  const quitar = useCallback((id: number) => setAvisos((a) => a.filter((x) => x.id !== id)), [])
  const avisar = useCallback(
    (mensaje: string, tipo: TipoAviso = 'exito') => {
      const id = Date.now() + Math.random()
      setAvisos((a) => [...a.slice(-3), { id, mensaje, tipo }])
      window.setTimeout(() => quitar(id), tipo === 'error' ? 8000 : 4500)
    },
    [quitar],
  )
  const valor = useMemo(() => ({ avisar }), [avisar])
  return (
    <AvisosCtx.Provider value={valor}>
      {children}
      <div className="fixed z-[60] bottom-4 right-4 left-4 sm:left-auto flex flex-col gap-2 sm:w-[380px]" aria-live="polite">
        {avisos.map((a) => (
          <div key={a.id} role={a.tipo === 'error' ? 'alert' : 'status'} className="flex items-start gap-2.5 bg-white border border-borde rounded-[14px] shadow-flotante px-3.5 py-3 text-[13.5px]">
            {a.tipo === 'exito' ? <CircleCheck className="size-5 text-exito flex-none" /> : a.tipo === 'error' ? <CircleAlert className="size-5 text-error flex-none" /> : <Info className="size-5 text-azul-claro flex-none" />}
            <span className="flex-1">{a.mensaje}</span>
            <button type="button" onClick={() => quitar(a.id)} aria-label="Cerrar aviso" className="text-texto-2 cursor-pointer"><X className="size-4" /></button>
          </div>
        ))}
      </div>
    </AvisosCtx.Provider>
  )
}
