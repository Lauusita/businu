import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Anchor, CalendarDays, Filter, Plus, Route, UsersRound } from 'lucide-react'
import { Navigate } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { BarrasHorizontales } from '../../components/charts/BarrasHorizontales'
import { Dona } from '../../components/charts/Dona'
import { GraficoBarras } from '../../components/charts/GraficoBarras'
import { GraficoLineas } from '../../components/charts/GraficoLineas'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { Paginacion } from '../../components/tables/Paginacion'
import { Tabla } from '../../components/tables/Tabla'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada, Selector } from '../../components/ui/Campo'
import { Kpi } from '../../components/ui/Kpi'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { usePermiso } from '../../hooks/usePermiso'
import { usuariosService } from '../../services/modules/usuarios.service'
import {
  AGRUPACION, EMBARCACION, EMBARCADERO, EMBARCADEROS, FRANJA, GRUPO_ETARIO, GRUPO_ETARIO_CORTO, SEXO, opciones,
  type AgrupacionSerie, type Embarcaciones, type Embarcaderos, type GrupoEtario, type Sexo,
} from '../../types/enums'
import { decimal, diaMes, entero, fechaHora, fechaLocal, hoyISO, inicioMes, porcentaje } from '../../utils/formato'
import { BotonExportar } from '../informes/BotonExportar'

interface Filtros { desde: string; hasta: string; origen?: Embarcaderos; embarcacion?: Embarcaciones; agrupacion: AgrupacionSerie }

/** Plantilla 13 · Caracterización de usuarios (GET /pasajeros/consolidado, R22). Solo SUPERVISOR. */
export function UsuariosPage() {
  const esSupervisor = usePermiso('usuarios.caracterizacion')
  if (!esSupervisor) return <Navigate to="/usuarios/registro" replace />
  return <Caracterizacion />
}

function Caracterizacion() {
  const [borrador, setBorrador] = useState<Filtros>({ desde: inicioMes(), hasta: hoyISO(), agrupacion: 'DIA' })
  const [filtros, setFiltros] = useState<Filtros>(borrador)
  const [pagina, setPagina] = useState(0)
  const q = useQuery({ queryKey: ['consolidado', filtros], queryFn: () => usuariosService.consolidado(filtros), placeholderData: keepPreviousData })
  const recientes = useQuery({
    queryKey: ['pasajeros', { embarcacion: filtros.embarcacion, puertoAbordaje: filtros.origen }, pagina],
    queryFn: () => usuariosService.listar({ embarcacion: filtros.embarcacion, puertoAbordaje: filtros.origen }, { page: pagina, size: 8 }),
    placeholderData: keepPreviousData,
  })
  const c = q.data
  const errorRango = borrador.desde > borrador.hasta ? 'La fecha inicial es posterior a la final' : undefined
  const origenTop = c ? (Object.entries(c.porOrigen) as [Embarcaderos, number][]).sort((a, b) => b[1] - a[1])[0] : undefined
  const diasConViajes = c?.serie.filter((p) => p.viajes > 0).length ?? 0

  return (
    <Pagina
      titulo="Caracterización de usuarios"
      migas={[{ etiqueta: 'Usuarios' }, { etiqueta: 'Caracterización' }]}
      descripcion="Comportamiento de la demanda y características generales de uso del servicio."
      acciones={<><Boton variante="suave" a="/usuarios/registro" icono={<Plus className="size-4" />}>Registrar usuario</Boton><BotonExportar conjunto="PASAJEROS" /></>}
    >
      <Tarjeta>
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-[1fr_1fr_1fr_1fr_0.8fr_auto] items-end">
          <Campo etiqueta="Desde" error={errorRango}>{(p) => <Entrada {...p} type="date" value={borrador.desde} max={hoyISO()} icono={<CalendarDays />} onChange={(e) => setBorrador((b) => ({ ...b, desde: e.target.value }))} />}</Campo>
          <Campo etiqueta="Hasta">{(p) => <Entrada {...p} type="date" value={borrador.hasta} max={hoyISO()} onChange={(e) => setBorrador((b) => ({ ...b, hasta: e.target.value }))} />}</Campo>
          <Campo etiqueta="Embarcadero de origen">{(p) => <Selector {...p} vacio="Todos" opciones={opciones(EMBARCADERO)} value={borrador.origen ?? ''} onChange={(e) => setBorrador((b) => ({ ...b, origen: (e.target.value || undefined) as Embarcaderos }))} />}</Campo>
          <Campo etiqueta="Embarcación">{(p) => <Selector {...p} vacio="Todas" opciones={opciones(EMBARCACION)} value={borrador.embarcacion ?? ''} onChange={(e) => setBorrador((b) => ({ ...b, embarcacion: (e.target.value || undefined) as Embarcaciones }))} />}</Campo>
          <Campo etiqueta="Agrupar por">{(p) => <Selector {...p} opciones={opciones(AGRUPACION)} value={borrador.agrupacion} onChange={(e) => setBorrador((b) => ({ ...b, agrupacion: e.target.value as AgrupacionSerie }))} />}</Campo>
          <Boton icono={<Filter className="size-4" />} disabled={!!errorRango} onClick={() => { setFiltros(borrador); setPagina(0) }}>Aplicar</Boton>
        </div>
      </Tarjeta>

      {q.isLoading ? <Cargando alto={320} /> : q.error ? <Tarjeta><ErrorCarga error={q.error} onReintentar={() => q.refetch()} /></Tarjeta> : c && (
        <>
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            <Kpi icono={<UsersRound />} tono="dorado" cifra={entero(c.totalUsuarios)} etiqueta="Usuarios transportados" nota={`Del ${fechaLocal(c.desde)} al ${fechaLocal(c.hasta)}`} />
            <Kpi icono={<CalendarDays />} cifra={diasConViajes ? decimal(c.totalUsuarios / diasConViajes) : '—'} etiqueta="Promedio por día de operación" nota={`${diasConViajes} día(s) con viajes`} />
            <Kpi icono={<Route />} cifra={c.promedioPorViaje !== undefined ? decimal(c.promedioPorViaje) : '—'} etiqueta="Promedio por viaje" nota={`${entero(c.viajes)} viajes`} />
            <Kpi icono={<Anchor />} cifra={origenTop && origenTop[1] > 0 ? EMBARCADERO[origenTop[0]] : '—'} etiqueta="Origen más frecuente" nota={origenTop && c.totalUsuarios ? `${porcentaje(origenTop[1], c.totalUsuarios)} de los abordajes` : undefined} />
          </div>
          {c.suspendidos.viajes > 0 && <Alerta tono="aviso">Incluye {entero(c.suspendidos.usuarios)} usuarios de {entero(c.suspendidos.viajes)} viaje(s) suspendido(s).</Alerta>}

          {c.totalUsuarios === 0 ? <Tarjeta><Vacio titulo="Sin usuarios en el periodo">Ajuste los filtros o registre usuarios en taquilla.</Vacio></Tarjeta> : (
            <>
              <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
                <Tarjeta>
                  <EncabezadoTarjeta titulo={`Usuarios por ${AGRUPACION[c.agrupacion].toLowerCase()}`} subtitulo={c.agrupacion === 'SEMANA' ? 'Cada punto es la semana que empieza ese lunes' : undefined} />
                  <GraficoLineas titulo="Usuarios por periodo" etiquetas={c.serie.map((p) => diaMes(`${p.fecha}T12:00:00Z`))} series={[{ nombre: 'Usuarios', valores: c.serie.map((p) => p.usuarios), color: '#0A6EDC' }]} />
                </Tarjeta>
                <div className="flex flex-col gap-5">
                  <Tarjeta>
                    <EncabezadoTarjeta titulo="Distribución por sexo" />
                    <Dona titulo="Distribución por sexo" etiquetaTotal="usuarios" partes={(['FEMENINO', 'MASCULINO'] as Sexo[]).map((s, i) => ({ nombre: SEXO[s], valor: c.porSexo[s] ?? 0, color: i ? '#0A6EDC' : '#051469' }))} />
                  </Tarjeta>
                  <Tarjeta>
                    <EncabezadoTarjeta titulo="Por embarcadero de origen" />
                    <BarrasHorizontales items={EMBARCADEROS.map((e, i) => ({ nombre: EMBARCADERO[e], valor: c.porOrigen[e] ?? 0, color: ['#051469', '#0A6EDC', '#EDC92A'][i] }))} />
                  </Tarjeta>
                </div>
              </div>
              <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
                <Tarjeta>
                  <EncabezadoTarjeta titulo="Usuarios por hora del día" subtitulo="Horario general 5:00 a. m. – 9:00 p. m." />
                  <GraficoBarras titulo="Usuarios por hora" etiquetas={c.porHora.map((h) => `${h.hora}`)} series={[{ nombre: 'Usuarios', valores: c.porHora.map((h) => h.usuarios), color: '#0A6EDC' }]} />
                </Tarjeta>
                <Tarjeta>
                  <EncabezadoTarjeta titulo="Por franja horaria" />
                  <BarrasHorizontales items={c.porFranjaHoraria.map((f) => ({ nombre: `${FRANJA[f.franja]} (${f.desde}–${f.hasta})`, valor: f.usuarios, color: '#013996' }))} />
                </Tarjeta>
              </div>
              <div className="grid xl:grid-cols-2 gap-5 items-start">
                <Tarjeta>
                  <EncabezadoTarjeta titulo="Usuarios por grupo etario" />
                  <GraficoBarras titulo="Usuarios por grupo etario" etiquetas={(Object.keys(GRUPO_ETARIO) as GrupoEtario[]).map((g) => GRUPO_ETARIO_CORTO[g])}
                    series={[{ nombre: 'Usuarios', valores: (Object.keys(GRUPO_ETARIO) as GrupoEtario[]).map((g) => c.porGrupoEtario[g] ?? 0), color: '#013996' }]} />
                </Tarjeta>
                <Tarjeta>
                  <EncabezadoTarjeta titulo="Matriz origen – destino" subtitulo="Usuarios por par de embarcaderos en el periodo" />
                  <div className="overflow-x-auto border border-borde rounded-[12px]">
                    <table className="tabla min-w-[460px]">
                      <thead><tr><th>Origen \ destino</th>{EMBARCADEROS.map((d) => <th key={d} className="text-center">{EMBARCADERO[d]}</th>)}</tr></thead>
                      <tbody>
                        {EMBARCADEROS.map((o) => (
                          <tr key={o}>
                            <td className="font-bold">{EMBARCADERO[o]}</td>
                            {EMBARCADEROS.map((d) => {
                              const v = c.origenDestino.find((x) => x.origen === o && x.destino === d)?.usuarios
                              return <td key={d} className="text-center">{o === d ? <span className="text-texto-3">—</span> : <span className={`inline-block min-w-[64px] rounded-lg px-2 py-1 font-bold ${v ? 'bg-azul-claro/15 text-noche' : 'text-texto-2'}`}>{entero(v ?? 0)}</span>}</td>
                            })}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </Tarjeta>
              </div>
            </>
          )}
        </>
      )}

      <Tarjeta sinRelleno>
        <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Registros recientes" subtitulo="Con los filtros de embarcadero y embarcación" /></div>
        <Tabla
          columnas={[
            { clave: 'f', titulo: 'Fecha', celda: (u) => fechaHora(u.fechaHora) },
            { clave: 'e', titulo: 'Embarcación', celda: (u) => (u.embarcacion ? EMBARCACION[u.embarcacion.nombre] : '—') },
            { clave: 'o', titulo: 'Origen', celda: (u) => (u.embarcaderoAbordaje ? EMBARCADERO[u.embarcaderoAbordaje.codigo] : '—') },
            { clave: 'd', titulo: 'Destinos', celda: (u) => Object.entries(u.porDestino ?? {}).filter(([, v]) => v).map(([k, v]) => `${EMBARCADERO[k as Embarcaderos]} (${v})`).join(', ') },
            { clave: 'n', titulo: 'Usuarios', alinear: 'derecha', celda: (u) => <b>{entero(u.usuariosTransportados)}</b> },
            { clave: 's', titulo: 'Sexo', celda: (u) => `M ${u.porSexo?.FEMENINO ?? 0} · H ${u.porSexo?.MASCULINO ?? 0}` },
            { clave: 'r', titulo: 'Registró', celda: (u) => u.registradoPor ?? '—' },
          ]}
          filas={recientes.data?.content}
          clave={(u) => u.id}
          cargando={recientes.isLoading}
          error={recientes.error}
          minimo={900}
          pie={recientes.data && <Paginacion pagina={pagina} totalPaginas={recientes.data.page.totalPages} totalElementos={recientes.data.page.totalElements} tamano={8} onCambio={setPagina} />}
        />
      </Tarjeta>
    </Pagina>
  )
}
