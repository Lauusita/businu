import ky, { HTTPError, TimeoutError } from 'ky'
import { sesion } from './sesion'

/** URL base de la API: VITE_API_URL (archivos .env.development / .env.production). */
export const API_URL = (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '') ?? 'http://localhost:3000'

/**
 * Error normalizado de la API. El backend responde ProblemDetail (RFC 9457):
 * { status, detail, errors?: { campo: mensaje } }.
 */
export class ApiError extends Error {
  readonly status: number
  readonly errores: Record<string, string>
  readonly sesionVencida: boolean

  constructor(status: number, mensaje: string, errores: Record<string, string> = {}, sesionVencida = false) {
    super(mensaje)
    this.name = 'ApiError'
    this.status = status
    this.errores = errores
    this.sesionVencida = sesionVencida
  }
}

const MENSAJES_POR_ESTADO: Record<number, string> = {
  400: 'Los datos enviados no son válidos.',
  401: 'Su sesión no es válida. Inicie sesión de nuevo.',
  403: 'No tiene permisos para esta acción.',
  404: 'No se encontró el recurso solicitado.',
  409: 'El registro cambió o ya existe. Actualice la información e intente de nuevo.',
  413: 'El archivo supera el tamaño máximo (10 MB).',
  429: 'Demasiadas solicitudes. Intente de nuevo en un momento.',
  502: 'Un servicio externo no respondió. Intente de nuevo.',
}

interface CuerpoProblema { detail?: unknown; message?: unknown; errors?: unknown }

function aApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  if (error instanceof HTTPError) {
    const status = error.response.status
    const cuerpo = (typeof error.data === 'object' && error.data !== null ? error.data : {}) as CuerpoProblema
    const errores =
      cuerpo.errors && typeof cuerpo.errors === 'object' ? (cuerpo.errors as Record<string, string>) : {}
    const detalle = typeof cuerpo.detail === 'string' ? cuerpo.detail : typeof cuerpo.message === 'string' ? cuerpo.message : ''
    const mensaje =
      detalle && detalle !== 'Datos inválidos'
        ? detalle
        : Object.keys(errores).length
          ? 'Revise los campos marcados.'
          : MENSAJES_POR_ESTADO[status] ?? `Error inesperado del servidor (${status}).`
    return new ApiError(status, mensaje, errores, status === 401 && error.response.headers.has('X-Token-Expired'))
  }
  if (error instanceof TimeoutError) return new ApiError(0, 'El servidor tardó demasiado en responder.')
  return new ApiError(0, 'No hay conexión con el servidor. Verifique su red e intente de nuevo.')
}

/** Se ejecuta cuando la API responde 401: la sesión se cierra en toda la aplicación. */
let alExpirar: ((vencida: boolean) => void) | null = null
export const registrarAlExpirar = (fn: (vencida: boolean) => void) => {
  alExpirar = fn
}

const http = ky.create({
  baseUrl: API_URL + '/',
  timeout: 30_000,
  retry: { limit: 1, methods: ['get'] },
  hooks: {
    beforeRequest: [
      ({ request }) => {
        const token = sesion.token()
        if (token) request.headers.set('Authorization', `Bearer ${token}`)
      },
    ],
  },
})

type Params = Record<string, string | number | boolean | undefined | null>

const limpiar = (params?: Params) => {
  const sp = new URLSearchParams()
  if (!params) return sp
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== '') sp.append(k, String(v))
  }
  return sp
}

async function ejecutar<T>(fn: () => Promise<T>): Promise<T> {
  try {
    return await fn()
  } catch (e) {
    const err = aApiError(e)
    if (err.status === 401 && sesion.token()) alExpirar?.(err.sesionVencida)
    throw err
  }
}

const ruta = (p: string) => p.replace(/^\//, '')

/** Respuesta sin cuerpo (204) o con cuerpo vacío → undefined. */
async function json<T>(r: Response): Promise<T> {
  const texto = await r.text()
  return (texto ? JSON.parse(texto) : undefined) as T
}

export const api = {
  get: <T>(p: string, params?: Params) =>
    ejecutar(() => http.get(ruta(p), { searchParams: limpiar(params) }).then((r) => json<T>(r))),
  post: <T>(p: string, body?: unknown, params?: Params) =>
    ejecutar(() =>
      http.post(ruta(p), { json: body ?? undefined, searchParams: limpiar(params) }).then((r) => json<T>(r)),
    ),
  put: <T>(p: string, body?: unknown) => ejecutar(() => http.put(ruta(p), { json: body }).then((r) => json<T>(r))),
  patch: <T>(p: string, body?: unknown) => ejecutar(() => http.patch(ruta(p), { json: body }).then((r) => json<T>(r))),
  delete: <T>(p: string) => ejecutar(() => http.delete(ruta(p)).then((r) => json<T>(r))),
  /** multipart/form-data: el navegador pone el boundary. */
  multipart: <T>(p: string, form: FormData, params?: Params) =>
    ejecutar(() =>
      http.post(ruta(p), { body: form, searchParams: limpiar(params), timeout: 120_000 }).then((r) => json<T>(r)),
    ),
  /** Descarga binaria (PDF, Excel, CSV): devuelve el archivo y el nombre que sugiere el servidor. */
  descargar: (p: string, params?: Params) =>
    ejecutar(async () => {
      const r = await http.get(ruta(p), { searchParams: limpiar(params), timeout: 120_000 })
      const disp = r.headers.get('Content-Disposition') ?? ''
      const nombre = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disp)?.[1]
      return { blob: await r.blob(), nombre: nombre ? decodeURIComponent(nombre) : undefined }
    }),
}
