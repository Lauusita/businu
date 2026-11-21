import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Fragment, useState } from 'react'
import { Check, Plus, ShieldCheck, Ship, Trash2 } from 'lucide-react'
import { useNavigate, useSearchParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { CargaArchivos } from '../../components/forms/CargaArchivos'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Selector } from '../../components/ui/Campo'
import { EstadoEmbarcacionInsignia } from '../../components/ui/InsigniasEstado'
import { OpcionTarjeta, Segmentado } from '../../components/ui/Selecciones'
import { EncabezadoTarjeta, PasoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAbiertas, useEmbarcaciones, useHojaDeVida, useMotores, usePersonal, useTripulacionDisponible } from '../../hooks/consultas'
import { usePermiso } from '../../hooks/usePermiso'
import { mantenimientoService } from '../../services/modules/mantenimiento.service'
import type { CrearMantenimientoRequest, Soporte } from '../../types/api'
import {
  EMBARCACION, REPUESTO, SISTEMA_INTERVENIDO, TIPO_MANTENIMIENTO, TIPO_MANTENIMIENTO_AYUDA, TIPO_RECOMENDACION, opciones,
  type RepuestoInsumo, type SistemaIntervenido, type TipoMantenimiento, type TipoRecomendacion,
} from '../../types/enums'
import { ahoraLocal, decimal, fecha, localAInstant, pesos } from '../../utils/formato'
import { NovedadesMecanicas } from '../operacion/NovedadesMecanicas'
import { aDTO, validarNovedades, type NovedadEditable } from '../operacion/novedades'

interface FilaRepuesto { item: RepuestoInsumo | ''; descripcion: string; cantidad: string; unidad: string; costo: string }
interface FilaRecomendacion { tipo: TipoRecomendacion | ''; descripcion: string }

/** Plantilla 08 · Registrar mantenimiento (POST /mantenimientos). Queda en la hoja de vida. */
export function MantenimientoPage() {
  const [params] = useSearchParams()
  const navegar = useNavigate()
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const esSupervisor = usePermiso('personal')
  const embarcaciones = useEmbarcaciones()
  const abiertas = useAbiertas()
  const personal = usePersonal(esSupervisor)
  const disponibles = useTripulacionDisponible(!esSupervisor)

  const [embarcacionId, setEmbarcacionId] = useState(params.get('embarcacion') ?? '')
  const [tipo, setTipo] = useState<TipoMantenimiento>('PREVENTIVO')
  const [sistema, setSistema] = useState<SistemaIntervenido | ''>('')
  const [motorId, setMotorId] = useState('')
  const [fechaIntervencion, setFechaIntervencion] = useState(ahoraLocal())
  const [responsableId, setResponsableId] = useState('')
  const [ejecutadoPor, setEjecutadoPor] = useState('')
  const [descripcion, setDescripcion] = useState('')
  const [manoObra, setManoObra] = useState('')
  const [repuestos, setRepuestos] = useState<FilaRepuesto[]>([])
  const [novedades, setNovedades] = useState<NovedadEditable[]>([])
  const [recomendaciones, setRecomendaciones] = useState<FilaRecomendacion[]>([])
  const [proxima, setProxima] = useState('')
  const [condicionProxima, setCondicionProxima] = useState('')
  const [soportes, setSoportes] = useState<Soporte[]>([])
  const [intentado, setIntentado] = useState(false)

  const seleccionada = embarcaciones.data?.find((e) => e.id === embarcacionId) ?? embarcaciones.data?.[0]
  const idEmb = embarcacionId || seleccionada?.id || ''
  const motores = useMotores(idEmb || undefined)
  const hv = useHojaDeVida(idEmb || undefined)
  const enCurso = abiertas.data?.find((o) => o.embarcacion === seleccionada?.nombre)
  // El responsable es un operador de embarcación (CrearMantenimientoRequest.responsableId)
  const responsables = (esSupervisor ? personal.data : disponibles.data)?.filter((p) => p.rol === 'OPERADOR_EMBARCACION') ?? []

  const errores: Record<string, string> = {}
  if (!idEmb) errores.embarcacion = 'Seleccione la embarcación'
  if (enCurso) errores.embarcacion = 'La embarcación tiene un viaje en curso: finalícelo antes de registrar el mantenimiento'
  if (!sistema) errores.sistema = 'Seleccione el sistema intervenido'
  if (sistema === 'MOTOR' && !motorId) errores.motor = 'Seleccione el motor intervenido'
  if (!fechaIntervencion) errores.fecha = 'Indique la fecha'
  else if (new Date(localAInstant(fechaIntervencion)) > new Date()) errores.fecha = 'La fecha no puede ser futura'
  if (!responsableId) errores.responsable = 'Seleccione el responsable'
  if (!descripcion.trim()) errores.descripcion = 'Describa las actividades ejecutadas'
  repuestos.forEach((r, i) => {
    if (!r.item || !r.cantidad || Number(r.cantidad) <= 0) errores[`rep-${i}`] = 'Seleccione el elemento y una cantidad mayor que 0'
    else if (r.item === 'OTRO' && !r.descripcion.trim()) errores[`rep-${i}`] = 'Describa el elemento «Otro»'
  })
  recomendaciones.forEach((r, i) => { if (!r.tipo || !r.descripcion.trim()) errores[`rec-${i}`] = 'Complete el tipo y la descripción' })
  if (proxima && new Date(localAInstant(proxima)) <= new Date()) errores.proxima = 'Debe ser una fecha futura'
  const errNov = validarNovedades(novedades)
  if (Object.keys(errNov).length) errores.novedades = 'Revise las novedades'
  const e = (k: string) => (intentado ? errores[k] : undefined)

  const costoRepuestos = repuestos.reduce((s, r) => s + (Number(r.costo) || 0), 0)

  const m = useMutation({
    mutationFn: (d: CrearMantenimientoRequest) => mantenimientoService.registrar(d),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hoja-de-vida'] })
      queryClient.invalidateQueries({ queryKey: ['embarcaciones'] })
      avisar('Mantenimiento registrado en la hoja de vida.')
      navegar(`/embarcaciones/${idEmb}`)
    },
  })

  const guardar = () => {
    setIntentado(true)
    if (Object.keys(errores).length) return
    m.mutate({
      embarcacionId: idEmb,
      motorId: sistema === 'MOTOR' ? motorId : null,
      tipo,
      sistema: sistema as SistemaIntervenido,
      fechaIntervencion: localAInstant(fechaIntervencion),
      descripcionActividades: descripcion.trim(),
      ejecutadoPor: ejecutadoPor.trim() || null,
      responsableId,
      costoManoObra: manoObra === '' ? null : Number(manoObra),
      repuestos: repuestos.map((r) => ({ item: r.item as RepuestoInsumo, descripcion: r.descripcion.trim() || null, cantidad: Number(r.cantidad), unidad: r.unidad.trim() || null, costo: r.costo === '' ? null : Number(r.costo) })),
      novedades: aDTO(novedades),
      recomendaciones: recomendaciones.map((r) => ({ tipo: r.tipo as TipoRecomendacion, descripcion: r.descripcion.trim() })),
      proximaIntervencion: proxima ? localAInstant(proxima) : null,
      condicionProximaIntervencion: condicionProxima.trim() || null,
      soporteIds: soportes.map((s) => s.id),
    })
  }

  if (embarcaciones.isLoading) return <Pagina titulo="Registrar mantenimiento"><Cargando alto={320} /></Pagina>
  if (embarcaciones.error) return <Pagina titulo="Registrar mantenimiento"><Tarjeta><ErrorCarga error={embarcaciones.error} onReintentar={() => embarcaciones.refetch()} /></Tarjeta></Pagina>

  return (
    <Pagina titulo="Registrar mantenimiento" migas={[{ etiqueta: 'Mantenimiento' }, { etiqueta: 'Nuevo registro' }]}
      descripcion="Registra intervenciones preventivas, correctivas o predictivas en la hoja de vida.">
      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <PasoTarjeta numero={1} titulo="Embarcación e intervención" subtitulo="Identifica la unidad y el tipo de mantenimiento" />
            <div className="grid gap-4 sm:grid-cols-3">
              <div className="flex flex-col gap-1.5">
                <span className="etiqueta">Embarcación intervenida <span className="text-error">*</span></span>
                <Segmentado etiqueta="Embarcación" valor={idEmb} onCambio={(v) => { setEmbarcacionId(v); setMotorId('') }}
                  opciones={(embarcaciones.data ?? []).map((x) => ({ valor: x.id, etiqueta: EMBARCACION[x.nombre] }))} />
                {e('embarcacion') && <p role="alert" className="m-0 text-[12px] text-error">{e('embarcacion')}</p>}
              </div>
              <Campo etiqueta="Fecha de la intervención" obligatorio error={e('fecha')}>
                {(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={fechaIntervencion} onChange={(ev) => setFechaIntervencion(ev.target.value)} />}
              </Campo>
              <Campo etiqueta="Responsable técnico" obligatorio ayuda="Operador de embarcación asignado" error={e('responsable')}>
                {(p) => (
                  <Selector {...p} vacio={(esSupervisor ? personal.isLoading : disponibles.isLoading) ? 'Cargando…' : 'Seleccione…'} value={responsableId} onChange={(ev) => setResponsableId(ev.target.value)}
                    opciones={responsables.map((r) => ({ valor: r.id, etiqueta: `${r.nombres} ${r.apellidos}` }))} />
                )}
              </Campo>
            </div>
            {enCurso && <Alerta tono="aviso">{EMBARCACION[enCurso.embarcacion]} tiene un viaje en curso. El backend no permite registrar el mantenimiento hasta que termine.</Alerta>}
            <div className="flex flex-col gap-1.5">
              <span className="etiqueta">Tipo de mantenimiento <span className="text-error">*</span></span>
              <div className="grid gap-3 sm:grid-cols-3" role="radiogroup" aria-label="Tipo de mantenimiento">
                {(Object.keys(TIPO_MANTENIMIENTO) as TipoMantenimiento[]).map((t) => (
                  <OpcionTarjeta key={t} seleccionada={tipo === t} onClick={() => setTipo(t)} titulo={TIPO_MANTENIMIENTO[t]} descripcion={TIPO_MANTENIMIENTO_AYUDA[t]} />
                ))}
              </div>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Campo etiqueta="Sistema intervenido" obligatorio error={e('sistema')}>
                {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(SISTEMA_INTERVENIDO)} value={sistema} onChange={(ev) => setSistema(ev.target.value as SistemaIntervenido)} />}
              </Campo>
              {sistema === 'MOTOR' && (
                <Campo etiqueta="Motor" obligatorio error={e('motor')}>
                  {(p) => <Selector {...p} vacio="Seleccione…" value={motorId} onChange={(ev) => setMotorId(ev.target.value)} opciones={(motores.data ?? []).map((x) => ({ valor: x.id, etiqueta: `${x.marca} ${x.modelo} · ${x.serial}` }))} />}
                </Campo>
              )}
            </div>
          </Tarjeta>

          <Tarjeta>
            <PasoTarjeta numero={2} titulo="Actividades ejecutadas" subtitulo="Describe lo realizado y registra los elementos utilizados" />
            <Campo etiqueta="Descripción de actividades" obligatorio error={e('descripcion')}>
              {(p) => <AreaTexto {...p} maxLength={4000} rows={4} value={descripcion} onChange={(ev) => setDescripcion(ev.target.value)} />}
            </Campo>
            <div className="grid gap-4 sm:grid-cols-2">
              <Campo etiqueta="Ejecutado por" opcional ayuda="Taller o técnico externo">{(p) => <Entrada {...p} maxLength={150} value={ejecutadoPor} onChange={(ev) => setEjecutadoPor(ev.target.value)} />}</Campo>
              <Campo etiqueta="Costo de mano de obra" opcional>{(p) => <Entrada {...p} type="number" min={0} step="1" sufijo="COP" value={manoObra} onChange={(ev) => setManoObra(ev.target.value)} />}</Campo>
            </div>
            <div className="flex flex-col gap-2">
              <span className="etiqueta">Repuestos e insumos</span>
              {repuestos.length === 0 && <p className="m-0 text-[13px] text-texto-2">No aplica / ninguno.</p>}
              {repuestos.map((r, i) => (
                <div key={i} className="grid gap-2 sm:grid-cols-[1.4fr_1.2fr_0.6fr_0.6fr_0.8fr_auto] items-start p-3 rounded-[12px] bg-fondo">
                  <Selector aria-label="Repuesto o insumo" vacio="Elemento…" opciones={opciones(REPUESTO)} value={r.item} onChange={(ev) => setRepuestos((l) => l.map((x, j) => (j === i ? { ...x, item: ev.target.value as RepuestoInsumo } : x)))} />
                  <Entrada aria-label="Descripción" placeholder={r.item === 'OTRO' ? 'Descripción (obligatoria)' : 'Descripción'} maxLength={200} value={r.descripcion} onChange={(ev) => setRepuestos((l) => l.map((x, j) => (j === i ? { ...x, descripcion: ev.target.value } : x)))} />
                  <Entrada aria-label="Cantidad" type="number" min={0} step="0.01" placeholder="Cant." value={r.cantidad} onChange={(ev) => setRepuestos((l) => l.map((x, j) => (j === i ? { ...x, cantidad: ev.target.value } : x)))} />
                  <Entrada aria-label="Unidad" placeholder="und" maxLength={20} value={r.unidad} onChange={(ev) => setRepuestos((l) => l.map((x, j) => (j === i ? { ...x, unidad: ev.target.value } : x)))} />
                  <Entrada aria-label="Costo" type="number" min={0} placeholder="Costo" value={r.costo} onChange={(ev) => setRepuestos((l) => l.map((x, j) => (j === i ? { ...x, costo: ev.target.value } : x)))} />
                  <Boton variante="fantasma" soloIcono aria-label="Quitar repuesto" onClick={() => setRepuestos((l) => l.filter((_, j) => j !== i))}><Trash2 className="size-4" /></Boton>
                  {e(`rep-${i}`) && <p role="alert" className="m-0 text-[12px] text-error sm:col-span-6">{e(`rep-${i}`)}</p>}
                </div>
              ))}
              <div className="flex flex-wrap items-center justify-between gap-2">
                <Boton variante="suave" pequeno icono={<Plus className="size-3.5" />} onClick={() => setRepuestos((l) => [...l, { item: '', descripcion: '', cantidad: '', unidad: '', costo: '' }])}>Agregar repuesto o insumo</Boton>
                {repuestos.length > 0 && <span className="text-[13px] text-texto-2">Costo de repuestos: <b className="text-negro">{pesos(costoRepuestos)}</b></span>}
              </div>
            </div>
          </Tarjeta>

          <Tarjeta>
            <PasoTarjeta numero={3} titulo="Hallazgos" subtitulo="Novedades mecánicas identificadas durante la intervención" />
            <NovedadesMecanicas valor={novedades} onCambio={setNovedades} errores={intentado ? errNov : undefined} />
          </Tarjeta>

          <Tarjeta>
            <PasoTarjeta numero={4} titulo="Recomendaciones y próxima intervención" subtitulo="Alertas para futuras intervenciones" />
            {recomendaciones.map((r, i) => (
              <div key={i} className="grid gap-2 sm:grid-cols-[0.6fr_2fr_auto] items-start p-3 rounded-[12px] bg-fondo">
                <Selector aria-label="Tipo de recomendación" vacio="Tipo…" opciones={opciones(TIPO_RECOMENDACION)} value={r.tipo} onChange={(ev) => setRecomendaciones((l) => l.map((x, j) => (j === i ? { ...x, tipo: ev.target.value as TipoRecomendacion } : x)))} />
                <Entrada aria-label="Recomendación" maxLength={2000} value={r.descripcion} onChange={(ev) => setRecomendaciones((l) => l.map((x, j) => (j === i ? { ...x, descripcion: ev.target.value } : x)))} placeholder="Recomendación o alerta" />
                <Boton variante="fantasma" soloIcono aria-label="Quitar recomendación" onClick={() => setRecomendaciones((l) => l.filter((_, j) => j !== i))}><Trash2 className="size-4" /></Boton>
                {e(`rec-${i}`) && <p role="alert" className="m-0 text-[12px] text-error sm:col-span-3">{e(`rec-${i}`)}</p>}
              </div>
            ))}
            <div><Boton variante="suave" pequeno icono={<Plus className="size-3.5" />} onClick={() => setRecomendaciones((l) => [...l, { tipo: '', descripcion: '' }])}>Agregar recomendación</Boton></div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Campo etiqueta="Fecha estimada de la próxima intervención" opcional error={e('proxima')}>
                {(p) => <Entrada {...p} type="datetime-local" min={ahoraLocal()} value={proxima} onChange={(ev) => setProxima(ev.target.value)} />}
              </Campo>
              <Campo etiqueta="Condición para intervenir" opcional>{(p) => <Entrada {...p} maxLength={300} value={condicionProxima} onChange={(ev) => setCondicionProxima(ev.target.value)} placeholder="Llegada del repuesto, cada 100 h…" />}</Campo>
            </div>
          </Tarjeta>

          <Tarjeta>
            <PasoTarjeta numero={5} titulo="Soportes adjuntos" subtitulo="Fotografías, órdenes de trabajo o facturas" />
            <CargaArchivos carpeta="mantenimiento" onCambio={setSoportes} />
          </Tarjeta>

          <Tarjeta className="!flex-row flex-wrap items-center justify-end gap-3 sm:sticky sm:bottom-3 z-10 shadow-flotante">
            {m.error && <div className="basis-full"><ErrorFormulario error={m.error} /></div>}
            {intentado && Object.keys(errores).length > 0 && <span className="text-[12.5px] text-error font-bold mr-auto">Revise {Object.keys(errores).length} campo(s)</span>}
            <Boton variante="fantasma" onClick={() => navegar(-1)}>Cancelar</Boton>
            <Boton variante="institucional" icono={<Check className="size-4" />} cargando={m.isPending} onClick={guardar}>Guardar mantenimiento</Boton>
          </Tarjeta>
        </div>

        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo={`Hoja de vida · ${seleccionada ? EMBARCACION[seleccionada.nombre] : ''}`} icono={<Ship />} />
            {hv.isLoading ? <Cargando alto={80} /> : hv.data && (
              <dl className="m-0 grid grid-cols-[1fr_auto] gap-y-2.5 text-[13px]">
                <dt className="text-texto-2">Estado</dt><dd className="m-0 text-right">{seleccionada && <EstadoEmbarcacionInsignia estado={seleccionada.estado} />}</dd>
                <dt className="text-texto-2">Última intervención</dt><dd className="m-0 text-right font-bold">{fecha(hv.data.ultimaIntervencion)}</dd>
                <dt className="text-texto-2">Preventivos / correctivos</dt><dd className="m-0 text-right font-bold">{hv.data.preventivos} / {hv.data.correctivos}</dd>
                <dt className="text-texto-2">Próxima intervención</dt><dd className="m-0 text-right font-bold">{fecha(hv.data.proximaIntervencion)}</dd>
                {motores.data?.map((x) => (<Fragment key={x.id}><dt className="text-texto-2">Horómetro {x.serial}</dt><dd className="m-0 text-right font-bold">{decimal(x.horometroActual)} h</dd></Fragment>))}
              </dl>
            )}
          </Tarjeta>
          <Tarjeta>
            <EncabezadoTarjeta titulo="Recuerda" icono={<ShieldCheck />} tono="exito" />
            <p className="m-0 text-[13px] text-texto-2">Si una necesidad de mantenimiento puede afectar la seguridad o la continuidad, infórmala de inmediato al supervisor del contrato desde la hoja de vida o el detalle del viaje.</p>
          </Tarjeta>
        </div>
      </div>
    </Pagina>
  )
}
