import { api } from '../api/client'
import type { RevisionResponse } from '../../types/api'
import type { TipoRegistroAuditado } from '../../types/enums'

/** Historial de cambios de un registro (R5). Solo SUPERVISOR. */
export const auditoriaService = {
  historial: (tipo: TipoRegistroAuditado, id: string) => api.get<RevisionResponse[]>(`/auditoria/${tipo}/${id}`),
}
