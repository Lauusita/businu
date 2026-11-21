import { api } from '../api/client'
import type { Soporte } from '../../types/api'

export type CarpetaAdjunto = 'combustible' | 'mantenimiento' | 'personal' | 'general' | 'infraestructura'

/** Formatos y tamaño que acepta el backend (AdjuntoServicio: PDF, JPG o PNG; las fotos se convierten a PDF). */
export const FORMATOS_ADJUNTO = ['application/pdf', 'image/jpeg', 'image/png']
export const EXTENSIONES_ADJUNTO = '.pdf,.jpg,.jpeg,.png'
export const MAX_BYTES_ADJUNTO = 10 * 1024 * 1024

export const adjuntosService = {
  subir: (carpeta: CarpetaAdjunto, archivo: File) => {
    const form = new FormData()
    form.append('archivo', archivo)
    return api.multipart<Soporte>('/adjuntos', form, { carpeta })
  },
}
