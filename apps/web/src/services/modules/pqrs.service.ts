import { API_URL, api } from '../api/client'
import type { GestionarPqrsRequest, Pagina, ParametrosPagina, PqrsResponse } from '../../types/api'
import type { EstadoPqrs, TipoPqrs } from '../../types/enums'

/** Atención al usuario (numeral 4.7). La gestión es solo del SUPERVISOR. */
export const pqrsService = {
  listar: (filtro: { estado?: EstadoPqrs; tipo?: TipoPqrs } = {}, pagina: ParametrosPagina = {}) =>
    api.get<Pagina<PqrsResponse>>('/pqrs/gestion', { ...filtro, ...pagina }),
  obtener: (id: string) => api.get<PqrsResponse>(`/pqrs/gestion/${id}`),
  gestionar: (id: string, d: GestionarPqrsRequest) => api.post<PqrsResponse>(`/pqrs/gestion/${id}`, d),
  /** Formulario público que sirve el backend (código QR). */
  urlFormularioPublico: `${API_URL}/pqrs`,
}
