import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { registrarAlExpirar } from '../../services/api/client'
import { sesion, type DatosSesion } from '../../services/api/sesion'
import { autenticacionService } from '../../services/modules/autenticacion.service'
import type { LoginRequest } from '../../types/api'
import { AuthCtx } from './auth-context'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [usuario, setUsuario] = useState<DatosSesion | null>(() => sesion.actual())
  const [motivoCierre, setMotivoCierre] = useState<string | null>(null)

  const cerrarSesion = useCallback(
    (motivo: 'manual' | 'vencida' = 'manual') => {
      sesion.cerrar()
      queryClient.clear()
      setUsuario(null)
      setMotivoCierre(motivo === 'vencida' ? 'Su sesión venció. Inicie sesión de nuevo para continuar.' : null)
    },
    [queryClient],
  )

  // Cualquier 401 de la API cierra la sesión (token vencido, persona retirada o sin acceso).
  useEffect(() => {
    registrarAlExpirar(() => cerrarSesion('vencida'))
  }, [cerrarSesion])

  // Cierre automático cuando vence el token, aunque no haya peticiones.
  useEffect(() => {
    if (!usuario) return
    const t = window.setTimeout(() => cerrarSesion('vencida'), Math.max(0, usuario.venceEn - Date.now()))
    return () => window.clearTimeout(t)
  }, [usuario, cerrarSesion])

  const iniciarSesion = useCallback(async (datos: LoginRequest) => {
    const r = await autenticacionService.login(datos)
    const d = sesion.guardar(r)
    setMotivoCierre(null)
    setUsuario(d)
    return d
  }, [])

  const valor = useMemo(
    () => ({ usuario, iniciarSesion, cerrarSesion, motivoCierre }),
    [usuario, iniciarSesion, cerrarSesion, motivoCierre],
  )
  return <AuthCtx.Provider value={valor}>{children}</AuthCtx.Provider>
}
