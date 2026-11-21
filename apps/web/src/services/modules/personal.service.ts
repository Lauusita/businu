import { api } from '../api/client'
import type { DocumentacionPersonaResponse, DocumentoPersonalResponse, PersonalDTO, PersonalResponse } from '../../types/api'
import type { TipoDocumentoPersonal } from '../../types/enums'

export const personalService = {
  /** Solo SUPERVISOR. */
  listar: () => api.get<PersonalResponse[]>('/personal'),
  obtener: (id: string) => api.get<PersonalResponse>(`/personal/${id}`),
  crear: (datos: PersonalDTO) => api.post<PersonalResponse>('/personal', datos),
  /** Retira a la persona: contrato inactivo y sin acceso. */
  desactivar: (id: string) => api.post<void>(`/personal/desactivar-cuenta/${id}`),
  /** SUPERVISOR u OPERADOR_EMBARCACION: operadores y guías activos que no están en un viaje en curso. */
  tripulacionDisponible: () => api.get<PersonalResponse[]>('/personal/tripulacion-disponible'),

  documentacion: (id: string, historial = false) =>
    api.get<DocumentacionPersonaResponse>(`/personal/${id}/documentos`, { historial }),
  cargarDocumento: (id: string, d: { tipo: TipoDocumentoPersonal; archivo: File; periodo?: string; observaciones?: string }) => {
    const form = new FormData()
    form.append('archivo', d.archivo)
    return api.multipart<DocumentoPersonalResponse>(`/personal/${id}/documentos`, form, {
      tipo: d.tipo,
      periodo: d.periodo,
      observaciones: d.observaciones,
    })
  },
  urlDocumento: (id: string, documentoId: string) =>
    api.get<{ url: string }>(`/personal/${id}/documentos/${documentoId}/url`),
}
