import { lazy, Suspense, type ReactNode } from 'react'
import { createBrowserRouter } from 'react-router'
import { Cargando } from '../components/feedback/Estados'
import { LoginPage } from '../features/autenticacion/LoginPage'
import { RecuperarPage } from '../features/autenticacion/RecuperarPage'
import { RestablecerPage } from '../features/autenticacion/RestablecerPage'
import { NoEncontradaPage } from '../features/NoEncontradaPage'
import { AppLayout } from './layouts/AppLayout'
import type { Permiso } from './permisos'
import { RutaProtegida } from './RutaProtegida'

const carga = <T extends Record<string, unknown>>(f: () => Promise<T>, nombre: keyof T) =>
  lazy(() => f().then((m) => ({ default: m[nombre] as React.ComponentType })))

const PanelPage = carga(() => import('../features/dashboard/PanelPage'), 'PanelPage')
const ViajesPage = carga(() => import('../features/operacion/ViajesPage'), 'ViajesPage')
const NuevaBitacoraPage = carga(() => import('../features/operacion/NuevaBitacoraPage'), 'NuevaBitacoraPage')
const DetalleViajePage = carga(() => import('../features/operacion/DetalleViajePage'), 'DetalleViajePage')
const NovedadPage = carga(() => import('../features/operacion/NovedadPage'), 'NovedadPage')
const EmbarcacionesPage = carga(() => import('../features/embarcaciones/EmbarcacionesPage'), 'EmbarcacionesPage')
const DetalleEmbarcacionPage = carga(() => import('../features/embarcaciones/DetalleEmbarcacionPage'), 'DetalleEmbarcacionPage')
const MantenimientoPage = carga(() => import('../features/mantenimiento/MantenimientoPage'), 'MantenimientoPage')
const EmbarcaderosPage = carga(() => import('../features/infraestructura/EmbarcaderosPage'), 'EmbarcaderosPage')
const DetalleEmbarcaderoPage = carga(() => import('../features/infraestructura/DetalleEmbarcaderoPage'), 'DetalleEmbarcaderoPage')
const AyudasPage = carga(() => import('../features/infraestructura/AyudasPage'), 'AyudasPage')
const UsuariosPage = carga(() => import('../features/usuarios/UsuariosPage'), 'UsuariosPage')
const RegistroUsuariosPage = carga(() => import('../features/usuarios/RegistroUsuariosPage'), 'RegistroUsuariosPage')
const PersonalPage = carga(() => import('../features/personal/PersonalPage'), 'PersonalPage')
const ColaboradorPage = carga(() => import('../features/personal/ColaboradorPage'), 'ColaboradorPage')
const CombustiblePage = carga(() => import('../features/combustible/CombustiblePage'), 'CombustiblePage')
const RegistroCombustiblePage = carga(() => import('../features/combustible/RegistroCombustiblePage'), 'RegistroCombustiblePage')
const AtencionUsuarioPage = carga(() => import('../features/atencion-usuario/AtencionUsuarioPage'), 'AtencionUsuarioPage')
const InformesPage = carga(() => import('../features/informes/InformesPage'), 'InformesPage')

const conPermiso = (permiso: Permiso, elemento: ReactNode) => (
  <RutaProtegida permiso={permiso}>
    <Suspense fallback={<Cargando alto={320} />}>{elemento}</Suspense>
  </RutaProtegida>
)

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/recuperar-contrasena', element: <RecuperarPage /> },
  { path: '/restablecer-contrasena', element: <RestablecerPage /> },
  {
    element: <AppLayout />,
    children: [
      { index: true, element: conPermiso('panel', <PanelPage />) },
      { path: 'viajes', element: conPermiso('viajes.ver', <ViajesPage />) },
      { path: 'viajes/nuevo', element: conPermiso('viajes.registrar', <NuevaBitacoraPage />) },
      { path: 'viajes/:id', element: conPermiso('viajes.ver', <DetalleViajePage />) },
      { path: 'novedades/nueva', element: conPermiso('viajes.registrar', <NovedadPage />) },
      { path: 'embarcaciones', element: conPermiso('embarcaciones.ver', <EmbarcacionesPage />) },
      { path: 'embarcaciones/:id', element: conPermiso('embarcaciones.ver', <DetalleEmbarcacionPage />) },
      { path: 'mantenimiento', element: conPermiso('mantenimiento.registrar', <MantenimientoPage />) },
      { path: 'infraestructura', element: conPermiso('infraestructura.ver', <EmbarcaderosPage />) },
      { path: 'infraestructura/embarcaderos/:codigo', element: conPermiso('infraestructura.ver', <DetalleEmbarcaderoPage />) },
      { path: 'infraestructura/ayudas', element: conPermiso('infraestructura.ver', <AyudasPage />) },
      { path: 'usuarios', element: conPermiso('usuarios.registrar', <UsuariosPage />) },
      { path: 'usuarios/registro', element: conPermiso('usuarios.registrar', <RegistroUsuariosPage />) },
      { path: 'personal', element: conPermiso('personal', <PersonalPage />) },
      { path: 'personal/:id', element: conPermiso('personal', <ColaboradorPage />) },
      { path: 'combustible', element: conPermiso('combustible', <CombustiblePage />) },
      { path: 'combustible/nuevo', element: conPermiso('combustible', <RegistroCombustiblePage />) },
      { path: 'atencion-usuario', element: conPermiso('pqrs', <AtencionUsuarioPage />) },
      { path: 'informes', element: conPermiso('informes', <InformesPage />) },
      { path: '*', element: <NoEncontradaPage /> },
    ],
  },
])
