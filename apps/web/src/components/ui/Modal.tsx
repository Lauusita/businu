import { useEffect, useId, useRef, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'
import { Boton } from './Boton'
import { cn } from './cn'

/** Modal accesible: cierra con Escape o con el fondo, devuelve el foco al cerrar. */
export function Modal({ abierto, onCerrar, titulo, subtitulo, children, pie, ancho = 'md' }: {
  abierto: boolean
  onCerrar: () => void
  titulo: string
  subtitulo?: string
  children: ReactNode
  pie?: ReactNode
  ancho?: 'sm' | 'md' | 'lg'
}) {
  const ref = useRef<HTMLDivElement>(null)
  const idTitulo = useId()

  useEffect(() => {
    if (!abierto) return
    const anterior = document.activeElement as HTMLElement | null
    const tecla = (e: KeyboardEvent) => e.key === 'Escape' && onCerrar()
    document.addEventListener('keydown', tecla)
    document.body.style.overflow = 'hidden'
    const primero = ref.current?.querySelector<HTMLElement>('input,select,textarea,button:not([data-cerrar])')
    ;(primero ?? ref.current)?.focus()
    return () => {
      document.removeEventListener('keydown', tecla)
      document.body.style.overflow = ''
      anterior?.focus()
    }
  }, [abierto, onCerrar])

  if (!abierto) return null
  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-6 bg-noche/40" onMouseDown={(e) => e.target === e.currentTarget && onCerrar()}>
      <div
        ref={ref}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        tabIndex={-1}
        className={cn(
          'bg-white w-full rounded-t-[20px] sm:rounded-[20px] shadow-flotante max-h-[92vh] flex flex-col outline-none',
          ancho === 'sm' ? 'sm:max-w-[440px]' : ancho === 'md' ? 'sm:max-w-[620px]' : 'sm:max-w-[860px]',
        )}
      >
        <div className="flex items-start gap-3 px-5 pt-5 pb-3 border-b border-borde">
          <div className="flex-1 min-w-0">
            <h2 id={idTitulo} className="m-0 font-titulo text-[18px] font-normal text-noche">{titulo}</h2>
            {subtitulo && <p className="m-0 mt-1 text-[13px] text-texto-2">{subtitulo}</p>}
          </div>
          <button type="button" data-cerrar onClick={onCerrar} aria-label="Cerrar" className="size-9 rounded-xl flex items-center justify-center text-texto-2 hover:bg-fondo cursor-pointer">
            <X className="size-5" />
          </button>
        </div>
        <div className="px-5 py-4 overflow-y-auto flex flex-col gap-4">{children}</div>
        {pie && <div className="px-5 py-4 border-t border-borde flex flex-wrap justify-end gap-2.5">{pie}</div>}
      </div>
    </div>,
    document.body,
  )
}

/** Confirmación de una acción con consecuencias (desactivar cuenta, cerrar informe…). */
export function Confirmacion({ abierto, onCerrar, onConfirmar, titulo, mensaje, textoConfirmar = 'Confirmar', peligro, cargando, error }: {
  abierto: boolean; onCerrar: () => void; onConfirmar: () => void; titulo: string; mensaje: ReactNode
  textoConfirmar?: string; peligro?: boolean; cargando?: boolean; error?: string | null
}) {
  return (
    <Modal
      abierto={abierto}
      onCerrar={onCerrar}
      titulo={titulo}
      ancho="sm"
      pie={
        <>
          <Boton variante="fantasma" onClick={onCerrar} disabled={cargando}>Cancelar</Boton>
          <Boton variante={peligro ? 'peligro' : 'institucional'} onClick={onConfirmar} cargando={cargando}>{textoConfirmar}</Boton>
        </>
      }
    >
      <div className="text-[14px] text-negro">{mensaje}</div>
      {error && <p role="alert" className="m-0 text-[13px] text-error">{error}</p>}
    </Modal>
  )
}
