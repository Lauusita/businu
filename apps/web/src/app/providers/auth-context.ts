import { createContext, useContext } from 'react'
import type { DatosSesion } from '../../services/api/sesion'
import type { LoginRequest } from '../../types/api'

export interface AuthContexto {
  usuario: DatosSesion | null
  iniciarSesion: (datos: LoginRequest) => Promise<DatosSesion>
  cerrarSesion: (motivo?: 'manual' | 'vencida') => void
  /** Mensaje para la pantalla de inicio cuando la sesión se cerró sola. */
  motivoCierre: string | null
}

export const AuthCtx = createContext<AuthContexto | null>(null)

export function useAuth() {
  const ctx = useContext(AuthCtx)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
