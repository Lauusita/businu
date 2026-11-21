import { api } from '../api/client'
import type { AbastecimientoRequest, AbastecimientoResponse, ConsumoPeriodo, Pagina, ParametrosPagina } from '../../types/api'

/** Abastecimientos de combustible (numeral 4.5). SUPERVISOR u OPERADOR_EMBARCACION. */
export const combustibleService = {
  registrar: (d: AbastecimientoRequest) => api.post<AbastecimientoResponse>('/abastecimiento', d),
  obtener: (id: string) => api.get<AbastecimientoResponse>(`/abastecimiento/${id}`),
  listar: (embarcacionId: string, pagina: ParametrosPagina = {}) =>
    api.get<Pagina<AbastecimientoResponse>>('/abastecimiento', { embarcacionId, ...pagina }),
  /** Consumo entre tanques llenos (R37). */
  consumo: (embarcacionId: string) => api.get<ConsumoPeriodo[]>('/abastecimiento/consumo', { embarcacionId }),
  urlSoporte: (id: string) => api.get<{ url: string }>(`/abastecimiento/${id}/soporte`),
}
