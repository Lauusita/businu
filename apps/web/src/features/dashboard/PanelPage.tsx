import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { ArrowRight, Fuel, Route, Ship, TriangleAlert, UsersRound, Wrench } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { GraficoBarras } from '../../components/charts/GraficoBarras'
import { GraficoLineas } from '../../components/charts/GraficoLineas'
import { Leyenda } from '../../components/charts/Leyenda'
import { MapaCorredor } from '../../components/charts/MapaCorredor'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Insignia } from '../../components/ui/Insignia'
import { Kpi } from '../../components/ui/Kpi'
import { Segmentado } from '../../components/ui/Selecciones'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAbiertas, useEmbarcaciones, useTablero } from '../../hooks/consultas'
import { usePermiso } from '../../hooks/usePermiso'
import { usuariosService } from '../../services/modules/usuarios.service'
import type { ConsolidadoDemanda } from '../../types/api'
import type { Embarcaciones, Embarcaderos, EstadoActivo } from '../../types/enums'
import { decimal, diaMes, entero, fechaLarga, hoyISO, rangoDia } from '../../utils/formato'
import { useAbastecimientosDesde } from '../combustible/consultas'
import { ActividadReciente } from './ActividadReciente'
import { AlertasPanel } from './AlertasPanel'
import { PERIODOS, rangoPeriodo, type Periodo } from './periodos'
import { EstadoEmbarcaciones } from './TarjetasEmbarcacion'

const PERIODO_TEXTO: Record<Periodo, string> = { HOY: 'de hoy', SEMANA: 'de la semana', MES: 'del mes' }

/** Plantilla 02 · Panel principal. Todas las cifras salen de la API; nada es demostrativo. */
export function PanelPage() {
  const [periodo, setPeriodo] = useState<Periodo>('MES')
  const rango = rangoPeriodo(periodo)
  const esSupervisor = usePermiso('usuarios.caracterizacion')
  const veCombustible = usePermiso('combustible')

  const embarcaciones = useEmbarcaciones()
  const tablero = useTablero()
  const abiertas = useAbiertas()

  const consolidado = useQuery({
    queryKey: ['consolidado', rango, 'DIA'],
    queryFn: () => usuariosService.consolidado({ ...rango, agrupacion: 'DIA' }),
    enabled: esSupervisor,
  })
  const porEmbarcacion = (nombre: Embarcaciones) => ({
    queryKey: ['consolidado', rango, 'DIA', nombre],
    queryFn: () => usuariosService.consolidado({ ...rango, agrupacion: 'DIA', embarcacion: nombre }),
    enabled: esSupervisor && periodo !== 'HOY',
  })
  const betanci = useQuery(porEmbarcacion('BETANCI'))
  const yapel = useQuery(porEmbarcacion('YAPEL'))
  const hoy = useQuery({
    queryKey: ['consolidado', { desde: hoyISO(), hasta: hoyISO() }, 'DIA'],
    queryFn: () => usuariosService.consolidado({ desde: hoyISO(), hasta: hoyISO(), agrupacion: 'DIA' }),
    enabled: esSupervisor,
  })
  const combustible = useAbastecimientosDesde(embarcaciones.data, rangoDia(rango.desde).desde, veCombustible)

  const disponibles = embarcaciones.data?.filter((e) => e.estado === 'OPERATIVA').length
  const enMantenimiento = embarcaciones.data?.filter((e) => e.estado === 'EN_MANTENIMIENTO')
  const abiertosInfra = tablero.data?.reduce((s, t) => s + t.registrosAbiertos, 0)
  const estadosMapa = Object.fromEntries(
    (tablero.data ?? []).filter((t) => t.tipo === 'EMBARCADERO').map((t) => [t.identificador, t.estadoActual]),
  ) as Partial<Record<Embarcaderos, EstadoActivo>>

  return (
    <Pagina
      titulo="Panel principal"
      descripcion={<>Resumen de la operación piloto · {fechaLarga(new Date().toISOString())}</>}
      acciones={<Segmentado etiqueta="Periodo" opciones={PERIODOS} valor={periodo} onCambio={setPeriodo} />}
    >
      <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-6 gap-4">
        {esSupervisor && (
          <>
            <Kpi icono={<Route />} cifra={entero(consolidado.data?.viajes)} etiqueta={`Viajes ${PERIODO_TEXTO[periodo]}`} cargando={consolidado.isLoading}
              insignia={abiertas.data?.length ? <Insignia tono="info">{abiertas.data.length} en curso</Insignia> : undefined} />
            <Kpi icono={<UsersRound />} tono="dorado" cifra={entero(consolidado.data?.totalUsuarios)} etiqueta="Pasajeros transportados" cargando={consolidado.isLoading}
              insignia={consolidado.data?.promedioPorViaje ? <Insignia tono="ok">{decimal(consolidado.data.promedioPorViaje)} por viaje</Insignia> : undefined} />
          </>
        )}
        <Kpi icono={<Ship />} tono="exito" cifra={embarcaciones.data ? `${disponibles} de ${embarcaciones.data.length}` : '—'} etiqueta="Embarcaciones disponibles" cargando={embarcaciones.isLoading} />
        <Kpi icono={<Wrench />} tono="dorado" cifra={entero(enMantenimiento?.length)} etiqueta="Embarcación en mantenimiento" cargando={embarcaciones.isLoading}
          insignia={enMantenimiento?.length ? <Insignia tono="warn">{enMantenimiento.map((e) => (e.nombre === 'BETANCI' ? 'Betancí' : 'Yapel')).join(', ')}</Insignia> : undefined} />
        <Kpi icono={<TriangleAlert />} tono="error" cifra={entero(abiertosInfra)} etiqueta="Novedades de infraestructura abiertas" cargando={tablero.isLoading} />
        {veCombustible && (
          <Kpi icono={<Fuel />} tono="azul" cifra={combustible.listo ? `${decimal(combustible.galones)} gal` : '—'} etiqueta={`Combustible ${PERIODO_TEXTO[periodo]}`}
            cargando={combustible.cargando}
            insignia={combustible.listo ? <Insignia tono="neutral">{combustible.todos.length} cargues</Insignia> : undefined} />
        )}
      </div>

      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        {esSupervisor ? (
          <PasajerosPorDia general={consolidado.data} betanci={betanci.data} yapel={yapel.data} cargando={consolidado.isLoading} error={consolidado.error} periodo={periodo} />
        ) : (
          <ViajesEnCurso />
        )}
        <EstadoEmbarcaciones />
      </div>

      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <Tarjeta>
          <EncabezadoTarjeta
            titulo="Recorrido operativo"
            subtitulo="Esquema del corredor fluvial y estado de cada embarcadero"
            acciones={<Boton a="/infraestructura" variante="suave" pequeno>Ver infraestructura <ArrowRight className="size-3.5" /></Boton>}
          />
          {tablero.error ? <ErrorCarga error={tablero.error} onReintentar={() => tablero.refetch()} /> : <MapaCorredor estados={estadosMapa} />}
          <Leyenda items={[
            { nombre: 'Operativo', color: '#0E8250' }, { nombre: 'Con restricciones', color: '#A85600' },
            { nombre: 'Fuera de servicio', color: '#C42F2F' }, { nombre: 'Extensión proyectada', color: '#5E6478' },
          ]} />
        </Tarjeta>
        <AlertasPanel />
      </div>

      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        {esSupervisor ? <PasajerosPorHora datos={hoy.data} cargando={hoy.isLoading} error={hoy.error} /> : <span className="hidden xl:block" />}
        <ActividadReciente />
      </div>
    </Pagina>
  )
}

function PasajerosPorDia({ general, betanci, yapel, cargando, error, periodo }: {
  general?: ConsolidadoDemanda; betanci?: ConsolidadoDemanda; yapel?: ConsolidadoDemanda; cargando: boolean; error: unknown; periodo: Periodo
}) {
  const serie = general?.serie ?? []
  const valoresDe = (c?: ConsolidadoDemanda) => serie.map((p) => c?.serie.find((x) => x.fecha === p.fecha)?.usuarios ?? null)
  const suspendidos = general?.suspendidos
  return (
    <Tarjeta>
      <EncabezadoTarjeta
        titulo="Pasajeros transportados por día"
        subtitulo="Usuarios registrados en taquilla, por embarcación"
        acciones={periodo !== 'HOY' ? <Leyenda items={[{ nombre: 'Betancí', color: '#0A6EDC' }, { nombre: 'Yapel', color: '#EDC92A' }]} /> : undefined}
      />
      {cargando ? <Cargando alto={240} /> : error ? <ErrorCarga error={error} /> : !general || general.totalUsuarios === 0 ? (
        <Vacio titulo="Sin pasajeros en el periodo">Aún no hay registros de usuarios para las fechas seleccionadas.</Vacio>
      ) : periodo === 'HOY' ? (
        <p className="m-0 text-[14px]">Hoy van <b>{entero(general.totalUsuarios)}</b> pasajeros en <b>{entero(general.viajes)}</b> viajes. La serie diaria se muestra en los periodos «Semana» y «Mes».</p>
      ) : (
        <GraficoLineas
          titulo="Pasajeros transportados por día"
          etiquetas={serie.map((p) => diaMes(`${p.fecha}T12:00:00Z`))}
          series={[{ nombre: 'Betancí', valores: valoresDe(betanci), color: '#0A6EDC' }, { nombre: 'Yapel', valores: valoresDe(yapel), color: '#EDC92A' }]}
        />
      )}
      {!!suspendidos?.viajes && (
        <Alerta tono="aviso">{entero(suspendidos.viajes)} viaje(s) suspendido(s) con {entero(suspendidos.usuarios)} usuarios en el periodo.</Alerta>
      )}
    </Tarjeta>
  )
}

function PasajerosPorHora({ datos, cargando, error }: { datos?: ConsolidadoDemanda; cargando: boolean; error: unknown }) {
  const horas = Array.from({ length: 17 }, (_, i) => i + 5) // 05:00 a 21:00
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Pasajeros por franja horaria · hoy" subtitulo="Horario general de operación: 5:00 a. m. – 9:00 p. m." />
      {cargando ? <Cargando alto={240} /> : error ? <ErrorCarga error={error} /> : (
        <GraficoBarras
          titulo="Pasajeros por hora del día de hoy"
          etiquetas={horas.map((h) => `${h}h`)}
          series={[{ nombre: 'Pasajeros', valores: horas.map((h) => datos?.porHora.find((x) => x.hora === h)?.usuarios ?? 0), color: '#0A6EDC' }]}
        />
      )}
    </Tarjeta>
  )
}

/** Para roles sin acceso al consolidado: los viajes en curso. */
function ViajesEnCurso() {
  const abiertas = useAbiertas()
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Viajes en curso" subtitulo="Bitácoras abiertas en este momento" acciones={<Boton a="/viajes" variante="suave" pequeno>Ver operación</Boton>} />
      {abiertas.isLoading ? <Cargando /> : abiertas.error ? <ErrorCarga error={abiertas.error} /> : !abiertas.data?.length ? (
        <Vacio titulo="No hay viajes en curso" />
      ) : (
        <ul className="m-0 p-0 list-none flex flex-col gap-2">
          {abiertas.data.map((o) => (
            <li key={o.id}>
              <Boton a={`/viajes/${o.id}`} variante="secundario" className="w-full justify-between">
                <span>{o.embarcacion === 'BETANCI' ? 'Betancí' : 'Yapel'} · {o.embarcaderoOrigen} → {o.embarcaderoDestino}</span>
                <ArrowRight className="size-4" />
              </Boton>
            </li>
          ))}
        </ul>
      )}
    </Tarjeta>
  )
}
