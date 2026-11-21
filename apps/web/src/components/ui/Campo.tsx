import { forwardRef, useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'
import { ChevronDown, CircleAlert } from 'lucide-react'
import { cn } from './cn'

/**
 * Campos de formulario (plantilla 00): etiqueta Nunito SemiBold 13 px, contenedor blanco con borde
 * sutil y radio 15 px, mensaje de error en rojo debajo. El id se comparte con la etiqueta y el error.
 */
interface CampoProps {
  etiqueta?: ReactNode
  obligatorio?: boolean
  opcional?: boolean
  ayuda?: ReactNode
  error?: string
  children: (props: { id: string; 'aria-invalid'?: boolean; 'aria-describedby'?: string; 'aria-required'?: boolean }) => ReactNode
  className?: string
}

export function Campo({ etiqueta, obligatorio, opcional, ayuda, error, children, className }: CampoProps) {
  const id = useId()
  const idMensaje = `${id}-msg`
  return (
    <div className={cn('flex flex-col gap-1.5 min-w-0', className)}>
      {etiqueta && (
        <label htmlFor={id} className="etiqueta">
          {etiqueta}
          {obligatorio && <span className="text-error" aria-hidden> *</span>}
          {opcional && <span className="text-texto-3 font-normal"> (opcional)</span>}
        </label>
      )}
      {children({ id, 'aria-invalid': error ? true : undefined, 'aria-describedby': error || ayuda ? idMensaje : undefined, 'aria-required': obligatorio || undefined })}
      {error ? (
        <p id={idMensaje} role="alert" className="m-0 text-[12px] text-error">{error}</p>
      ) : ayuda ? (
        <p id={idMensaje} className="m-0 text-[12px] text-texto-2">{ayuda}</p>
      ) : null}
    </div>
  )
}

interface EntradaProps extends InputHTMLAttributes<HTMLInputElement> {
  icono?: ReactNode
  sufijo?: ReactNode
  bloqueado?: boolean
  conError?: boolean
}
export const Entrada = forwardRef<HTMLInputElement, EntradaProps>(function Entrada(
  { icono, sufijo, bloqueado, conError, className, ...resto }, ref,
) {
  return (
    <div className={cn('campo', className)} data-error={conError || resto['aria-invalid'] ? 'true' : undefined} data-bloqueado={bloqueado || resto.readOnly ? 'true' : undefined}>
      {icono && <span className="text-texto-2 flex [&_svg]:size-4" aria-hidden>{icono}</span>}
      <input ref={ref} readOnly={bloqueado || resto.readOnly} {...resto} />
      {sufijo && <span className="text-[13px] text-texto-2 whitespace-nowrap">{sufijo}</span>}
      {(conError || resto['aria-invalid']) && <CircleAlert className="size-4 text-error flex-none" aria-hidden />}
    </div>
  )
})

interface SelectorProps extends SelectHTMLAttributes<HTMLSelectElement> {
  opciones: { valor: string; etiqueta: string }[]
  vacio?: string
  icono?: ReactNode
}
export const Selector = forwardRef<HTMLSelectElement, SelectorProps>(function Selector(
  { opciones, vacio, icono, className, ...resto }, ref,
) {
  return (
    <div className={cn('campo relative', className)} data-error={resto['aria-invalid'] ? 'true' : undefined} data-bloqueado={resto.disabled ? 'true' : undefined}>
      {icono && <span className="text-texto-2 flex [&_svg]:size-4" aria-hidden>{icono}</span>}
      <select ref={ref} {...resto} className="pr-6">
        {vacio !== undefined && <option value="">{vacio}</option>}
        {opciones.map((o) => (
          <option key={o.valor} value={o.valor}>{o.etiqueta}</option>
        ))}
      </select>
      <ChevronDown className="size-4 text-noche pointer-events-none absolute right-3.5" aria-hidden />
    </div>
  )
})

export const AreaTexto = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(function AreaTexto(
  { className, ...resto }, ref,
) {
  return (
    <div className={cn('campo campo-area', className)} data-error={resto['aria-invalid'] ? 'true' : undefined}>
      <textarea ref={ref} {...resto} />
    </div>
  )
})

/** Casilla de verificación con texto. */
export function Casilla({ etiqueta, descripcion, className, ...resto }: InputHTMLAttributes<HTMLInputElement> & { etiqueta: ReactNode; descripcion?: ReactNode }) {
  return (
    <label className={cn('inline-flex items-start gap-2.5 cursor-pointer text-[14px]', className)}>
      <input type="checkbox" className="mt-0.5 size-[18px] accent-azul flex-none" {...resto} />
      <span className="min-w-0">
        {etiqueta}
        {descripcion && <span className="block text-[12px] text-texto-2">{descripcion}</span>}
      </span>
    </label>
  )
}

/** Interruptor (toggle) de la plantilla 00. */
export function Interruptor({ activo, onCambio, etiqueta, descripcion, disabled }: {
  activo: boolean; onCambio: (v: boolean) => void; etiqueta: ReactNode; descripcion?: ReactNode; disabled?: boolean
}) {
  return (
    <label className={cn('flex items-start gap-3 cursor-pointer', disabled && 'opacity-60 cursor-not-allowed')}>
      <button
        type="button"
        role="switch"
        aria-checked={activo}
        disabled={disabled}
        onClick={() => onCambio(!activo)}
        className={cn('relative flex-none w-[42px] h-[24px] rounded-full transition-colors mt-0.5', activo ? 'bg-azul' : 'bg-borde')}
      >
        <span className={cn('absolute top-[3px] size-[18px] rounded-full bg-white shadow transition-all', activo ? 'left-[21px]' : 'left-[3px]')} />
      </button>
      <span className="min-w-0">
        <span className="block font-bold text-[14px]">{etiqueta}</span>
        {descripcion && <span className="block text-[12px] text-texto-2">{descripcion}</span>}
      </span>
    </label>
  )
}
