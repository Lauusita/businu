import { Clock } from 'lucide-react'
import { NavLink, useLocation } from 'react-router'
import { Logo, Ondas } from '../../components/brand/Marca'
import { cn } from '../../components/ui/cn'
import { puede } from '../permisos'
import { useAuth } from '../providers/auth-context'
import { MENU } from './navegacion'

/** Componente Sidebar (264 px, azul noche, acento dorado en el ítem activo). */
export function MenuLateral({ onNavegar }: { onNavegar?: () => void }) {
  const { usuario } = useAuth()
  const { pathname } = useLocation()
  const activo = (a: string, extra?: string[]) =>
    a === '/' ? pathname === '/' : [a, ...(extra ?? [])].some((r) => pathname === r || pathname.startsWith(r + '/'))

  return (
    <nav aria-label="Navegación principal" className="h-full w-[264px] bg-noche flex flex-col gap-1 pt-7 pb-5 px-4 overflow-y-auto">
      <div className="px-2.5 pb-[18px] flex flex-col gap-0.5">
        <Logo tamano={40} />
        <span className="text-[12px] text-white/60">Transporte fluvial · Montería</span>
      </div>
      <div className="h-px bg-white/12" />
      {MENU.map((g) => {
        const items = g.items.filter((i) => puede(usuario?.rol, i.permiso))
        if (!items.length) return null
        return (
          <div key={g.titulo} className="flex flex-col gap-1">
            <div className="px-3 pt-4 pb-1.5 text-[11px] font-extrabold tracking-[.1em] uppercase text-white/50">{g.titulo}</div>
            {items.map((i) => {
              const esActivo = activo(i.a, i.activoEn)
              const Icono = i.icono
              return (
                <NavLink
                  key={i.a}
                  to={i.a}
                  onClick={onNavegar}
                  aria-current={esActivo ? 'page' : undefined}
                  className={cn(
                    'relative flex items-center gap-3 px-3 py-[11px] rounded-[12px] no-underline transition-colors',
                    esActivo ? 'bg-white/10' : 'hover:bg-white/5',
                  )}
                >
                  {esActivo && <span className="absolute left-0 top-1/2 -mt-[11px] w-1 h-[22px] rounded-sm bg-dorado" aria-hidden />}
                  <Icono className={cn('size-5 flex-none', esActivo ? 'text-dorado' : 'text-sidebar-texto')} aria-hidden />
                  <span className="font-titulo text-[14px] text-white leading-tight">{i.etiqueta}</span>
                </NavLink>
              )
            })}
          </div>
        )
      })}
      <div className="flex-1" />
      {/* Horario configurado en el backend (operacion.dias: lunes a domingo, 05:00–21:00) */}
      <div className="rounded-[12px] bg-white/8 p-3 mt-4">
        <div className="flex items-center gap-1.5 text-dorado text-[12px] font-bold">
          <Clock className="size-3.5" aria-hidden /> Horario de operación
        </div>
        <p className="m-0 mt-1 text-[12px] text-white/85">Lunes a domingo · 5:00 a. m. – 9:00 p. m.</p>
      </div>
      <Ondas className="mt-3 h-7" />
    </nav>
  )
}
