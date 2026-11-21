import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import {
  CalendarClock, Cog, Download, FileText, Gauge, History, Info, Package, PauseCircle, PlayCircle, ShieldCheck, TriangleAlert, Wrench,
} from 'lucide-react'
import { useNavigate, useParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { IlustracionEmbarcacion } from '../../components/brand/Marca'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Paginacion } from '../../components/tables/Paginacion'
import { Tabla } from '../../components/tables/Tabla'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Insignia } from '../../components/ui/Insignia'
import { EstadoEmbarcacionInsignia, EstadoOperacionInsignia } from '../../components/ui/InsigniasEstado'
import { Pestanas } from '../../components/ui/Selecciones'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useHojaDeVida, useMotores } from '../../hooks/consultas'
import { usePermiso } from '../../hooks/usePermiso'
import { embarcacionesService } from '../../services/modules/embarcaciones.service'
import { viajesService } from '../../services/modules/viajes.service'
import type { HojaDeVidaResponse, MantenimientoResponse, MotorResponse, SuspensionResponse } from '../../types/api'
import { EMBARCACION, TIPO_MANTENIMIENTO, nombreEmbarcadero } from '../../types/enums'
import { guardarArchivo } from '../../utils/descargas'
import { bytes, codigoCorto, decimal, entero, fecha, fechaHora, hora, pesos } from '../../utils/formato'
import { Comunicaciones } from '../compartido/Comunicaciones'
import { Trazabilidad } from '../compartido/Trazabilidad'
import { DialogoHorometro, DialogoLevantarSuspension, DialogoSuspenderEmbarcacion } from './DialogosEmbarcacion'

type Pestana = 'RESUMEN' | 'OPERACIONES' | 'MANTENIMIENTOS' | 'REPUESTOS' | 'MOTORES' | 'SUSPENSIONES'
const PESTANAS: { valor: Pestana; etiqueta: string }[] = [
  { valor: 'RESUMEN', etiqueta: 'Resumen' },
  { valor: 'OPERACIONES', etiqueta: 'Historial de operaciones' },
  { valor: 'MANTENIMIENTOS', etiqueta: 'Mantenimientos' },
  { valor: 'REPUESTOS', etiqueta: 'Repuestos e insumos' },
  { valor: 'MOTORES', etiqueta: 'Motores y horómetro' },
  { valor: 'SUSPENSIONES', etiqueta: 'Suspensiones' },
]

/** Plantilla 07 · Hoja de vida y seguimiento técnico de una embarcación (numeral 4.3). */
export function DetalleEmbarcacionPage() {
  const { id = '' } = useParams()
  const navegar = useNavigate()
  const { avisar } = useAvisos()
  const [pestana, setPestana] = useState<Pestana>('RESUMEN')
  const [dialogo, setDialogo] = useState<'suspender' | { levantar: SuspensionResponse } | { motor: MotorResponse } | null>(null)
  const [descargando, setDescargando] = useState(false)
  const puedePdf = usePermiso('embarcaciones.hojaVidaPdf')
  const puedeSuspender = usePermiso('embarcaciones.suspender')
  const puedeMantener = usePermiso('mantenimiento.registrar')

  const hv = useHojaDeVida(id)
  const suspensiones = useQuery({ queryKey: ['suspensiones', id], queryFn: () => embarcacionesService.suspensiones(id) })

  if (hv.isLoading) return <Pagina titulo="Embarcación"><Cargando alto={320} /></Pagina>
  if (hv.error || !hv.data) return <Pagina titulo="Embarcación"><Tarjeta><ErrorCarga error={hv.error} onReintentar={() => hv.refetch()} /></Tarjeta></Pagina>
  const h = hv.data
  const e = h.embarcacion
  const nombre = EMBARCACION[e.nombre]
  const vigente = suspensiones.data?.find((s) => s.vigente)

  const descargarPdf = async () => {
    setDescargando(true)
    try {
      const r = await embarcacionesService.hojaDeVidaPdf(id)
      guardarArchivo(r.blob, r.nombre ?? `hoja-de-vida-${e.nombre.toLowerCase()}.pdf`)
    } catch (err) {
      avisar(err instanceof Error ? err.message : 'No se pudo descargar la hoja de vida.', 'error')
    } finally {
      setDescargando(false)
    }
  }

  return (
    <Pagina
      titulo={nombre}
      migas={[{ etiqueta: 'Embarcaciones', a: '/embarcaciones' }, { etiqueta: nombre }]}
      descripcion="Hoja de vida y seguimiento técnico de la embarcación (numeral 4.3)."
      acciones={puedePdf && <Boton variante="secundario" icono={<Download className="size-4" />} cargando={descargando} onClick={descargarPdf}>Descargar hoja de vida</Boton>}
    >
      <Tarjeta className="!flex-row flex-wrap items-center gap-5">
        <div className="w-[150px] rounded-[14px] bg-azul-suave p-3"><IlustracionEmbarcacion ondas={false} /></div>
        <div className="flex-1 min-w-[240px]">
          <div className="flex flex-wrap items-center gap-2.5">
            <h2 className="m-0 font-titulo text-[30px] font-normal text-noche">{nombre}</h2>
            <EstadoEmbarcacionInsignia estado={e.estado} />
            {vigente && <Insignia tono="err">Suspendida</Insignia>}
          </div>
          <p className="m-0 text-[13px] text-texto-2">Embarcación de transporte de pasajeros · patente {e.patente}</p>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-3">
            <Dato etiqueta="Capacidad">{e.capacidadPasajeros} pasajeros</Dato>
            <Dato etiqueta="Intervenciones">{entero(h.totalIntervenciones)}</Dato>
            <Dato etiqueta="Preventivos / correctivos">{entero(h.preventivos)} / {entero(h.correctivos)}</Dato>
            <Dato etiqueta="Costo de mantenimiento">{pesos(h.costoTotalMantenimiento)}</Dato>
          </div>
        </div>
        <div className="flex flex-col gap-2 min-w-[210px]">
          {puedeMantener && <Boton icono={<Wrench className="size-4" />} onClick={() => navegar(`/mantenimiento?embarcacion=${id}`)}>Registrar mantenimiento</Boton>}
          {puedeSuspender && (vigente
            ? <Boton variante="secundario" icono={<PlayCircle className="size-4" />} onClick={() => setDialogo({ levantar: vigente })}>Levantar suspensión</Boton>
            : <Boton variante="peligro" icono={<PauseCircle className="size-4" />} onClick={() => setDialogo('suspender')}>Suspender embarcación</Boton>)}
        </div>
      </Tarjeta>

      <Pestanas opciones={PESTANAS} valor={pestana} onCambio={setPestana} />

      {pestana === 'RESUMEN' && <Resumen h={h} vigente={vigente} />}
      {pestana === 'OPERACIONES' && <HistorialOperaciones nombre={e.nombre} />}
      {pestana === 'MANTENIMIENTOS' && <Mantenimientos lista={h.intervenciones} />}
      {pestana === 'REPUESTOS' && <Repuestos lista={h.intervenciones} />}
      {pestana === 'MOTORES' && <Motores embarcacionId={id} onLectura={(m) => setDialogo({ motor: m })} />}
      {pestana === 'SUSPENSIONES' && (
        <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
          <Suspensiones cargando={suspensiones.isLoading} error={suspensiones.error} lista={suspensiones.data} />
          {vigente && <Comunicaciones referenciaTipo="SUSPENSION" referenciaId={vigente.id} asuntoSugerido={`Suspensión de ${nombre} · ${vigente.causaDescripcion}`} />}
        </div>
      )}

      {dialogo === 'suspender' && <DialogoSuspenderEmbarcacion embarcacionId={id} nombre={nombre} onCerrar={() => setDialogo(null)} />}
      {dialogo && typeof dialogo === 'object' && 'levantar' in dialogo && <DialogoLevantarSuspension suspension={dialogo.levantar} onCerrar={() => setDialogo(null)} />}
      {dialogo && typeof dialogo === 'object' && 'motor' in dialogo && <DialogoHorometro motor={dialogo.motor} onCerrar={() => setDialogo(null)} />}
    </Pagina>
  )
}

function Resumen({ h, vigente }: { h: HojaDeVidaResponse; vigente?: SuspensionResponse }) {
  const ahora = new Date().toISOString()
  const proximas = h.intervenciones.filter((i) => i.proximaIntervencion && i.proximaIntervencion >= ahora)
    .sort((a, b) => a.proximaIntervencion!.localeCompare(b.proximaIntervencion!))
  const novedades = h.intervenciones.flatMap((i) => i.novedades.map((n) => ({ ...n, fecha: i.fechaIntervencion })))
  const ultima = h.intervenciones[0]
  return (
    <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
      <div className="flex flex-col gap-5 min-w-0">
        <Tarjeta>
          <EncabezadoTarjeta titulo="Información general" icono={<Info />} />
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
            <Dato etiqueta="Identificación">{EMBARCACION[h.embarcacion.nombre]}</Dato>
            <Dato etiqueta="Patente">{h.embarcacion.patente}</Dato>
            <Dato etiqueta="Capacidad autorizada">{h.embarcacion.capacidadPasajeros} pasajeros</Dato>
            <Dato etiqueta="Motores">{h.embarcacion.motores.length}</Dato>
            <Dato etiqueta="Última intervención">{fecha(h.ultimaIntervencion)}</Dato>
            <Dato etiqueta="Próxima intervención">{fecha(h.proximaIntervencion)}</Dato>
          </div>
        </Tarjeta>
        <Mantenimientos lista={h.intervenciones.slice(0, 4)} titulo="Últimos mantenimientos" />
      </div>
      <div className="flex flex-col gap-5 min-w-0">
        <Tarjeta>
          <EncabezadoTarjeta titulo="Alertas y recomendaciones" icono={<TriangleAlert />} tono="advertencia" />
          {vigente && <Alerta tono="error" titulo={`Suspendida · ${vigente.causaDescripcion}`}>Desde {fechaHora(vigente.fechaInicio)}</Alerta>}
          {ultima?.recomendaciones.map((r, i) => <Alerta key={i} tono="info" titulo={`Recomendación ${r.tipo.toLowerCase()}`}>{r.descripcion}</Alerta>)}
          {!vigente && !ultima?.recomendaciones.length && <p className="m-0 text-[13px] text-texto-2">Sin alertas ni recomendaciones pendientes.</p>}
        </Tarjeta>
        <Tarjeta>
          <EncabezadoTarjeta titulo="Próximas intervenciones" icono={<CalendarClock />} />
          {!proximas.length ? <p className="m-0 text-[13px] text-texto-2">No hay intervenciones programadas.</p> : (
            <ul className="m-0 p-0 list-none flex flex-col gap-2.5">
              {proximas.map((i) => (
                <li key={i.id} className="text-[13px]">
                  <b>{fecha(i.proximaIntervencion)}</b> · {TIPO_MANTENIMIENTO[i.tipo]} · {i.sistema}
                  {i.condicionProximaIntervencion && <div className="text-[12px] text-texto-2">{i.condicionProximaIntervencion}</div>}
                </li>
              ))}
            </ul>
          )}
        </Tarjeta>
        <Tarjeta>
          <EncabezadoTarjeta titulo="Novedades mecánicas" icono={<ShieldCheck />} />
          {!novedades.length ? <p className="m-0 text-[13px] text-texto-2">Sin novedades registradas en mantenimientos.</p> : (
            <ul className="m-0 p-0 list-none flex flex-col gap-2 text-[13px]">
              {novedades.slice(0, 6).map((n, i) => <li key={i}><b>{n.condicion}</b> · {n.severidad.toLowerCase()} · {fecha(n.fecha)}</li>)}
            </ul>
          )}
        </Tarjeta>
        <Trazabilidad tipo="EMBARCACION" id={h.embarcacion.id} />
      </div>
    </div>
  )
}

function HistorialOperaciones({ nombre }: { nombre: HojaDeVidaResponse['embarcacion']['nombre'] }) {
  const navegar = useNavigate()
  const [pagina, setPagina] = useState(0)
  const q = useQuery({
    queryKey: ['operaciones', { embarcacion: nombre }, 'TODOS', pagina],
    queryFn: () => viajesService.listar({ embarcacion: nombre }, { page: pagina, size: 10 }),
    placeholderData: keepPreviousData,
  })
  return (
    <Tarjeta sinRelleno>
      <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Historial de operaciones" icono={<History />} /></div>
      <Tabla
        columnas={[
          { clave: 'id', titulo: 'Viaje', celda: (o) => <b className="text-azul">{codigoCorto('V', o.id)}</b> },
          { clave: 'f', titulo: 'Fecha', celda: (o) => fecha(o.fechaHoraInicio) },
          { clave: 'r', titulo: 'Recorrido', celda: (o) => `${nombreEmbarcadero(o.embarcaderoOrigen)} → ${nombreEmbarcadero(o.embarcaderoDestino)}` },
          { clave: 's', titulo: 'Salida', celda: (o) => hora(o.fechaHoraInicio) },
          { clave: 'p', titulo: 'Pasajeros', alinear: 'derecha', celda: (o) => entero(o.totalUsuariosTransportados) },
          { clave: 'e', titulo: 'Estado', celda: (o) => <EstadoOperacionInsignia estado={o.estado} /> },
        ]}
        filas={q.data?.content}
        clave={(o) => o.id}
        cargando={q.isLoading}
        error={q.error}
        onFila={(o) => navegar(`/viajes/${o.id}`)}
        pie={q.data && <Paginacion pagina={pagina} totalPaginas={q.data.page.totalPages} totalElementos={q.data.page.totalElements} tamano={10} onCambio={setPagina} nombre="viajes" />}
      />
    </Tarjeta>
  )
}

function Mantenimientos({ lista, titulo = 'Historial de mantenimiento preventivo y correctivo' }: { lista: MantenimientoResponse[]; titulo?: string }) {
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo={titulo} icono={<Wrench />} />
      {!lista.length ? <Vacio titulo="Sin mantenimientos registrados" /> : (
        <ol className="m-0 p-0 list-none flex flex-col gap-4">
          {lista.map((m) => (
            <li key={m.id} className="flex gap-3">
              <span className={`size-7 rounded-lg flex items-center justify-center flex-none ${m.tipo === 'CORRECTIVO' ? 'bg-advertencia-suave text-advertencia' : 'bg-azul-suave text-azul'}`} aria-hidden><Wrench className="size-4" /></span>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <b className="text-[14px]">{TIPO_MANTENIMIENTO[m.tipo]} · {m.sistema}{m.motor && ` (motor ${m.motor.serial})`}</b>
                  <Insignia tono={m.tipo === 'CORRECTIVO' ? 'warn' : 'info'}>{TIPO_MANTENIMIENTO[m.tipo]}</Insignia>
                </div>
                <p className="m-0 text-[13px] text-texto-2 mt-0.5">{m.descripcionActividades}</p>
                <div className="text-[12px] text-texto-3 mt-0.5">
                  {fecha(m.fechaIntervencion)}{m.responsable && ` · Responsable: ${m.responsable}`}{m.ejecutadoPor && ` · Ejecutó: ${m.ejecutadoPor}`}
                  {m.costoTotal !== undefined && ` · ${pesos(m.costoTotal)}`}
                </div>
                {!!m.soportes.length && (
                  <div className="flex flex-wrap gap-1.5 mt-1.5">
                    {m.soportes.map((s) => <span key={s.id} className="inline-flex items-center gap-1 text-[12px] bg-fondo rounded-lg px-2 py-1"><FileText className="size-3.5 text-azul" />{s.nombreOriginal} · {bytes(s.tamanoBytes)}</span>)}
                  </div>
                )}
              </div>
            </li>
          ))}
        </ol>
      )}
    </Tarjeta>
  )
}

function Repuestos({ lista }: { lista: MantenimientoResponse[] }) {
  const filas = lista.flatMap((m) => m.repuestos.map((r, i) => ({ ...r, clave: `${m.id}-${i}`, tipo: m.tipo, fecha: m.fechaIntervencion })))
  return (
    <Tarjeta sinRelleno>
      <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Repuestos e insumos utilizados" icono={<Package />} /></div>
      <Tabla
        columnas={[
          { clave: 'i', titulo: 'Elemento', celda: (r) => <>{r.item}{r.descripcion && <span className="text-texto-2"> · {r.descripcion}</span>}</> },
          { clave: 'c', titulo: 'Cantidad', alinear: 'derecha', celda: (r) => `${decimal(r.cantidad, 2)} ${r.unidad ?? ''}` },
          { clave: 'co', titulo: 'Costo', alinear: 'derecha', celda: (r) => pesos(r.costo) },
          { clave: 't', titulo: 'Intervención', celda: (r) => TIPO_MANTENIMIENTO[r.tipo] },
          { clave: 'f', titulo: 'Fecha', celda: (r) => fecha(r.fecha) },
        ]}
        filas={filas}
        clave={(r) => r.clave}
        vacio={<Vacio titulo="Sin repuestos registrados" />}
        minimo={620}
      />
    </Tarjeta>
  )
}

function Motores({ embarcacionId, onLectura }: { embarcacionId: string; onLectura: (m: MotorResponse) => void }) {
  const motores = useMotores(embarcacionId)
  const puedeLeer = usePermiso('embarcaciones.horometro')
  return (
    <Tarjeta sinRelleno>
      <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Motores y horómetro" icono={<Cog />} subtitulo="Lectura actual e intervalo de servicio" /></div>
      <Tabla
        columnas={[
          { clave: 'm', titulo: 'Motor', celda: (m) => <b>{m.marca} {m.modelo}</b> },
          { clave: 's', titulo: 'Serial', celda: (m) => m.serial },
          { clave: 'p', titulo: 'Potencia', alinear: 'derecha', celda: (m) => `${m.potenciaHp} HP` },
          { clave: 'h', titulo: 'Horómetro', alinear: 'derecha', celda: (m) => (m.horometroActual !== null ? `${decimal(m.horometroActual)} h` : 'Sin lectura') },
          { clave: 'i', titulo: 'Servicio cada', alinear: 'derecha', celda: (m) => `${entero(m.intervaloServicioHoras)} h` },
          { clave: 'a', titulo: '', alinear: 'derecha', celda: (m) => puedeLeer ? <Boton variante="suave" pequeno icono={<Gauge className="size-3.5" />} onClick={() => onLectura(m)}>Registrar lectura</Boton> : null },
        ]}
        filas={motores.data}
        clave={(m) => m.id}
        cargando={motores.isLoading}
        error={motores.error}
        vacio={<Vacio titulo="Sin motores registrados" />}
        minimo={720}
      />
    </Tarjeta>
  )
}

function Suspensiones({ cargando, error, lista }: { cargando: boolean; error: unknown; lista?: SuspensionResponse[] }) {
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Historial de suspensiones" icono={<PauseCircle />} subtitulo="§7.1.7: suspensiones de la embarcación mientras no navega" />
      {cargando ? <Cargando /> : error ? <ErrorCarga error={error} /> : !lista?.length ? <Vacio titulo="Sin suspensiones registradas" /> : (
        <ul className="m-0 p-0 list-none flex flex-col gap-3">
          {lista.map((s) => (
            <li key={s.id} className="rounded-[12px] border border-borde p-3.5 text-[13px]">
              <div className="flex flex-wrap items-center gap-2">
                <b>{s.causaDescripcion}</b>
                <Insignia tono={s.vigente ? 'err' : 'neutral'}>{s.vigente ? 'Vigente' : 'Levantada'}</Insignia>
              </div>
              {s.detalle && <p className="m-0 mt-1 text-texto-2">{s.detalle}</p>}
              {s.autoridad && <p className="m-0 text-texto-2">Autoridad: {s.autoridad}</p>}
              <div className="text-[12px] text-texto-3 mt-1">
                Desde {fechaHora(s.fechaInicio)}{s.registradoPor && ` · ${s.registradoPor}`}
                {s.fechaFin && ` · Hasta ${fechaHora(s.fechaFin)}${s.levantadaPor ? ` · ${s.levantadaPor}` : ''}`}
              </div>
              {s.observacionLevantamiento && <p className="m-0 mt-1 text-[12.5px]">Levantamiento: {s.observacionLevantamiento}</p>}
            </li>
          ))}
        </ul>
      )}
    </Tarjeta>
  )
}
