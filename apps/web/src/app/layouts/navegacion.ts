import {
  Anchor, ChartColumn, Fuel, Headset, LayoutDashboard, Route, Ship, UsersRound, Wrench, IdCard,
  type LucideIcon,
} from 'lucide-react'
import type { Permiso } from '../permisos'

export interface ItemMenu { a: string; etiqueta: string; icono: LucideIcon; permiso: Permiso; activoEn?: string[] }
export interface GrupoMenu { titulo: string; items: ItemMenu[] }

/**
 * Menú de la plantilla (componente Sidebar). Se omiten «Configuración», «Gestión de turnos» y
 * «Encuestas» porque el backend no tiene esas funciones (ver docs/FRONTEND_INTEGRACION.md).
 */
export const MENU: GrupoMenu[] = [
  {
    titulo: 'Gestión operativa',
    items: [
      { a: '/', etiqueta: 'Panel principal', icono: LayoutDashboard, permiso: 'panel' },
      { a: '/viajes', etiqueta: 'Operación', icono: Route, permiso: 'viajes.ver', activoEn: ['/viajes', '/novedades'] },
      { a: '/embarcaciones', etiqueta: 'Embarcaciones', icono: Ship, permiso: 'embarcaciones.ver' },
      { a: '/infraestructura', etiqueta: 'Embarcaderos e infraestructura', icono: Anchor, permiso: 'infraestructura.ver' },
      { a: '/usuarios', etiqueta: 'Usuarios', icono: UsersRound, permiso: 'usuarios.registrar' },
      { a: '/personal', etiqueta: 'Personal', icono: IdCard, permiso: 'personal' },
      { a: '/combustible', etiqueta: 'Combustible', icono: Fuel, permiso: 'combustible' },
      { a: '/mantenimiento', etiqueta: 'Mantenimiento', icono: Wrench, permiso: 'mantenimiento.registrar' },
    ],
  },
  {
    titulo: 'Seguimiento',
    items: [
      { a: '/informes', etiqueta: 'Informes y reportes', icono: ChartColumn, permiso: 'informes' },
      { a: '/atencion-usuario', etiqueta: 'Atención al usuario', icono: Headset, permiso: 'pqrs' },
    ],
  },
]
