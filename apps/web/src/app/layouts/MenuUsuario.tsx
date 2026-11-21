import { useEffect, useRef, useState } from 'react'
import { ChevronDown, LogOut } from 'lucide-react'
import { Avatar } from '../../components/ui/Avatar'
import { ROL } from '../../types/enums'
import { useAuth } from '../providers/auth-context'

/** Perfil del encabezado: avatar, nombre, rol y cierre de sesión. */
export function MenuUsuario() {
  const { usuario, cerrarSesion } = useAuth()
  const [abierto, setAbierto] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!abierto) return
    const fuera = (e: MouseEvent) => !ref.current?.contains(e.target as Node) && setAbierto(false)
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && setAbierto(false)
    document.addEventListener('mousedown', fuera)
    document.addEventListener('keydown', esc)
    return () => {
      document.removeEventListener('mousedown', fuera)
      document.removeEventListener('keydown', esc)
    }
  }, [abierto])

  if (!usuario) return null
  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        aria-haspopup="menu"
        aria-expanded={abierto}
        onClick={() => setAbierto((v) => !v)}
        className="flex items-center gap-2.5 bg-transparent border-0 p-0 cursor-pointer"
      >
        <Avatar nombre={usuario.nombre} />
        <span className="hidden sm:flex flex-col items-start gap-px text-left">
          <span className="text-[14px] font-bold text-negro leading-tight">{usuario.nombre}</span>
          <span className="text-[12px] text-texto-2">{ROL[usuario.rol]}</span>
        </span>
        <ChevronDown className="size-4 text-texto-2" aria-hidden />
      </button>
      {abierto && (
        <div role="menu" className="absolute right-0 top-[calc(100%+8px)] z-40 w-60 bg-white border border-borde rounded-[14px] shadow-flotante p-1.5">
          <div className="px-3 py-2 border-b border-borde mb-1 sm:hidden">
            <div className="font-bold text-[14px]">{usuario.nombre}</div>
            <div className="text-[12px] text-texto-2">{ROL[usuario.rol]}</div>
          </div>
          <button
            type="button"
            role="menuitem"
            onClick={() => cerrarSesion('manual')}
            className="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-[10px] text-[14px] text-error hover:bg-error-suave cursor-pointer font-titulo"
          >
            <LogOut className="size-4" aria-hidden /> Cerrar sesión
          </button>
        </div>
      )}
    </div>
  )
}
