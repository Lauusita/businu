import { api } from '../api/client'
import type {
  ConsolidadoDemanda, FiltroUsuarios, Pagina, ParametrosPagina, UsuarioRequest, UsuarioResponse,
} from '../../types/api'
import type { AgrupacionSerie, Embarcaciones, Embarcaderos } from '../../types/enums'

/** Registro y caracterización de usuarios (pasajeros, numeral 4.2). */
export const usuariosService = {
  listar: (filtro: FiltroUsuarios = {}, pagina: ParametrosPagina = {}) =>
    api.get<Pagina<UsuarioResponse>>('/pasajeros', { ...filtro, ...pagina }),
  registrar: (d: UsuarioRequest) => api.post<UsuarioResponse>('/pasajeros', d),
  porOperacion: (operacionId: string) => api.get<UsuarioResponse[]>(`/pasajeros/operacion/${operacionId}`),
  /** Solo SUPERVISOR. Sin fechas: el mes en curso. */
  consolidado: (f: { desde?: string; hasta?: string; embarcacion?: Embarcaciones; origen?: Embarcaderos; agrupacion?: AgrupacionSerie }) =>
    api.get<ConsolidadoDemanda>('/pasajeros/consolidado', f),
}
