import { api } from '../api/client'
import type { ComunicacionRequest, ComunicacionResponse } from '../../types/api'
import type { TipoReferenciaFalla } from '../../types/enums'

/** Constancia de comunicación de fallas al supervisor del contrato por correo (§7.1.12, R50). */
export const comunicacionesService = {
  registrar: (d: ComunicacionRequest) => api.post<ComunicacionResponse>('/comunicaciones', d),
  reintentar: (id: string) => api.post<ComunicacionResponse>(`/comunicaciones/${id}/reintentar`),
  listar: (referenciaTipo: TipoReferenciaFalla, referenciaId: string) =>
    api.get<ComunicacionResponse[]>('/comunicaciones', { referenciaTipo, referenciaId }),
}
