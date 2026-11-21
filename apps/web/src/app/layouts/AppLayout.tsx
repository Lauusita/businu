import { useEffect, useMemo, useState } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../providers/auth-context'
import { MenuLateral } from './MenuLateral'
import { MenuMovilCtx } from './menu-movil'

/** Estructura de las plantillas 02–22: menú lateral fijo en escritorio y cajón en tableta y móvil. */
export function AppLayout() {
  const { usuario } = useAuth()
  const location = useLocation()
  const [movilAbierto, setMovilAbierto] = useState(false)

  useEffect(() => {
    if (!movilAbierto) return
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && setMovilAbierto(false)
    document.addEventListener('keydown', esc)
    return () => document.removeEventListener('keydown', esc)
  }, [movilAbierto])

  const ctx = useMemo(() => ({ abrir: () => setMovilAbierto(true) }), [])

  if (!usuario) return <Navigate to="/login" replace state={{ desde: location.pathname + location.search }} />

  return (
    <MenuMovilCtx.Provider value={ctx}>
      <div className="min-h-screen lg:pl-[264px]">
        <aside className="hidden lg:block fixed inset-y-0 left-0 z-30 no-imprimir">
          <MenuLateral />
        </aside>
        {movilAbierto && (
          <div className="lg:hidden fixed inset-0 z-50 flex no-imprimir">
            <div className="shadow-flotante"><MenuLateral onNavegar={() => setMovilAbierto(false)} /></div>
            <button type="button" aria-label="Cerrar menú" className="flex-1 bg-noche/40" onClick={() => setMovilAbierto(false)} />
          </div>
        )}
        <Outlet />
      </div>
    </MenuMovilCtx.Provider>
  )
}
