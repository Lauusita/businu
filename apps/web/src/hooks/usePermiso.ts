import { puede, type Permiso } from '../app/permisos'
import { useAuth } from '../app/providers/auth-context'

export function usePermiso(permiso: Permiso) {
  const { usuario } = useAuth()
  return puede(usuario?.rol, permiso)
}
