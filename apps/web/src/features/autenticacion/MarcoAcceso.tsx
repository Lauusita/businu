import type { ReactNode } from 'react'
import { BookOpenCheck, FileChartColumn, UsersRound } from 'lucide-react'
import { IlustracionEmbarcacion, Logo } from '../../components/brand/Marca'

/** Plantilla 01: panel amarillo institucional a la izquierda y formulario a la derecha. */
export function MarcoAcceso({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen grid lg:grid-cols-[minmax(0,740px)_1fr] bg-white">
      <aside className="bg-dorado px-8 sm:px-16 py-10 flex flex-col gap-8 relative overflow-hidden">
        <div className="flex items-center gap-2 text-[13px] text-noche font-semibold">
          <span className="size-2 rounded-full bg-noche" aria-hidden /> Operación piloto · Montería, Córdoba
        </div>
        <div className="flex flex-col gap-5 lg:mt-20">
          <Logo tamano={88} className="!text-noche text-[56px] sm:text-[88px]" />
          <p className="m-0 font-titulo text-[20px] sm:text-[22px] text-noche max-w-[460px] leading-snug">
            Sistema de gestión y supervisión del transporte fluvial de pasajeros sobre el río Sinú.
          </p>
          <ul className="m-0 p-0 list-none flex flex-col gap-3 text-[14px] text-noche font-semibold">
            {[
              [BookOpenCheck, 'Bitácoras de viaje y hojas de vida'],
              [UsersRound, 'Registro y caracterización de usuarios'],
              [FileChartColumn, 'Informes mensuales de operación'],
            ].map(([Icono, texto]) => {
              const I = Icono as typeof BookOpenCheck
              return (
                <li key={texto as string} className="flex items-center gap-3">
                  <span className="size-8 rounded-[10px] bg-noche text-dorado flex items-center justify-center" aria-hidden><I className="size-4" /></span>
                  {texto as string}
                </li>
              )
            })}
          </ul>
        </div>
        <div className="hidden lg:block mt-auto max-w-[512px]">
          <IlustracionEmbarcacion />
        </div>
      </aside>
      <main className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-[440px] flex flex-col gap-5">
          {children}
          <p className="m-0 mt-4 text-center text-[12px] text-texto-2">© {new Date().getFullYear()} BUSINÚ · Versión piloto 1.0</p>
        </div>
      </main>
    </div>
  )
}
