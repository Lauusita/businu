import { keepPreviousData, useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { ExternalLink, MessageSquare, Search, Send, ShieldAlert, ThumbsUp, TriangleAlert } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, ErrorFormulario, Vacio } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Paginacion } from '../../components/tables/Paginacion'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Selector } from '../../components/ui/Campo'
import { cn } from '../../components/ui/cn'
import { Insignia } from '../../components/ui/Insignia'
import { EstadoPqrsInsignia } from '../../components/ui/InsigniasEstado'
import { Kpi } from '../../components/ui/Kpi'
import { Pestanas } from '../../components/ui/Selecciones'
import { Dato, Tarjeta } from '../../components/ui/Tarjeta'
import { pqrsService } from '../../services/modules/pqrs.service'
import type { PqrsResponse } from '../../types/api'
import { EMBARCADERO, ESTADO_PQRS, TIPO_PQRS, opciones, type EstadoPqrs, type TipoPqrs } from '../../types/enums'
import { abrirEnPestana } from '../../utils/descargas'
import { diaMes, fechaHora, fechaLocal } from '../../utils/formato'
import { BotonExportar } from '../informes/BotonExportar'

const TONO_TIPO: Record<TipoPqrs, 'warn' | 'info' | 'err' | 'ok'> = { PETICION: 'info', QUEJA: 'warn', RECLAMO: 'err', SUGERENCIA: 'ok', FELICITACION: 'ok' }
type Pestana = 'TODAS' | EstadoPqrs
const TAMANO = 8

/** Plantilla 20 · Atención al usuario: gestión de PQRS (GET/POST /pqrs/gestion, numeral 4.7). */
export function AtencionUsuarioPage() {
  const [pestana, setPestana] = useState<Pestana>('RECIBIDA')
  const [tipo, setTipo] = useState<TipoPqrs | ''>('')
  const [buscar, setBuscar] = useState('')
  const [pagina, setPagina] = useState(0)
  const [seleccion, setSeleccion] = useState<string | null>(null)

  const filtro = { estado: pestana === 'TODAS' ? undefined : pestana, tipo: tipo || undefined }
  const lista = useQuery({ queryKey: ['pqrs', filtro, pagina], queryFn: () => pqrsService.listar(filtro, { page: pagina, size: TAMANO }), placeholderData: keepPreviousData })
  const porTipo = useQueries({ queries: (Object.keys(TIPO_PQRS) as TipoPqrs[]).map((t) => ({ queryKey: ['pqrs', 'conteo', t], queryFn: () => pqrsService.listar({ tipo: t }, { size: 1 }) })) })
  const porEstado = useQueries({ queries: (Object.keys(ESTADO_PQRS) as EstadoPqrs[]).map((e) => ({ queryKey: ['pqrs', 'conteo-estado', e], queryFn: () => pqrsService.listar({ estado: e }, { size: 1 }) })) })
  const total = (i: number, q = porTipo) => q[i]?.data?.page.totalElements

  const filas = (lista.data?.content ?? []).filter((p) => !buscar || `${p.radicado} ${p.descripcion}`.toLowerCase().includes(buscar.toLowerCase()))
  // Sin selección explícita se muestra la primera solicitud de la lista
  const activa = seleccion ?? filas[0]?.id ?? null

  return (
    <Pagina
      titulo="Atención al usuario"
      migas={[{ etiqueta: 'Atención al usuario' }, { etiqueta: 'PQRS' }]}
      descripcion="Peticiones, quejas, reclamos, sugerencias y felicitaciones de los usuarios."
      acciones={<><BotonExportar conjunto="PQRS" /><Boton icono={<ExternalLink className="size-4" />} onClick={() => abrirEnPestana(pqrsService.urlFormularioPublico)}>Formulario público (QR)</Boton></>}
    >
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <Kpi icono={<MessageSquare />} cifra={total(0) ?? '—'} etiqueta="Peticiones" cargando={porTipo[0]?.isLoading} />
        <Kpi icono={<TriangleAlert />} tono="dorado" cifra={total(1) ?? '—'} etiqueta="Quejas" cargando={porTipo[1]?.isLoading} />
        <Kpi icono={<ShieldAlert />} tono="error" cifra={total(2) ?? '—'} etiqueta="Reclamos" cargando={porTipo[2]?.isLoading} />
        <Kpi icono={<ThumbsUp />} tono="exito" cifra={total(3) !== undefined && total(4) !== undefined ? total(3)! + total(4)! : '—'} etiqueta="Sugerencias y felicitaciones" cargando={porTipo[3]?.isLoading} />
      </div>
      <div className="grid xl:grid-cols-[minmax(0,1fr)_minmax(0,1.5fr)] gap-5 items-start">
        <Tarjeta>
          <Entrada aria-label="Buscar radicado o descripción" placeholder="Buscar radicado o descripción" icono={<Search />} value={buscar} onChange={(e) => setBuscar(e.target.value)} />
          <Selector aria-label="Tipo" vacio="Todos los tipos" opciones={opciones(TIPO_PQRS)} value={tipo} onChange={(e) => { setTipo(e.target.value as TipoPqrs); setPagina(0); setSeleccion(null) }} />
          <Pestanas valor={pestana} onCambio={(v) => { setPestana(v); setPagina(0); setSeleccion(null) }}
            opciones={[
              ...(Object.keys(ESTADO_PQRS) as EstadoPqrs[]).map((e, i) => ({ valor: e as Pestana, etiqueta: `${ESTADO_PQRS[e]}${total(i, porEstado) !== undefined ? ` (${total(i, porEstado)})` : ''}` })),
              { valor: 'TODAS', etiqueta: 'Todas' },
            ]} />
          {lista.isLoading ? <Cargando /> : lista.error ? <ErrorCarga error={lista.error} onReintentar={() => lista.refetch()} /> : !filas.length ? <Vacio titulo="Sin solicitudes" /> : (
            <ul className="m-0 p-0 list-none flex flex-col gap-2.5">
              {filas.map((p) => (
                <li key={p.id}>
                  <button type="button" onClick={() => setSeleccion(p.id)} aria-pressed={activa === p.id}
                    className={cn('w-full text-left rounded-[14px] border-2 px-3.5 py-3 cursor-pointer transition-colors', activa === p.id ? 'border-azul bg-azul-suave' : 'border-borde bg-white hover:border-azul-claro')}>
                    <div className="flex items-center gap-2">
                      <b className="text-azul text-[13px]">{p.radicado}</b>
                      <Insignia tono={TONO_TIPO[p.tipo]}>{TIPO_PQRS[p.tipo]}</Insignia>
                      <span className="flex-1" />
                      <EstadoPqrsInsignia estado={p.estado} />
                    </div>
                    <div className="font-bold text-[13.5px] mt-1 line-clamp-1">{p.descripcion}</div>
                    <div className="text-[12px] text-texto-2">{diaMes(p.fechaRadicacion)}{p.embarcadero && ` · ${EMBARCADERO[p.embarcadero]}`}</div>
                  </button>
                </li>
              ))}
            </ul>
          )}
          {lista.data && <Paginacion pagina={pagina} totalPaginas={lista.data.page.totalPages} totalElementos={lista.data.page.totalElements} tamano={TAMANO} onCambio={setPagina} nombre="solicitudes" />}
        </Tarjeta>
        {activa ? <DetallePqrs id={activa} /> : <Tarjeta><Vacio titulo="Seleccione una solicitud" /></Tarjeta>}
      </div>
    </Pagina>
  )
}

function DetallePqrs({ id }: { id: string }) {
  const q = useQuery({ queryKey: ['pqrs', 'detalle', id], queryFn: () => pqrsService.obtener(id) })
  if (q.isLoading) return <Tarjeta><Cargando /></Tarjeta>
  if (q.error || !q.data) return <Tarjeta><ErrorCarga error={q.error} onReintentar={() => q.refetch()} /></Tarjeta>
  return <FichaPqrs key={q.data.id + q.data.estado} p={q.data} />
}

function FichaPqrs({ p }: { p: PqrsResponse }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [estado, setEstado] = useState<EstadoPqrs>(p.estado === 'RECIBIDA' ? 'EN_GESTION' : p.estado)
  const [respuesta, setRespuesta] = useState(p.respuesta ?? '')
  const [entidad, setEntidad] = useState(p.entidadTraslado ?? '')
  const [nota, setNota] = useState(p.observacionInterna ?? '')
  const [intentado, setIntentado] = useState(false)
  const errores: Record<string, string> = {}
  if (estado === 'RESPONDIDA' && !respuesta.trim()) errores.respuesta = 'Escriba la respuesta al usuario'
  if (estado === 'TRASLADADA' && !entidad.trim()) errores.entidad = 'Indique la entidad a la que se traslada'
  const m = useMutation({
    mutationFn: () => pqrsService.gestionar(p.id, { estado, respuesta: respuesta.trim() || null, entidadTraslado: entidad.trim() || null, observacionInterna: nota.trim() || null }),
    onSuccess: (r) => {
      queryClient.setQueryData(['pqrs', 'detalle', p.id], r)
      queryClient.invalidateQueries({ queryKey: ['pqrs'] })
      avisar(`Solicitud ${r.radicado} actualizada: ${ESTADO_PQRS[r.estado].toLowerCase()}.`)
    },
  })
  const cerrada = p.estado === 'CERRADA'
  return (
    <Tarjeta>
      <div className="flex flex-wrap items-center gap-2">
        <b className="text-azul">{p.radicado}</b>
        <Insignia tono={TONO_TIPO[p.tipo]}>{TIPO_PQRS[p.tipo]}</Insignia>
        <EstadoPqrsInsignia estado={p.estado} />
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <Dato etiqueta="Fecha de recepción">{fechaHora(p.fechaRadicacion)}</Dato>
        <Dato etiqueta="Embarcadero">{p.embarcadero ? EMBARCADERO[p.embarcadero] : 'No indica'}</Dato>
        <Dato etiqueta="Fecha del hecho">{p.fechaHecho ? fechaLocal(p.fechaHecho) : 'No indica'}</Dato>
        <Dato etiqueta="Gestionado por">{p.gestionadoPor ?? '—'}</Dato>
      </div>
      <div>
        <div className="etiqueta mb-1.5">Descripción del usuario</div>
        <blockquote className="m-0 rounded-[12px] bg-fondo px-4 py-3 text-[14px] italic">“{p.descripcion}”</blockquote>
      </div>
      {p.autorizaDatos ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          <Dato etiqueta="Nombre">{p.nombre ?? '—'}</Dato>
          <Dato etiqueta="Correo">{p.correo ?? '—'}</Dato>
          <Dato etiqueta="Teléfono">{p.telefono ?? '—'}</Dato>
        </div>
      ) : <p className="m-0 text-[12.5px] text-texto-2">El usuario no autorizó el tratamiento de datos de contacto (Ley 1581 de 2012).</p>}
      {p.respuesta && <Dato etiqueta={`Respuesta${p.fechaRespuesta ? ` · ${fechaHora(p.fechaRespuesta)}` : ''}`}><span className="font-normal">{p.respuesta}</span></Dato>}
      {p.entidadTraslado && <Dato etiqueta="Trasladada a">{p.entidadTraslado}</Dato>}
      {!cerrada && (
        <div className="border-t border-borde pt-4 flex flex-col gap-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo etiqueta="Nuevo estado" obligatorio>
              {(x) => <Selector {...x} opciones={opciones(ESTADO_PQRS).filter((o) => o.valor !== 'RECIBIDA')} value={estado} onChange={(e) => setEstado(e.target.value as EstadoPqrs)} />}
            </Campo>
            {estado === 'TRASLADADA' && (
              <Campo etiqueta="Entidad a la que se traslada" obligatorio error={intentado ? errores.entidad : undefined}>{(x) => <Entrada {...x} maxLength={200} value={entidad} onChange={(e) => setEntidad(e.target.value)} />}</Campo>
            )}
          </div>
          <Campo etiqueta="Respuesta al usuario" obligatorio={estado === 'RESPONDIDA'} opcional={estado !== 'RESPONDIDA'} error={intentado ? errores.respuesta : undefined}
            ayuda={p.autorizaDatos && p.correo ? 'Se registra en el sistema; el envío al correo del usuario no está disponible en el backend.' : undefined}>
            {(x) => <AreaTexto {...x} maxLength={5000} rows={4} value={respuesta} onChange={(e) => setRespuesta(e.target.value)} placeholder="Escribe la respuesta dentro del ámbito de competencia del operador…" />}
          </Campo>
          <Campo etiqueta="Nota interna" opcional>{(x) => <AreaTexto {...x} maxLength={2000} value={nota} onChange={(e) => setNota(e.target.value)} />}</Campo>
          <ErrorFormulario error={m.error} />
          <Boton variante="institucional" className="self-end" icono={<Send className="size-4" />} cargando={m.isPending}
            onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>
            Guardar gestión
          </Boton>
        </div>
      )}
    </Tarjeta>
  )
}
