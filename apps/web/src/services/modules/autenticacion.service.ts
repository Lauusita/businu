import { api } from '../api/client'
import type { LoginRequest, LoginResponse, PersonalResponse } from '../../types/api'

export const autenticacionService = {
  login: (datos: LoginRequest) => api.post<LoginResponse>('/auth/login', datos),
  /** Usuario del token. Responde 401 si la persona fue retirada o perdió el acceso. */
  yo: () => api.get<PersonalResponse>('/auth/me'),
  /** Siempre responde lo mismo, exista o no la cuenta. */
  solicitarRecuperacion: (numeroIdentificacion: string) =>
    api.post<{ mensaje: string }>('/auth/request-password-change', { numeroIdentificacion }),
  restablecer: (token: string, nuevaContrasena: string) =>
    api.post<void>('/auth/new-password', { token, nuevaContrasena }),
}
