import { Fragment, useEffect, type ReactNode } from 'react'
import { Menu } from 'lucide-react'
import { Link } from 'react-router'
import { useMenuMovil } from './menu-movil'
import { MenuUsuario } from './MenuUsuario'

export interface Miga { etiqueta: string; a?: string }

/**
 * Página interna: encabezado blanco (componente Header) con migas, título Huninn 22 px y perfil;
 * debajo, la descripción y las acciones de la página y el contenido sobre el fondo #F4F6FB.
 */
export function Pagina({ titulo, migas = [], descripcion, acciones, children }: {
  titulo: string; migas?: Miga[]; descripcion?: ReactNode; acciones?: ReactNode; children: ReactNode
}) {
  const { abrir } = useMenuMovil()
  useEffect(() => {
    document.title = `${titulo} · BUSINÚ`
  }, [titulo])
  const todas: Miga[] = [{ etiqueta: 'Inicio', a: '/' }, ...migas]
  return (
    <>
      <header className="sticky top-0 z-20 flex items-center gap-4 px-4 sm:px-8 py-3.5 min-h-[76px] bg-white border-b border-borde no-imprimir">
        <button type="button" onClick={abrir} aria-label="Abrir menú" className="lg:hidden size-[42px] rounded-campo border border-borde flex items-center justify-center text-noche cursor-pointer">
          <Menu className="size-5" />
        </button>
        <div className="flex-1 min-w-0 flex flex-col gap-0.5">
          <nav aria-label="Ruta de navegación" className="text-[12px] text-texto-2 truncate">
            {todas.map((m, i) => (
              <Fragment key={i}>
                {i > 0 && ' / '}
                {m.a && i < todas.length - 1 ? <Link to={m.a} className="text-texto-2 no-underline hover:text-azul">{m.etiqueta}</Link> : m.etiqueta}
              </Fragment>
            ))}
          </nav>
          <h1 className="m-0 font-titulo text-[20px] sm:text-[22px] font-normal text-noche truncate">{titulo}</h1>
        </div>
        <MenuUsuario />
      </header>
      <main className="px-4 sm:px-8 py-6 flex flex-col gap-5 max-w-[1600px]">
        {(descripcion || acciones) && (
          <div className="flex flex-wrap items-center justify-between gap-3 no-imprimir">
            {descripcion && <p className="m-0 text-[13.5px] text-texto-2 max-w-3xl">{descripcion}</p>}
            {acciones && <div className="flex flex-wrap items-center gap-2.5">{acciones}</div>}
          </div>
        )}
        {children}
      </main>
    </>
  )
}
