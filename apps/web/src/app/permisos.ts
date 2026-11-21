import type { Rol } from '../types/enums'

/**
 * Qué módulos ve cada rol, según las anotaciones @PreAuthorize del backend.
 * Ocultar un módulo es solo una ayuda de navegación: la API sigue validando cada permiso.
 */
export type Permiso =
  | 'panel'
  | 'viajes.ver' | 'viajes.registrar'
  | 'embarcaciones.ver' | 'embarcaciones.hojaVidaPdf' | 'embarcaciones.horometro' | 'embarcaciones.suspender'
  | 'infraestructura.ver'
  | 'usuarios.registrar' | 'usuarios.caracterizacion'
  | 'personal'
  | 'combustible'
  | 'mantenimiento.registrar'
  | 'informes' | 'exportar' | 'auditoria'
  | 'pqrs'
  | 'comunicaciones'

const TODOS: Rol[] = ['SUPERVISOR', 'OPERADOR_EMBARCACION', 'TAQUILLERO']
const SUP: Rol[] = ['SUPERVISOR']
const SUP_OP: Rol[] = ['SUPERVISOR', 'OPERADOR_EMBARCACION']

const MATRIZ: Record<Permiso, Rol[]> = {
  panel: TODOS,
  'viajes.ver': TODOS,
  'viajes.registrar': SUP_OP,
  'embarcaciones.ver': TODOS,
  'embarcaciones.hojaVidaPdf': SUP,
  'embarcaciones.horometro': SUP,
  'embarcaciones.suspender': SUP_OP,
  'infraestructura.ver': TODOS,
  'usuarios.registrar': TODOS,
  'usuarios.caracterizacion': SUP,
  personal: SUP,
  combustible: SUP_OP,
  'mantenimiento.registrar': SUP_OP,
  informes: SUP,
  exportar: SUP,
  auditoria: SUP,
  pqrs: SUP,
  comunicaciones: SUP_OP,
}

export const puede = (rol: Rol | undefined, permiso: Permiso) => !!rol && MATRIZ[permiso].includes(rol)
