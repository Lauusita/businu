import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { CircleCheck, Download, FileChartColumn, FileSpreadsheet, Lock, Save, Send } from 'lucide-react'
import { Logo } from '../../components/brand/Marca'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Selector } from '../../components/ui/Campo'
import { cn } from '../../components/ui/cn'
import { Insignia } from '../../components/ui/Insignia'
import { Confirmacion } from '../../components/ui/Modal'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { informesService } from '../../services/modules/informes.service'
import type { InformeMensualResponse } from '../../types/api'
import { CONJUNTO_EXPORTACION, ESTADO_INFORME, opciones, type ConjuntoExportacion } from '../../types/enums'
import { guardarArchivo } from '../../utils/descargas'
import { decimal, entero, fechaHora, mesActual, mesLargo, sumarMes } from '../../utils/formato'
import { DialogoExportar } from './BotonExportar'

/** Plazo de ejecución: 5 meses, un informe por mes (numeral 4.7). Se listan el mes actual y los cuatro anteriores. */
const MESES = Array.from({ length: 5 }, (_, i) => sumarMes(mesActual(), -i))
type Texto = 'resumenActividades' | 'conclusiones' | 'recomendaciones' | 'otrosDocumentos'
const TEXTOS: { clave: Texto; titulo: string }[] = [
  { clave: 'resumenActividades', titulo: 'Resumen de las actividades desarrolladas' },
  { clave: 'conclusiones', titulo: 'Conclusiones del periodo' },
  { clave: 'recomendaciones', titulo: 'Recomendaciones técnicas y operativas' },
  { clave: 'otrosDocumentos', titulo: 'Otros documentos anexos' },
]

/** Plantilla 22 · Informes mensuales de operación (GET/PUT /informes/mensual/{mes}) y exportaciones. */
export function InformesPage() {
  const [mes, setMes] = useState(mesActual())
  const [otroMes, setOtroMes] = useState('')
  const q = useQuery({ queryKey: ['informe', mes], queryFn: () => informesService.obtener(mes) })
  return (
    <Pagina titulo="Informes mensuales de operación" migas={[{ etiqueta: 'Informes y reportes' }]}
      descripcion="Consulta, redacta, cierra y entrega el informe mensual consolidado para el supervisor del contrato (numeral 4.7).">
      <div className="grid xl:grid-cols-[260px_minmax(0,1.5fr)_minmax(0,1fr)] gap-5 items-start">
        <Tarjeta>
          <EncabezadoTarjeta titulo="Informes de la operación piloto" icono={<FileChartColumn />} />
          <ul className="m-0 p-0 list-none flex flex-col gap-2">
            {MESES.map((m) => (
              <li key={m}>
                <button type="button" onClick={() => setMes(m)} aria-pressed={mes === m}
                  className={cn('w-full text-left rounded-[14px] border-2 px-3.5 py-3 cursor-pointer capitalize', mes === m ? 'border-azul bg-azul-suave' : 'border-borde hover:border-azul-claro')}>
                  <b className="text-[14px]">{mesLargo(m)}</b>
                  {m === mesActual() && <span className="block text-[12px] text-texto-2 normal-case">Mes en curso</span>}
                </button>
              </li>
            ))}
          </ul>
          <Campo etiqueta="Otro mes">{(p) => <Entrada {...p} type="month" max={mesActual()} value={otroMes} onChange={(e) => { setOtroMes(e.target.value); if (e.target.value) setMes(e.target.value) }} />}</Campo>
          <Alerta tono="dorado">Plazo de ejecución: 5 meses. Un informe mensual al supervisor del contrato (numeral 4.7).</Alerta>
        </Tarjeta>
        {q.isLoading ? <Tarjeta><Cargando alto={320} /></Tarjeta> : q.error || !q.data ? <Tarjeta><ErrorCarga error={q.error} onReintentar={() => q.refetch()} /></Tarjeta> : <Informe key={mes + q.data.estado} inf={q.data} />}
        <div className="flex flex-col gap-5 min-w-0">
          {q.data && <VistaPrevia inf={q.data} />}
          <Exportaciones />
        </div>
      </div>
    </Pagina>
  )
}

function Informe({ inf }: { inf: InformeMensualResponse }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const editable = inf.estado === 'BORRADOR'
  const [textos, setTextos] = useState<Record<Texto, string>>({
    resumenActividades: inf.resumenActividades ?? '', conclusiones: inf.conclusiones ?? '', recomendaciones: inf.recomendaciones ?? '', otrosDocumentos: inf.otrosDocumentos ?? '',
  })
  const [confirmar, setConfirmar] = useState<'cerrar' | 'entregar' | null>(null)
  const [descargando, setDescargando] = useState(false)
  const actualizar = (r: InformeMensualResponse, msg: string) => {
    queryClient.setQueryData(['informe', inf.periodo], r)
    avisar(msg)
  }
  const guardar = useMutation({ mutationFn: () => informesService.redactar(inf.periodo, textos), onSuccess: (r) => actualizar(r, 'Redacción guardada.') })
  const cerrar = useMutation({ mutationFn: () => informesService.cerrar(inf.periodo), onSuccess: (r) => { actualizar(r, 'Informe cerrado: las cifras quedaron congeladas.'); setConfirmar(null) } })
  const entregar = useMutation({ mutationFn: () => informesService.entregar(inf.periodo), onSuccess: (r) => { actualizar(r, 'Informe marcado como entregado.'); setConfirmar(null) } })
  const sinGuardar = TEXTOS.some((t) => (inf[t.clave] ?? '') !== textos[t.clave])

  const d = inf.datos
  const secciones = d ? [
    { t: 'Registro y consolidado de viajes', s: `${entero(d.viajes.total)} bitácoras`, ok: d.viajes.total > 0 },
    { t: 'Usuarios transportados y caracterización', s: `${entero(d.demanda.totalUsuarios)} usuarios`, ok: d.demanda.totalUsuarios > 0 },
    { t: 'Combustible y consumo', s: `${entero(d.combustible.abastecimientos)} abastecimientos · ${decimal(d.combustible.galones)} gal`, ok: d.combustible.abastecimientos > 0 },
    { t: 'Estado y novedades de las embarcaciones', s: `${d.embarcaciones.length} embarcaciones`, ok: d.embarcaciones.length > 0 },
    { t: 'Alertas o necesidades de mantenimiento', s: `${d.alertasMantenimiento.length} alerta(s)`, ok: true },
    { t: 'Estado de embarcaderos y ayudas a la navegación', s: `${entero(d.infraestructura.registrosDelMes)} registros · ${d.infraestructura.pendientesDeComunicar} sin comunicar`, ok: d.infraestructura.pendientesDeComunicar === 0, revisar: d.infraestructura.pendientesDeComunicar > 0 },
    { t: 'Soportes de seguridad social', s: `${d.seguridadSocial.conPlanilla} de ${d.seguridadSocial.personalActivo} con planilla`, ok: d.seguridadSocial.sinPlanilla.length === 0, revisar: d.seguridadSocial.sinPlanilla.length > 0 },
    { t: 'Peticiones, quejas y reclamos', s: `${entero(d.pqrs.total)} solicitudes`, ok: true },
  ] : []
  const redactadas = TEXTOS.filter((t) => textos[t.clave].trim()).length
  const listas = secciones.filter((s) => s.ok).length + redactadas
  const totalSecciones = secciones.length + TEXTOS.length

  const pdf = async () => {
    setDescargando(true)
    try {
      const r = await informesService.pdf(inf.periodo)
      guardarArchivo(r.blob, r.nombre ?? `informe-mensual-${inf.periodo}.pdf`)
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo generar el PDF.', 'error')
    } finally {
      setDescargando(false)
    }
  }

  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo={<span className="capitalize">Contenido del informe · {mesLargo(inf.periodo)}</span>} subtitulo="Secciones mínimas exigidas por el anexo técnico"
        acciones={<Insignia tono={inf.estado === 'BORRADOR' ? 'info' : inf.estado === 'CERRADO' ? 'gold' : 'ok'}>{ESTADO_INFORME[inf.estado]}</Insignia>} />
      <div>
        <div className="flex justify-between text-[13px] font-bold"><span>{listas} de {totalSecciones} secciones listas</span><span className="text-azul">{Math.round((listas / totalSecciones) * 100)} %</span></div>
        <div className="h-2 mt-1.5 rounded-full bg-fondo overflow-hidden" aria-hidden><div className="h-full bg-azul rounded-full" style={{ width: `${(listas / totalSecciones) * 100}%` }} /></div>
      </div>
      <ul className="m-0 p-0 list-none flex flex-col divide-y divide-borde">
        {secciones.map((s) => (
          <li key={s.t} className="flex items-start gap-3 py-2.5">
            <CircleCheck className={cn('size-5 flex-none', s.ok ? 'text-white fill-exito' : 'text-texto-3')} aria-hidden />
            <div className="flex-1 min-w-0"><div className="text-[13.5px] font-semibold">{s.t}</div><div className="text-[12px] text-texto-2">{s.s}</div></div>
            <Insignia tono={s.ok ? 'ok' : s.revisar ? 'warn' : 'neutral'}>{s.ok ? 'Listo' : s.revisar ? 'Revisar' : 'Sin datos'}</Insignia>
          </li>
        ))}
      </ul>
      {d?.seguridadSocial.sinPlanilla.length ? <Alerta tono="aviso" titulo="Sin planilla PILA del mes">{d.seguridadSocial.sinPlanilla.join(', ')}</Alerta> : null}
      {d?.alertasMantenimiento.length ? <Alerta tono="info" titulo="Alertas de mantenimiento">{d.alertasMantenimiento.join(' · ')}</Alerta> : null}
      <div className="flex flex-col gap-4 border-t border-borde pt-4">
        <span className="font-titulo text-[15px] text-noche">Redacción del supervisor</span>
        {TEXTOS.map((t) => (
          <Campo key={t.clave} etiqueta={t.titulo} opcional={t.clave === 'otrosDocumentos'}>
            {(p) => <AreaTexto {...p} rows={t.clave === 'resumenActividades' ? 5 : 3} maxLength={20000} readOnly={!editable} value={textos[t.clave]} onChange={(e) => setTextos((x) => ({ ...x, [t.clave]: e.target.value }))} />}
          </Campo>
        ))}
      </div>
      {inf.fechaCierre && <p className="m-0 text-[12.5px] text-texto-2">Cerrado el {fechaHora(inf.fechaCierre)}{inf.cerradoPor && ` por ${inf.cerradoPor}`}. Las cifras quedaron congeladas{inf.huellaDatos && ` (huella ${inf.huellaDatos.slice(0, 12)}…)`}.</p>}
      {inf.fechaEntrega && <p className="m-0 text-[12.5px] text-texto-2">Entregado el {fechaHora(inf.fechaEntrega)}{inf.entregadoPor && ` por ${inf.entregadoPor}`}.</p>}
      <ErrorFormulario error={guardar.error} />
      <div className="flex flex-wrap justify-end gap-2.5">
        <Boton variante="secundario" icono={<Download className="size-4" />} cargando={descargando} onClick={pdf}>Descargar PDF</Boton>
        {editable && <Boton variante="suave" icono={<Save className="size-4" />} disabled={!sinGuardar} cargando={guardar.isPending} onClick={() => guardar.mutate()}>Guardar redacción</Boton>}
        {editable && <Boton variante="institucional" icono={<Lock className="size-4" />} disabled={sinGuardar} onClick={() => setConfirmar('cerrar')}>Cerrar informe</Boton>}
        {inf.estado === 'CERRADO' && <Boton variante="institucional" icono={<Send className="size-4" />} onClick={() => setConfirmar('entregar')}>Marcar como entregado</Boton>}
      </div>
      {editable && sinGuardar && <p className="m-0 text-right text-[12px] text-advertencia">Guarde la redacción antes de cerrar el informe.</p>}
      <Confirmacion abierto={confirmar === 'cerrar'} onCerrar={() => setConfirmar(null)} onConfirmar={() => cerrar.mutate()} cargando={cerrar.isPending} error={cerrar.error?.message}
        titulo="Cerrar informe" textoConfirmar="Cerrar informe" mensaje="Las cifras del mes quedarán congeladas tal como están ahora y la redacción ya no podrá modificarse." />
      <Confirmacion abierto={confirmar === 'entregar'} onCerrar={() => setConfirmar(null)} onConfirmar={() => entregar.mutate()} cargando={entregar.isPending} error={entregar.error?.message}
        titulo="Marcar como entregado" textoConfirmar="Confirmar entrega" mensaje="Registra que el informe fue entregado al supervisor del contrato. El envío del documento se hace por fuera del sistema." />
    </Tarjeta>
  )
}

function VistaPrevia({ inf }: { inf: InformeMensualResponse }) {
  const d = inf.datos
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Vista previa" />
      <div className="rounded-[14px] overflow-hidden border border-borde">
        <div className="bg-noche px-4 py-4">
          <Logo tamano={22} />
          <div className="font-titulo text-[17px] text-white mt-1">Informe mensual de operación</div>
          <div className="text-[12px] text-white/70 capitalize">{mesLargo(inf.periodo)} · Operación piloto</div>
        </div>
        {d && (
          <div className="grid grid-cols-2 gap-3 p-4">
            <div><div className="text-[11.5px] text-texto-2">Viajes realizados</div><b>{entero(d.viajes.total)}</b></div>
            <div><div className="text-[11.5px] text-texto-2">Pasajeros</div><b>{entero(d.demanda.totalUsuarios)}</b></div>
            <div><div className="text-[11.5px] text-texto-2">Combustible</div><b>{decimal(d.combustible.galones)} gal</b></div>
            <div><div className="text-[11.5px] text-texto-2">Horas de navegación</div><b>{decimal(d.viajes.horasNavegacion)} h</b></div>
            <div><div className="text-[11.5px] text-texto-2">PQRS</div><b>{entero(d.pqrs.total)}</b></div>
            <div><div className="text-[11.5px] text-texto-2">Registros de infraestructura</div><b>{entero(d.infraestructura.registrosDelMes)}</b></div>
          </div>
        )}
      </div>
      {d && <p className="m-0 text-[12px] text-texto-2">Datos generados el {fechaHora(d.generadoEn)}{inf.estado === 'BORRADOR' ? ' (en vivo mientras sea borrador)' : ' (congelados al cerrar)'}.</p>}
    </Tarjeta>
  )
}

function Exportaciones() {
  const [conjunto, setConjunto] = useState<ConjuntoExportacion>('COMPLETO')
  const [abierto, setAbierto] = useState(false)
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Exportaciones" subtitulo="Registros en Excel o CSV (numeral 4.8)" icono={<FileSpreadsheet />} />
      <Selector aria-label="Conjunto de datos" opciones={opciones(CONJUNTO_EXPORTACION)} value={conjunto} onChange={(e) => setConjunto(e.target.value as ConjuntoExportacion)} />
      <Boton variante="primario" icono={<Download className="size-4" />} onClick={() => setAbierto(true)}>Exportar</Boton>
      <DialogoExportar abierto={abierto} onCerrar={() => setAbierto(false)} conjunto={conjunto} />
    </Tarjeta>
  )
}
