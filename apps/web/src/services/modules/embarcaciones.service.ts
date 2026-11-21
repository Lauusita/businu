import { api } from '../api/client'
import type {
  EmbarcacionResponse, HojaDeVidaResponse, MotorResponse, SuspensionResponse,
} from '../../types/api'
import type { CausaSuspension } from '../../types/enums'

export const embarcacionesService = {
  listar: () => api.get<EmbarcacionResponse[]>('/embarcaciones'),
  hojaDeVida: (id: string) => api.get<HojaDeVidaResponse>(`/embarcaciones/${id}/hoja-de-vida`),
  /** Solo SUPERVISOR. */
  hojaDeVidaPdf: (id: string) => api.descargar(`/embarcaciones/${id}/hoja-de-vida/pdf`),

  motores: (embarcacionId?: string) => api.get<MotorResponse[]>('/motores', { embarcacionId }),
  /** Solo SUPERVISOR. */
  registrarHorometro: (motorId: string, lectura: number) =>
    api.patch<MotorResponse>(`/motores/${motorId}/horometro`, { lectura }),

  suspensiones: (id: string) => api.get<SuspensionResponse[]>(`/embarcaciones/${id}/suspensiones`),
  suspender: (id: string, d: { causa: CausaSuspension; detalle?: string | null; autoridad?: string | null; fechaInicio?: string | null }) =>
    api.post<SuspensionResponse>(`/embarcaciones/${id}/suspensiones`, d),
  levantarSuspension: (id: string, suspensionId: string, d: { observacion: string; fecha?: string | null }) =>
    api.post<SuspensionResponse>(`/embarcaciones/${id}/suspensiones/${suspensionId}/levantar`, d),
}
