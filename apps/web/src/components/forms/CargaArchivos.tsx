import { useEffect, useRef, useState, type DragEvent } from 'react'
import { CircleAlert, CircleCheck, FileText, Image as ImageIcon, Loader2, Trash2, UploadCloud } from 'lucide-react'
import { ApiError } from '../../services/api/client'
import {
  adjuntosService, EXTENSIONES_ADJUNTO, FORMATOS_ADJUNTO, MAX_BYTES_ADJUNTO, type CarpetaAdjunto,
} from '../../services/modules/adjuntos.service'
import type { Soporte } from '../../types/api'
import { bytes } from '../../utils/formato'
import { Boton } from '../ui/Boton'
import { cn } from '../ui/cn'

interface Item { clave: string; nombre: string; tamano: number; tipo: string; estado: 'subiendo' | 'listo' | 'error'; soporte?: Soporte; error?: string }

/**
 * Zona de carga de la plantilla 04. Cada archivo se sube de inmediato a POST /adjuntos y solo se
 * entrega su id cuando el servidor lo guardó (las fotos se convierten a PDF en el backend).
 */
export function CargaArchivos({ carpeta, onCambio, multiple = true, titulo = 'Arrastra archivos aquí o selecciónalos desde tu equipo', disabled }: {
  carpeta: CarpetaAdjunto
  onCambio: (soportes: Soporte[]) => void
  multiple?: boolean
  titulo?: string
  disabled?: boolean
}) {
  const [items, setItems] = useState<Item[]>([])
  const [arrastrando, setArrastrando] = useState(false)
  const input = useRef<HTMLInputElement>(null)

  // Se avisa al formulario cuando cambian los archivos (nunca dentro de un setState)
  const alCambiar = useRef(onCambio)
  useEffect(() => {
    alCambiar.current = onCambio
  }, [onCambio])
  useEffect(() => {
    alCambiar.current(items.filter((i) => i.estado === 'listo' && i.soporte).map((i) => i.soporte!))
  }, [items])

  const actualizar = (clave: string, cambio: Partial<Item>) =>
    setItems((prev) => prev.map((i) => (i.clave === clave ? { ...i, ...cambio } : i)))

  const agregar = (archivos: FileList | null) => {
    if (!archivos?.length) return
    const seleccion = multiple ? [...archivos] : [archivos[0]!]
    const nuevos: Item[] = seleccion.map((f) => {
      const error = !FORMATOS_ADJUNTO.includes(f.type)
        ? 'Formato no permitido: use PDF, JPG o PNG.'
        : f.size > MAX_BYTES_ADJUNTO
          ? 'El archivo supera 10 MB.'
          : undefined
      return { clave: `${f.name}-${f.size}-${Math.random()}`, nombre: f.name, tamano: f.size, tipo: f.type, estado: error ? 'error' : 'subiendo', error }
    })
    setItems((prev) => [...(multiple ? prev : []), ...nuevos])
    nuevos.forEach((n, i) => {
      if (n.estado !== 'subiendo') return
      adjuntosService
        .subir(carpeta, seleccion[i]!)
        .then((s) => actualizar(n.clave, { estado: 'listo', soporte: s }))
        .catch((e: unknown) => actualizar(n.clave, { estado: 'error', error: e instanceof ApiError ? e.message : 'No se pudo subir.' }))
    })
    if (input.current) input.current.value = ''
  }

  const quitar = (clave: string) => setItems((prev) => prev.filter((i) => i.clave !== clave))

  const soltar = (e: DragEvent) => {
    e.preventDefault()
    setArrastrando(false)
    if (!disabled) agregar(e.dataTransfer.files)
  }

  return (
    <div className="grid gap-3 md:grid-cols-2">
      <div
        onDragOver={(e) => { e.preventDefault(); setArrastrando(true) }}
        onDragLeave={() => setArrastrando(false)}
        onDrop={soltar}
        className={cn(
          'flex flex-col items-center justify-center text-center gap-2 p-5 rounded-[14px] border-2 border-dashed min-h-[150px] transition-colors',
          arrastrando ? 'border-azul-claro bg-azul-suave' : 'border-[#C9D3E6] bg-fondo/60',
        )}
      >
        <span className="size-9 rounded-xl bg-white flex items-center justify-center text-azul shadow-tarjeta" aria-hidden><UploadCloud className="size-5" /></span>
        <span className="font-bold text-[13.5px]">{titulo}</span>
        <span className="text-[12px] text-texto-2">PDF, JPG o PNG · máx. 10 MB por archivo</span>
        <Boton variante="secundario" pequeno onClick={() => input.current?.click()} disabled={disabled}>Seleccionar archivos</Boton>
        <input ref={input} type="file" hidden multiple={multiple} accept={EXTENSIONES_ADJUNTO} onChange={(e) => agregar(e.target.files)} />
      </div>
      <ul className="m-0 p-0 list-none flex flex-col gap-2">
        {items.length === 0 && <li className="text-[12.5px] text-texto-2 self-center my-auto">Aún no hay archivos adjuntos.</li>}
        {items.map((i) => (
          <li key={i.clave} className="flex items-center gap-3 border border-borde rounded-[12px] px-3 py-2.5 bg-white">
            <span className={cn('size-8 rounded-lg flex items-center justify-center flex-none', i.tipo.startsWith('image') ? 'bg-dorado-suave text-noche' : 'bg-azul-suave text-azul')} aria-hidden>
              {i.tipo.startsWith('image') ? <ImageIcon className="size-4" /> : <FileText className="size-4" />}
            </span>
            <span className="flex-1 min-w-0">
              <span className="block text-[13px] font-bold truncate">{i.nombre}</span>
              <span className={cn('block text-[12px]', i.estado === 'error' ? 'text-error' : 'text-texto-2')}>
                {i.estado === 'error' ? i.error : i.estado === 'subiendo' ? 'Subiendo…' : `${bytes(i.soporte?.tamanoBytes ?? i.tamano)} · guardado como PDF`}
              </span>
            </span>
            {i.estado === 'subiendo' && <Loader2 className="size-4 animate-spin text-azul-claro" aria-label="Subiendo" />}
            {i.estado === 'listo' && <CircleCheck className="size-4 text-exito" aria-label="Guardado" />}
            {i.estado === 'error' && <CircleAlert className="size-4 text-error" aria-hidden />}
            <button type="button" onClick={() => quitar(i.clave)} aria-label={`Quitar ${i.nombre}`} className="text-texto-2 hover:text-error cursor-pointer"><Trash2 className="size-4" /></button>
          </li>
        ))}
      </ul>
    </div>
  )
}

