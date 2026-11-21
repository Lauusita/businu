import { useQuery } from '@tanstack/react-query'
import { Anchor, CircleCheck, ClipboardList, Plus, TriangleAlert } from 'lucide-react'
import { useParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { CajaIcono } from '../../components/ui/CajaIcono'
import { EstadoActivoInsignia } from '../../components/ui/InsigniasEstado'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useTablero } from '../../hooks/consultas'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import type { RegistroInfraResponse } from '../../types/api'
import { COMPONENTE, EMBARCADERO, EMBARCADEROS, type ComponenteInfraestructura, type Embarcaderos } from '../../types/enums'
import { fecha, fechaHora } from '../../utils/formato'
import { NoEncontradaPage } from '../NoEncontradaPage'
import { FormularioRegistroInfra } from './FormularioRegistroInfra'
import { TablaRegistrosInfra } from './RegistrosInfra'

/** Plantilla 10 · Detalle de un embarcadero: estado por componente, historial y registro de revisiones. */
export function DetalleEmbarcaderoPage() {
  const { codigo = '' } = useParams()
  const e = codigo as Embarcaderos
  const tablero = useTablero()
  // Últimos registros para conocer el estado más reciente de cada componente
  const recientes = useQuery({
    queryKey: ['infraestructura', 'registros', { embarcadero: e }, 0, 50],
    queryFn: () => infraestructuraService.registros({ embarcadero: e }, { size: 50 }),
    enabled: EMBARCADEROS.includes(e),
  })
  if (!EMBARCADEROS.includes(e)) return <NoEncontradaPage />

  const t = tablero.data?.find((x) => x.tipo === 'EMBARCADERO' && x.identificador === e)
  const porComponente = new Map<ComponenteInfraestructura, RegistroInfraResponse>()
  recientes.data?.content.forEach((r) => !porComponente.has(r.componente) && porComponente.set(r.componente, r))
  const abiertos = recientes.data?.content.filter((r) => r.estado !== 'CERRADO') ?? []

  return (
    <Pagina
      titulo={EMBARCADERO[e]}
      migas={[{ etiqueta: 'Embarcaderos e infraestructura', a: '/infraestructura' }, { etiqueta: EMBARCADERO[e] }]}
      descripcion="Estado, revisiones, deterioros y evidencias del embarcadero."
    >
      <Tarjeta className="!flex-row flex-wrap items-center gap-5">
        <CajaIcono tono="dorado" className="!size-14 [&_svg]:!size-7"><Anchor /></CajaIcono>
        <div className="flex-1 min-w-[220px]">
          <div className="flex flex-wrap items-center gap-2.5">
            <h2 className="m-0 font-titulo text-[28px] font-normal text-noche">{EMBARCADERO[e]}</h2>
            {t && <EstadoActivoInsignia estado={t.estadoActual} />}
          </div>
          <p className="m-0 text-[13px] text-texto-2">Embarcadero del recorrido piloto</p>
        </div>
        <div className="grid grid-cols-2 gap-6">
          <Dato etiqueta="Última revisión">{t?.ultimaRevision ? fecha(t.ultimaRevision) : '—'}</Dato>
          <Dato etiqueta="Registros abiertos"><span className={t?.registrosAbiertos ? 'text-error' : ''}>{t?.registrosAbiertos ?? 0}</span></Dato>
        </div>
      </Tarjeta>

      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Componentes del embarcadero" subtitulo="Condición según el último registro de cada componente" icono={<ClipboardList />} />
            {recientes.isLoading ? <Cargando /> : recientes.error ? <ErrorCarga error={recientes.error} /> : porComponente.size === 0 ? (
              <Vacio titulo="Sin revisiones registradas">Registre la primera revisión del embarcadero.</Vacio>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {[...porComponente.values()].map((r) => (
                  <div key={r.componente} className="rounded-[14px] border border-borde p-3.5 flex flex-col gap-2">
                    <div className="flex items-start justify-between gap-2">
                      <b className="text-[14px]">{COMPONENTE[r.componente].etiqueta}</b>
                      <EstadoActivoInsignia estado={r.estadoObservado} />
                    </div>
                    <p className="m-0 text-[12.5px] text-texto-2 line-clamp-3">{r.descripcion}</p>
                    <span className="text-[11.5px] text-texto-3">{fechaHora(r.fechaHora)}</span>
                  </div>
                ))}
              </div>
            )}
          </Tarjeta>
          <Tarjeta>
            <EncabezadoTarjeta titulo="Deterioros y alertas abiertas" icono={<TriangleAlert />} tono="advertencia" />
            {recientes.isLoading ? <Cargando alto={60} /> : !abiertos.length ? (
              <p className="m-0 flex items-center gap-2 text-[13px] text-exito"><CircleCheck className="size-4" /> No hay registros abiertos.</p>
            ) : (
              <ul className="m-0 p-0 list-none flex flex-col gap-2">
                {abiertos.map((r) => (
                  <li key={r.id} className="rounded-[12px] bg-advertencia-suave px-3.5 py-2.5 text-[13px]">
                    <b>{COMPONENTE[r.componente].etiqueta}</b> · {r.descripcion}
                    <div className="text-[12px] text-texto-2">{fechaHora(r.fechaHora)}{r.pendienteDeComunicar && ' · sin comunicar al supervisor'}</div>
                  </li>
                ))}
              </ul>
            )}
          </Tarjeta>
          <Tarjeta sinRelleno>
            <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Historial de revisiones" icono={<ClipboardList />} /></div>
            <TablaRegistrosInfra filtro={{ embarcadero: e }} />
          </Tarjeta>
        </div>
        <Tarjeta>
          <EncabezadoTarjeta titulo="Registrar revisión" icono={<Plus />} />
          <FormularioRegistroInfra embarcaderoFijo={e} compacto />
        </Tarjeta>
      </div>
    </Pagina>
  )
}
