import { useQuery } from '@tanstack/react-query'
import { ArrowRight, Megaphone, TriangleAlert } from 'lucide-react'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { Boton } from '../../components/ui/Boton'
import { Insignia } from '../../components/ui/Insignia'
import { SeveridadInsignia } from '../../components/ui/InsigniasEstado'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import { COMPONENTE, EMBARCADERO, TIPO_REGISTRO_INFRA } from '../../types/enums'
import { fechaHora } from '../../utils/formato'
import { cn } from '../../components/ui/cn'

/** «Alertas y novedades»: registros de infraestructura abiertos y los pendientes de comunicar (§4.4). */
export function AlertasPanel() {
  const abiertos = useQuery({
    queryKey: ['infraestructura', 'registros', { estado: 'ABIERTO', size: 4 }],
    queryFn: () => infraestructuraService.registros({ estado: 'ABIERTO' }, { size: 4 }),
  })
  const total = abiertos.data?.page.totalElements ?? 0
  return (
    <Tarjeta>
      <EncabezadoTarjeta
        titulo="Alertas y novedades"
        subtitulo="Registros de infraestructura abiertos que requieren seguimiento"
        acciones={total > 0 ? <Insignia tono="err">{total} abiertas</Insignia> : undefined}
      />
      {abiertos.isLoading ? <Cargando /> : abiertos.error ? <ErrorCarga error={abiertos.error} onReintentar={() => abiertos.refetch()} /> :
        total === 0 ? <Vacio titulo="Sin novedades abiertas">No hay registros de infraestructura pendientes.</Vacio> : (
          <ul className="m-0 p-0 list-none flex flex-col gap-2.5">
            {abiertos.data!.content.map((r) => (
              <li key={r.id} className={cn('rounded-[12px] px-3.5 py-3', r.severidad === 'ALTA' || r.severidad === 'CRITICA' ? 'bg-error-suave' : 'bg-advertencia-suave')}>
                <div className="flex items-start gap-2.5">
                  <TriangleAlert className={cn('size-4 mt-0.5 flex-none', r.severidad === 'ALTA' || r.severidad === 'CRITICA' ? 'text-error' : 'text-advertencia')} aria-hidden />
                  <div className="flex-1 min-w-0">
                    <div className="font-bold text-[13.5px]">
                      {TIPO_REGISTRO_INFRA[r.tipo]} · {r.embarcadero ? EMBARCADERO[r.embarcadero] : r.ayuda?.codigo}
                    </div>
                    <div className="text-[12.5px] text-texto-2 line-clamp-2">{COMPONENTE[r.componente].etiqueta}: {r.descripcion}</div>
                    <div className="flex flex-wrap items-center gap-2 mt-1.5 text-[11.5px] font-bold text-texto-2">
                      {fechaHora(r.fechaHora)} <SeveridadInsignia severidad={r.severidad} />
                      {r.pendienteDeComunicar && <span className="inline-flex items-center gap-1 text-error"><Megaphone className="size-3.5" /> Sin comunicar al supervisor</span>}
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      <Boton a="/infraestructura" variante="secundario" className="w-full">Ver infraestructura <ArrowRight className="size-4" /></Boton>
    </Tarjeta>
  )
}
