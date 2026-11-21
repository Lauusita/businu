import { api } from '../api/client'
import type { CrearMantenimientoRequest, MantenimientoResponse } from '../../types/api'

/** Registro de intervenciones (numeral 4.3). El historial se consulta en la hoja de vida de cada embarcación. */
export const mantenimientoService = {
  registrar: (d: CrearMantenimientoRequest) => api.post<MantenimientoResponse>('/mantenimientos', d),
  obtener: (id: string) => api.get<MantenimientoResponse>(`/mantenimientos/${id}`),
}
