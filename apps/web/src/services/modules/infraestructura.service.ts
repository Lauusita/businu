import { api } from '../api/client'
import type {
  AyudaResumen, CrearAyudaRequest, CrearRegistroInfraRequest, EstadoInfraestructura, FiltroRegistrosInfra, Pagina,
  ParametrosPagina, RegistroInfraResponse,
} from '../../types/api'
import type { MedioComunicacion } from '../../types/enums'

/** Embarcaderos y ayudas a la navegación (numeral 4.4). */
export const infraestructuraService = {
  tablero: () => api.get<EstadoInfraestructura[]>('/infraestructura/tablero'),
  ayudas: () => api.get<AyudaResumen[]>('/infraestructura/ayudas'),
  crearAyuda: (d: CrearAyudaRequest) => api.post<AyudaResumen>('/infraestructura/ayudas', d),
  desactivarAyuda: (id: string) => api.post<void>(`/infraestructura/ayudas/${id}/desactivar`),

  registros: (filtro: FiltroRegistrosInfra = {}, pagina: ParametrosPagina = {}) =>
    api.get<Pagina<RegistroInfraResponse>>('/infraestructura/registros', { ...filtro, ...pagina }),
  registro: (id: string) => api.get<RegistroInfraResponse>(`/infraestructura/registros/${id}`),
  registrar: (d: CrearRegistroInfraRequest) => api.post<RegistroInfraResponse>('/infraestructura/registros', d),
  pendientesDeComunicar: () => api.get<RegistroInfraResponse[]>('/infraestructura/registros/pendientes-comunicar'),
  comunicar: (id: string, d: { medio: MedioComunicacion; referencia?: string | null; fecha?: string | null }) =>
    api.post<RegistroInfraResponse>(`/infraestructura/registros/${id}/comunicar`, d),
  cerrar: (id: string, d: { observacion: string; fecha?: string | null }) =>
    api.post<RegistroInfraResponse>(`/infraestructura/registros/${id}/cerrar`, d),
}
