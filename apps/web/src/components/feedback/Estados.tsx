import type { ReactNode } from 'react'
import { Inbox, Loader2, RefreshCw, ShieldOff } from 'lucide-react'
import { ApiError } from '../../services/api/client'
import { Boton } from '../ui/Boton'

export function Cargando({ texto = 'Cargando…', alto = 160 }: { texto?: string; alto?: number }) {
  return (
    <div className="flex items-center justify-center gap-2 text-texto-2 text-[13px]" style={{ minHeight: alto }} role="status">
      <Loader2 className="size-5 animate-spin text-azul-claro" aria-hidden />
      {texto}
    </div>
  )
}

export function Vacio({ titulo = 'Sin registros', children, icono, accion }: { titulo?: string; children?: ReactNode; icono?: ReactNode; accion?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center text-center gap-2 py-10 px-4">
      <span className="size-11 rounded-2xl bg-fondo text-texto-2 flex items-center justify-center" aria-hidden>{icono ?? <Inbox className="size-5" />}</span>
      <div className="font-titulo text-[15px] text-noche">{titulo}</div>
      {children && <p className="m-0 text-[13px] text-texto-2 max-w-md">{children}</p>}
      {accion}
    </div>
  )
}

/** Error de carga con opción de reintentar. No muestra datos ficticios en su lugar. */
export function ErrorCarga({ error, onReintentar }: { error: unknown; onReintentar?: () => void }) {
  const api = error instanceof ApiError ? error : null
  if (api?.status === 403) {
    return (
      <Vacio titulo="Sin permisos" icono={<ShieldOff className="size-5" />}>
        Su rol no tiene acceso a esta información.
      </Vacio>
    )
  }
  return (
    <div role="alert" className="flex flex-col items-center text-center gap-2 py-8 px-4">
      <div className="font-titulo text-[15px] text-error">No se pudo cargar la información</div>
      <p className="m-0 text-[13px] text-texto-2 max-w-md">{api?.message ?? 'Ocurrió un error inesperado.'}</p>
      {onReintentar && (
        <Boton variante="secundario" pequeno icono={<RefreshCw className="size-3.5" />} onClick={onReintentar}>
          Reintentar
        </Boton>
      )}
    </div>
  )
}

/** Mensaje de error de un formulario (respuesta del backend). */
export function ErrorFormulario({ error }: { error: unknown }) {
  if (!error) return null
  const mensaje = error instanceof ApiError ? error.message : 'No se pudo completar la operación.'
  const errores = error instanceof ApiError ? Object.entries(error.errores) : []
  return (
    <div role="alert" className="rounded-[12px] bg-error-suave px-3.5 py-3 text-[13px] text-noche">
      <div className="font-bold text-error">{mensaje}</div>
      {errores.length > 0 && (
        <ul className="m-0 mt-1 pl-4 text-texto-2">
          {errores.map(([campo, msg]) => (
            <li key={campo}>
              <b className="text-negro">{campo}</b>: {msg}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
