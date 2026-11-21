import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Eye, FileText, Megaphone } from 'lucide-react'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Paginacion } from '../../components/tables/Paginacion'
import { Tabla } from '../../components/tables/Tabla'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Selector } from '../../components/ui/Campo'
import { Insignia } from '../../components/ui/Insignia'
import { EstadoActivoInsignia, SeguimientoInsignia, SeveridadInsignia } from '../../components/ui/InsigniasEstado'
import { Modal } from '../../components/ui/Modal'
import { Dato } from '../../components/ui/Tarjeta'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import type { FiltroRegistrosInfra, RegistroInfraResponse } from '../../types/api'
import {
  ASPECTO_AFECTADO, COMPONENTE, EMBARCADERO, ENTIDAD_COMPETENTE, MEDIO_COMUNICACION, TIPO_REGISTRO_INFRA, opciones,
  type MedioComunicacion,
} from '../../types/enums'
import { ahoraLocal, bytes, fecha, fechaHora, instanteSiCambio } from '../../utils/formato'

/** Tabla paginada de registros de infraestructura (GET /infraestructura/registros). */
export function TablaRegistrosInfra({ filtro = {}, tamano = 8, minimo = 860 }: { filtro?: FiltroRegistrosInfra; tamano?: number; minimo?: number }) {
  const [pagina, setPagina] = useState(0)
  const [abierto, setAbierto] = useState<RegistroInfraResponse | null>(null)
  const q = useQuery({
    queryKey: ['infraestructura', 'registros', filtro, pagina, tamano],
    queryFn: () => infraestructuraService.registros(filtro, { page: pagina, size: tamano }),
    placeholderData: keepPreviousData,
  })
  return (
    <>
      <Tabla
        columnas={[
          { clave: 'fecha', titulo: 'Fecha', celda: (r) => fecha(r.fechaHora) },
          { clave: 'activo', titulo: 'Activo', celda: (r) => (r.embarcadero ? EMBARCADERO[r.embarcadero] : <b>{r.ayuda?.codigo}</b>) },
          { clave: 'tipo', titulo: 'Tipo', celda: (r) => TIPO_REGISTRO_INFRA[r.tipo] },
          { clave: 'comp', titulo: 'Componente', celda: (r) => COMPONENTE[r.componente].etiqueta },
          { clave: 'cond', titulo: 'Condición', celda: (r) => <EstadoActivoInsignia estado={r.estadoObservado} /> },
          { clave: 'seg', titulo: 'Seguimiento', celda: (r) => <span className="inline-flex items-center gap-1.5"><SeguimientoInsignia estado={r.estado} />{r.pendienteDeComunicar && <Megaphone className="size-4 text-error" aria-label="Pendiente de comunicar" />}</span> },
          { clave: 'reg', titulo: 'Registró', celda: (r) => r.registradoPor ?? '—' },
          { clave: 'ver', titulo: '', alinear: 'derecha', celda: (r) => <Boton variante="suave" pequeno soloIcono aria-label="Ver registro" onClick={() => setAbierto(r)}><Eye className="size-4" /></Boton> },
        ]}
        filas={q.data?.content}
        clave={(r) => r.id}
        cargando={q.isLoading}
        error={q.error}
        onReintentar={() => q.refetch()}
        minimo={minimo}
        pie={q.data && <Paginacion pagina={pagina} totalPaginas={q.data.page.totalPages} totalElementos={q.data.page.totalElements} tamano={tamano} onCambio={setPagina} />}
      />
      {abierto && <DetalleRegistro registro={abierto} onCerrar={() => setAbierto(null)} />}
    </>
  )
}

/** Detalle de un registro con las acciones de seguimiento: comunicar al supervisor y cerrar. */
export function DetalleRegistro({ registro, onCerrar }: { registro: RegistroInfraResponse; onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [r, setR] = useState(registro)
  const [accion, setAccion] = useState<'comunicar' | 'cerrar' | null>(null)
  const [medio, setMedio] = useState<MedioComunicacion | ''>('')
  const [referencia, setReferencia] = useState('')
  const [observacion, setObservacion] = useState('')
  const [inicial] = useState(ahoraLocal)
  const [fechaAccion, setFechaAccion] = useState(inicial)
  const listo = (x: RegistroInfraResponse, msg: string) => {
    setR(x)
    setAccion(null)
    queryClient.invalidateQueries({ queryKey: ['infraestructura'] })
    avisar(msg)
  }
  const comunicar = useMutation({
    mutationFn: () => infraestructuraService.comunicar(r.id, { medio: medio as MedioComunicacion, referencia: referencia.trim() || null, fecha: instanteSiCambio(fechaAccion, inicial) }),
    onSuccess: (x) => listo(x, 'Comunicación al supervisor registrada.'),
  })
  const cerrar = useMutation({
    mutationFn: () => infraestructuraService.cerrar(r.id, { observacion: observacion.trim(), fecha: instanteSiCambio(fechaAccion, inicial) }),
    onSuccess: (x) => listo(x, 'Registro cerrado.'),
  })
  return (
    <Modal abierto onCerrar={onCerrar} ancho="lg" titulo={`${TIPO_REGISTRO_INFRA[r.tipo]} · ${r.embarcadero ? EMBARCADERO[r.embarcadero] : r.ayuda?.codigo}`} subtitulo={fechaHora(r.fechaHora)}
      pie={r.estado !== 'CERRADO' && !accion ? (
        <>
          {r.estado === 'ABIERTO' && <Boton variante="secundario" icono={<Megaphone className="size-4" />} onClick={() => setAccion('comunicar')}>Comunicar al supervisor</Boton>}
          <Boton variante="institucional" onClick={() => setAccion('cerrar')}>Cerrar registro</Boton>
        </>
      ) : undefined}>
      <div className="flex flex-wrap gap-2">
        <EstadoActivoInsignia estado={r.estadoObservado} /><SeveridadInsignia severidad={r.severidad} /><SeguimientoInsignia estado={r.estado} />
        {r.pendienteDeComunicar && <Insignia tono="err">Sin comunicar {r.horasSinComunicar !== undefined ? `hace ${r.horasSinComunicar} h` : ''}</Insignia>}
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
        <Dato etiqueta="Componente">{COMPONENTE[r.componente].etiqueta}</Dato>
        <Dato etiqueta="Registró">{r.registradoPor ?? '—'}</Dato>
        <Dato etiqueta="Aspectos afectados">{r.aspectosAfectados?.length ? r.aspectosAfectados.map((a) => ASPECTO_AFECTADO[a]).join(', ') : 'Ninguno'}</Dato>
        <Dato etiqueta="Intervención externa">{r.requiereIntervencionExterna ? ENTIDAD_COMPETENTE[r.entidadCompetente ?? 'NO_APLICA'] : 'No'}</Dato>
      </div>
      <Dato etiqueta="Descripción"><span className="font-normal">{r.descripcion}</span></Dato>
      {r.accionesRealizadas && <Dato etiqueta="Acciones realizadas"><span className="font-normal">{r.accionesRealizadas}</span></Dato>}
      {r.comunicacion && <Alerta tono="info" titulo="Comunicado al supervisor">{MEDIO_COMUNICACION[r.comunicacion.medio]} · {fechaHora(r.comunicacion.fecha)}{r.comunicacion.referencia && ` · ${r.comunicacion.referencia}`}{r.comunicacion.comunicadoPor && ` · ${r.comunicacion.comunicadoPor}`}</Alerta>}
      {r.cierre && <Alerta tono="exito" titulo="Registro cerrado">{r.cierre.observacion} · {fechaHora(r.cierre.fecha)}{r.cierre.cerradoPor && ` · ${r.cierre.cerradoPor}`}</Alerta>}
      {!!r.evidencias?.length && (
        <div className="flex flex-col gap-2">
          <span className="etiqueta">Evidencias ({r.evidencias.length})</span>
          <ul className="m-0 p-0 list-none grid sm:grid-cols-2 gap-2">
            {r.evidencias.map((ev) => (
              <li key={ev.id} className="flex items-center gap-2.5 border border-borde rounded-[12px] px-3 py-2 text-[13px]">
                <FileText className="size-4 text-azul flex-none" aria-hidden /><span className="flex-1 min-w-0 truncate">{ev.nombreOriginal}</span><span className="text-texto-2">{bytes(ev.tamanoBytes)}</span>
              </li>
            ))}
          </ul>
          <p className="m-0 text-[12px] text-texto-2">El backend aún no expone la descarga de evidencias de infraestructura.</p>
        </div>
      )}
      {accion === 'comunicar' && (
        <div className="rounded-[14px] border border-borde p-4 flex flex-col gap-3">
          <div className="grid gap-3 sm:grid-cols-3">
            <Campo etiqueta="Medio" obligatorio>{(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(MEDIO_COMUNICACION)} value={medio} onChange={(e) => setMedio(e.target.value as MedioComunicacion)} />}</Campo>
            <Campo etiqueta="Referencia" opcional>{(p) => <Entrada {...p} maxLength={200} value={referencia} onChange={(e) => setReferencia(e.target.value)} placeholder="N.° de oficio, asunto…" />}</Campo>
            <Campo etiqueta="Fecha">{(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={fechaAccion} onChange={(e) => setFechaAccion(e.target.value)} />}</Campo>
          </div>
          <ErrorFormulario error={comunicar.error} />
          <div className="flex justify-end gap-2"><Boton variante="fantasma" onClick={() => setAccion(null)}>Cancelar</Boton><Boton disabled={!medio} cargando={comunicar.isPending} onClick={() => comunicar.mutate()}>Registrar comunicación</Boton></div>
        </div>
      )}
      {accion === 'cerrar' && (
        <div className="rounded-[14px] border border-borde p-4 flex flex-col gap-3">
          <Campo etiqueta="Observación de cierre" obligatorio>{(p) => <AreaTexto {...p} maxLength={2000} value={observacion} onChange={(e) => setObservacion(e.target.value)} />}</Campo>
          <Campo etiqueta="Fecha">{(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={fechaAccion} onChange={(e) => setFechaAccion(e.target.value)} />}</Campo>
          <ErrorFormulario error={cerrar.error} />
          <div className="flex justify-end gap-2"><Boton variante="fantasma" onClick={() => setAccion(null)}>Cancelar</Boton><Boton variante="institucional" disabled={!observacion.trim()} cargando={cerrar.isPending} onClick={() => cerrar.mutate()}>Cerrar registro</Boton></div>
        </div>
      )}
    </Modal>
  )
}
