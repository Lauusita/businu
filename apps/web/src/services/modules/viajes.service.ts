import { api } from '../api/client'
import type {
  ActualizarOperacionRequest, CrearOperacionRequest, FiltroOperaciones, FinalizarOperacionRequest,
  OperacionResponse, OperacionResumen, Pagina, ParadaRequest, ParametrosPagina, SuspenderOperacionRequest,
} from '../../types/api'
import type { Embarcaderos } from '../../types/enums'

/** Bitácoras de viaje (operaciones, numeral 4.1). */
export const viajesService = {
  listar: (filtro: FiltroOperaciones = {}, pagina: ParametrosPagina = {}) =>
    api.get<Pagina<OperacionResumen>>('/operaciones', { ...filtro, ...pagina }),
  abiertas: (embarcadero?: Embarcaderos) => api.get<OperacionResumen[]>('/operaciones/abiertas', { embarcadero }),
  obtener: (id: string) => api.get<OperacionResponse>(`/operaciones/${id}`),
  crear: (d: CrearOperacionRequest) => api.post<OperacionResponse>('/operaciones', d),
  actualizar: (id: string, d: ActualizarOperacionRequest) => api.patch<OperacionResponse>(`/operaciones/${id}`, d),
  agregarTripulante: (id: string, personalId: string) =>
    api.post<OperacionResponse>(`/operaciones/${id}/tripulacion/${personalId}`),
  quitarTripulante: (id: string, personalId: string) =>
    api.delete<OperacionResponse>(`/operaciones/${id}/tripulacion/${personalId}`),
  registrarParada: (id: string, d: ParadaRequest) => api.post<OperacionResponse>(`/operaciones/${id}/paradas`, d),
  finalizar: (id: string, d: FinalizarOperacionRequest) => api.post<OperacionResponse>(`/operaciones/${id}/finalizar`, d),
  suspender: (id: string, d: SuspenderOperacionRequest) => api.post<OperacionResponse>(`/operaciones/${id}/suspender`, d),
}
