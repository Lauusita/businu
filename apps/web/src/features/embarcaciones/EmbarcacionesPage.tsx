import { useQueries, useQuery } from '@tanstack/react-query'
import { ArrowRight, CircleCheck, OctagonAlert, Wrench } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { IlustracionEmbarcacion } from '../../components/brand/Marca'
import { GraficoBarras } from '../../components/charts/GraficoBarras'
import { Leyenda } from '../../components/charts/Leyenda'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { EstadoEmbarcacionInsignia } from '../../components/ui/InsigniasEstado'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useEmbarcaciones, useHojaDeVida } from '../../hooks/consultas'
import { usePermiso } from '../../hooks/usePermiso'
import { embarcacionesService } from '../../services/modules/embarcaciones.service'
import { usuariosService } from '../../services/modules/usuarios.service'
import type { EmbarcacionResponse } from '../../types/api'
import { EMBARCACION, ESTADO_EMBARCACION, TIPO_MANTENIMIENTO, type Embarcaciones } from '../../types/enums'
import { decimal, diaMes, entero, fecha, fechaLocal, hoyISO, inicioMes, mesLargo, mesActual, rangoDia } from '../../utils/formato'
import { useAbastecimientosDesde } from '../combustible/consultas'

const COLOR: Record<Embarcaciones, string> = { BETANCI: '#0A6EDC', YAPEL: '#EDC92A' }

/** Plantilla 06 · Las embarcaciones de la operación piloto, su estado y alertas. */
export function EmbarcacionesPage() {
  const emb = useEmbarcaciones()
  return (
    <Pagina titulo="Embarcaciones" migas={[{ etiqueta: 'Embarcaciones' }]} descripcion="Las embarcaciones de la operación piloto, con su estado y alertas.">
      {emb.isLoading ? <Cargando alto={320} /> : emb.error ? <Tarjeta><ErrorCarga error={emb.error} onReintentar={() => emb.refetch()} /></Tarjeta> : !emb.data?.length ? (
        <Tarjeta><Vacio titulo="No hay embarcaciones registradas" /></Tarjeta>
      ) : (
        <>
          <div className="grid gap-5 lg:grid-cols-2">
            {emb.data.map((e) => <TarjetaEmbarcacion key={e.id} e={e} />)}
          </div>
          <ComparativoMes embarcaciones={emb.data} />
        </>
      )}
    </Pagina>
  )
}

function TarjetaEmbarcacion({ e }: { e: EmbarcacionResponse }) {
  const hv = useHojaDeVida(e.id)
  const suspensiones = useQuery({ queryKey: ['suspensiones', e.id], queryFn: () => embarcacionesService.suspensiones(e.id) })
  const puedeMantener = usePermiso('mantenimiento.registrar')
  const vigente = suspensiones.data?.find((s) => s.vigente)
  const ultima = hv.data?.intervenciones[0]
  const proxima = hv.data?.intervenciones.filter((i) => i.proximaIntervencion && i.proximaIntervencion > new Date().toISOString())
    .sort((a, b) => a.proximaIntervencion!.localeCompare(b.proximaIntervencion!))[0]
  const recomendaciones = ultima?.recomendaciones ?? []
  return (
    <Tarjeta>
      <div className="rounded-[14px] bg-azul-suave px-6 pt-5 overflow-hidden"><IlustracionEmbarcacion className="max-h-[150px]" /></div>
      <div className="flex items-start gap-3">
        <div className="flex-1 min-w-0">
          <h2 className="m-0 font-titulo text-[24px] font-normal text-noche">{EMBARCACION[e.nombre]}</h2>
          <p className="m-0 text-[12.5px] text-texto-2">Embarcación de pasajeros · patente {e.patente}</p>
        </div>
        <EstadoEmbarcacionInsignia estado={e.estado} />
      </div>
      <div className="grid grid-cols-2 gap-x-5 gap-y-3 rounded-[14px] bg-fondo p-4">
        <Dato etiqueta="Capacidad">{e.capacidadPasajeros} pasajeros</Dato>
        <Dato etiqueta="Estado operativo"><span className={e.estado === 'OPERATIVA' ? 'text-exito' : 'text-advertencia'}>{ESTADO_EMBARCACION[e.estado]}</span></Dato>
        <Dato etiqueta="Motores">{e.motores.length ? e.motores.map((m) => `${m.marca} ${m.potenciaHp} HP`).join(' · ') : 'Sin registrar'}</Dato>
        <Dato etiqueta="Intervenciones">{hv.data ? entero(hv.data.totalIntervenciones) : '…'}</Dato>
        <Dato etiqueta="Último mantenimiento">{ultima ? `${fecha(ultima.fechaIntervencion)} · ${TIPO_MANTENIMIENTO[ultima.tipo]}` : hv.data ? 'Sin registros' : '…'}</Dato>
        <Dato etiqueta="Próximo mantenimiento">{proxima ? `${fecha(proxima.proximaIntervencion)} · ${TIPO_MANTENIMIENTO[proxima.tipo]}` : hv.data ? 'Sin programar' : '…'}</Dato>
      </div>
      <div className="flex flex-col gap-2">
        <span className="font-bold text-[13px]">Alertas activas</span>
        {vigente && (
          <Alerta tono="error" titulo={`Suspendida · ${vigente.causaDescripcion}`}>
            Desde {fecha(vigente.fechaInicio)}{vigente.detalle && ` · ${vigente.detalle}`}
          </Alerta>
        )}
        {recomendaciones.map((r, i) => <Alerta key={i} tono="aviso" titulo={`Recomendación ${r.tipo.toLowerCase()}`}>{r.descripcion}</Alerta>)}
        {!vigente && !recomendaciones.length && (suspensiones.data || hv.data) && (
          <Alerta tono="exito">Sin alertas activas.</Alerta>
        )}
      </div>
      <div className="flex flex-wrap gap-2 mt-auto">
        <Boton a={`/embarcaciones/${e.id}`}>Ver hoja de vida <ArrowRight className="size-4" /></Boton>
        {puedeMantener && <Boton variante="secundario" a={`/mantenimiento?embarcacion=${e.id}`} icono={<Wrench className="size-4" />}>Registrar mantenimiento</Boton>}
      </div>
    </Tarjeta>
  )
}

/** «Comparativo del mes»: viajes por semana (consolidado, supervisor) y combustible del mes. */
function ComparativoMes({ embarcaciones }: { embarcaciones: EmbarcacionResponse[] }) {
  const esSupervisor = usePermiso('usuarios.caracterizacion')
  const veCombustible = usePermiso('combustible')
  const rango = { desde: inicioMes(), hasta: hoyISO() }
  const consolidados = useQueries({
    queries: embarcaciones.map((e) => ({
      queryKey: ['consolidado', rango, 'SEMANA', e.nombre],
      queryFn: () => usuariosService.consolidado({ ...rango, agrupacion: 'SEMANA', embarcacion: e.nombre }),
      enabled: esSupervisor,
    })),
  })
  const combustible = useAbastecimientosDesde(embarcaciones, rangoDia(inicioMes()).desde, veCombustible)
  if (!esSupervisor && !veCombustible) return null

  const semanas = [...new Set(consolidados.flatMap((c) => c.data?.serie.map((p) => p.fecha) ?? []))].sort()
  const error = consolidados.find((c) => c.error)?.error
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Comparativo del mes" subtitulo={`${mesLargo(mesActual())} · viajes por semana`}
        acciones={<Leyenda items={embarcaciones.map((e) => ({ nombre: EMBARCACION[e.nombre], color: COLOR[e.nombre] }))} />} />
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1.4fr)_minmax(0,1fr)] items-start">
        {esSupervisor ? (
          consolidados.some((c) => c.isLoading) ? <Cargando /> : error ? <ErrorCarga error={error} /> : !semanas.length ? (
            <Vacio titulo="Sin viajes este mes" />
          ) : (
            <GraficoBarras titulo="Viajes por semana y embarcación"
              etiquetas={semanas.map((s) => `Sem. ${diaMes(`${s}T12:00:00Z`)}`)}
              series={embarcaciones.map((e, i) => ({ nombre: EMBARCACION[e.nombre], color: COLOR[e.nombre], valores: semanas.map((s) => consolidados[i]?.data?.serie.find((p) => p.fecha === s)?.viajes ?? 0) }))} />
          )
        ) : <span />}
        <div className="overflow-x-auto border border-borde rounded-[12px]">
          <table className="tabla">
            <thead><tr><th>Indicador</th>{embarcaciones.map((e) => <th key={e.id} className="text-right">{EMBARCACION[e.nombre]}</th>)}</tr></thead>
            <tbody>
              {esSupervisor && <>
                <tr><td>Viajes</td>{embarcaciones.map((e, i) => <td key={e.id} className="text-right">{entero(consolidados[i]?.data?.viajes)}</td>)}</tr>
                <tr><td>Pasajeros</td>{embarcaciones.map((e, i) => <td key={e.id} className="text-right">{entero(consolidados[i]?.data?.totalUsuarios)}</td>)}</tr>
              </>}
              {veCombustible && (
                <tr><td>Combustible (gal)</td>{embarcaciones.map((e) => <td key={e.id} className="text-right">{combustible.listo ? decimal((combustible.porEmbarcacion.get(e.id) ?? []).reduce((s, a) => s + Number(a.galones), 0)) : '…'}</td>)}</tr>
              )}
              <tr><td>Estado actual</td>{embarcaciones.map((e) => <td key={e.id} className="text-right">{e.estado === 'OPERATIVA' ? <CircleCheck className="size-4 text-exito inline" aria-label="Operativa" /> : <OctagonAlert className="size-4 text-advertencia inline" aria-label={ESTADO_EMBARCACION[e.estado]} />}</td>)}</tr>
            </tbody>
          </table>
          <p className="m-0 px-4 py-2 text-[11.5px] text-texto-2 border-t border-borde">Del {fechaLocal(rango.desde)} al {fechaLocal(rango.hasta)}</p>
        </div>
      </div>
    </Tarjeta>
  )
}
