import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useRef, useState } from 'react'
import { CircleAlert, CircleCheck, Eye, FileText, ShieldCheck, Upload, UserRound, UserX } from 'lucide-react'
import { useNavigate, useParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { useAuth } from '../../app/providers/auth-context'
import { Cargando, ErrorCarga, ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Avatar } from '../../components/ui/Avatar'
import { Boton } from '../../components/ui/Boton'
import { Campo, Casilla, Entrada, Selector } from '../../components/ui/Campo'
import { Insignia } from '../../components/ui/Insignia'
import { Confirmacion, Modal } from '../../components/ui/Modal'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { EXTENSIONES_ADJUNTO, FORMATOS_ADJUNTO, MAX_BYTES_ADJUNTO } from '../../services/modules/adjuntos.service'
import { personalService } from '../../services/modules/personal.service'
import type { DocumentacionPersonaResponse } from '../../types/api'
import { ROL, TIPO_DOCUMENTO, TIPO_IDENTIFICACION, opciones, usaSistema, type TipoDocumentoPersonal } from '../../types/enums'
import { abrirEnPestana } from '../../utils/descargas'
import { bytes, fecha, mesActual, mesLargo, textoReal } from '../../utils/formato'
import { Trazabilidad } from '../compartido/Trazabilidad'

/** Plantilla 15 · Ficha del colaborador y documentos exigibles (numeral 4.6). */
export function ColaboradorPage() {
  const { id = '' } = useParams()
  const navegar = useNavigate()
  const { usuario } = useAuth()
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [historial, setHistorial] = useState(false)
  const [cargar, setCargar] = useState<TipoDocumentoPersonal | 'nuevo' | null>(null)
  const [retirar, setRetirar] = useState(false)
  const p = useQuery({ queryKey: ['personal', id], queryFn: () => personalService.obtener(id) })
  const docs = useQuery({ queryKey: ['documentos', id, historial], queryFn: () => personalService.documentacion(id, historial) })

  const ver = useMutation({
    mutationFn: (documentoId: string) => personalService.urlDocumento(id, documentoId),
    onSuccess: (r) => abrirEnPestana(r.url),
    onError: (e) => avisar(e.message, 'error'),
  })
  const desactivar = useMutation({
    mutationFn: () => personalService.desactivar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['personal'] })
      avisar('La cuenta quedó desactivada: contrato inactivo y sin acceso al sistema.')
      setRetirar(false)
      navegar('/personal')
    },
  })

  if (p.isLoading) return <Pagina titulo="Colaborador"><Cargando alto={320} /></Pagina>
  if (p.error || !p.data) return <Pagina titulo="Colaborador"><Tarjeta><ErrorCarga error={p.error} onReintentar={() => p.refetch()} /></Tarjeta></Pagina>
  const c = p.data
  const nombre = `${c.nombres} ${c.apellidos}`
  const d = docs.data
  const requeridos = d ? [...new Set([...d.faltantes, ...d.documentos.map((x) => x.tipo)])] : []

  return (
    <Pagina
      titulo={nombre}
      migas={[{ etiqueta: 'Personal', a: '/personal' }, { etiqueta: nombre }]}
      descripcion="Ficha del colaborador, documentos exigibles y registros asociados."
    >
      <Tarjeta className="!flex-row flex-wrap items-center gap-5">
        <Avatar nombre={nombre} tamano={64} />
        <div className="flex-1 min-w-[240px]">
          <h2 className="m-0 font-titulo text-[28px] font-normal text-noche">{nombre}</h2>
          <p className="m-0 text-[13px] text-texto-2">{ROL[c.rol]}</p>
          {d && (
            <div className="flex flex-wrap gap-2 mt-2">
              {d.faltantes.length ? <Insignia tono="warn">{d.faltantes.length} documento(s) pendiente(s)</Insignia> : <Insignia tono="ok">Documentación completa</Insignia>}
              {d.planillasPila.includes(mesActual()) ? <Insignia tono="ok">Seguridad social al día</Insignia> : <Insignia tono="warn">PILA de {mesLargo(mesActual())} pendiente</Insignia>}
              {!usaSistema(c.rol) && <Insignia tono="neutral">Sin acceso al sistema</Insignia>}
            </div>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          <Boton icono={<Upload className="size-4" />} onClick={() => setCargar('nuevo')}>Cargar documento</Boton>
          {c.id !== usuario?.personalId && <Boton variante="peligro" icono={<UserX className="size-4" />} onClick={() => setRetirar(true)}>Desactivar cuenta</Boton>}
        </div>
      </Tarjeta>

      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Información personal y cargo" icono={<UserRound />} />
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              <Dato etiqueta="Documento">{TIPO_IDENTIFICACION[c.tipoIdentificacion]} {c.numeroIdentificacion}</Dato>
              <Dato etiqueta="Correo">{c.email ?? '—'}</Dato>
              <Dato etiqueta="Cargo">{ROL[c.rol]}</Dato>
            </div>
          </Tarjeta>
          <Tarjeta>
            <EncabezadoTarjeta titulo="Documentos de soporte" icono={<FileText />}
              acciones={<Casilla etiqueta="Ver historial" checked={historial} onChange={(e) => setHistorial(e.target.checked)} />} />
            {docs.isLoading ? <Cargando /> : docs.error ? <ErrorCarga error={docs.error} onReintentar={() => docs.refetch()} /> : d && (
              <ul className="m-0 p-0 list-none flex flex-col divide-y divide-borde">
                {requeridos.filter((t) => t !== 'PLANILLA_PILA').map((t) => {
                  const lista = d.documentos.filter((x) => x.tipo === t)
                  const falta = d.faltantes.includes(t)
                  return (
                    <li key={t} className="py-3 flex flex-col gap-1.5">
                      <div className="flex items-start gap-2.5">
                        {falta ? <CircleAlert className="size-5 text-advertencia flex-none" aria-hidden /> : <CircleCheck className="size-5 text-exito flex-none" aria-hidden />}
                        <div className="flex-1 min-w-0"><b className="text-[14px]">{TIPO_DOCUMENTO[t]}</b></div>
                        {falta ? <Boton variante="suave" pequeno onClick={() => setCargar(t)}>Cargar</Boton> : <Insignia tono="ok">Cargado</Insignia>}
                      </div>
                      {lista.map((x) => <FilaDocumento key={x.id} doc={x} onVer={() => ver.mutate(x.id)} cargando={ver.isPending && ver.variables === x.id} />)}
                    </li>
                  )
                })}
              </ul>
            )}
          </Tarjeta>
        </div>
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Seguridad social (planillas PILA)" icono={<ShieldCheck />} acciones={<Boton variante="suave" pequeno onClick={() => setCargar('PLANILLA_PILA')}>Cargar planilla</Boton>} />
            {d && (d.planillasPila.length ? (
              <ul className="m-0 p-0 list-none flex flex-col gap-2">
                {[...d.planillasPila].sort().reverse().map((m) => {
                  const x = d.documentos.find((y) => y.tipo === 'PLANILLA_PILA' && y.periodo === m)
                  return (
                    <li key={m} className="flex items-center justify-between gap-2 text-[13.5px]">
                      <span>Planilla de {mesLargo(m)}</span>
                      <span className="flex items-center gap-2"><Insignia tono="ok">Pagada</Insignia>{x && <Boton variante="fantasma" pequeno soloIcono aria-label="Ver planilla" onClick={() => ver.mutate(x.id)}><Eye className="size-4" /></Boton>}</span>
                    </li>
                  )
                })}
              </ul>
            ) : <p className="m-0 text-[13px] text-texto-2">Sin planillas cargadas.</p>)}
          </Tarjeta>
          <Trazabilidad tipo="PERSONAL" id={c.id} />
        </div>
      </div>

      {cargar && <DialogoDocumento personalId={id} tipoInicial={cargar === 'nuevo' ? undefined : cargar} onCerrar={() => setCargar(null)} />}
      <Confirmacion abierto={retirar} onCerrar={() => setRetirar(false)} onConfirmar={() => desactivar.mutate()} peligro textoConfirmar="Desactivar cuenta"
        titulo="Desactivar cuenta" cargando={desactivar.isPending} error={desactivar.error?.message}
        mensaje={<><b>{nombre}</b> quedará con el contrato inactivo, fecha de retiro de hoy y sin acceso al sistema. Su historial se conserva.</>} />
    </Pagina>
  )
}

function FilaDocumento({ doc, onVer, cargando }: { doc: DocumentacionPersonaResponse['documentos'][number]; onVer: () => void; cargando: boolean }) {
  return (
    <div className="ml-7 flex items-center gap-2.5 rounded-[10px] bg-fondo px-3 py-2 text-[12.5px]">
      <FileText className="size-4 text-azul flex-none" aria-hidden />
      <span className="flex-1 min-w-0">
        <span className="block truncate font-semibold">{doc.nombreArchivo}</span>
        <span className="text-texto-2">{fecha(doc.cargadoEn)} · {bytes(doc.tamanoBytes)} · {doc.registradoPor}{textoReal(doc.observaciones) && ` · ${doc.observaciones}`}</span>
      </span>
      {!doc.vigente && <Insignia tono="neutral">Reemplazado</Insignia>}
      <Boton variante="fantasma" pequeno soloIcono aria-label={`Ver ${doc.nombreArchivo}`} cargando={cargando} onClick={onVer}><Eye className="size-4" /></Boton>
    </div>
  )
}

/** POST /personal/{id}/documentos (multipart): tipo, periodo (solo PILA), observaciones y archivo. */
function DialogoDocumento({ personalId, tipoInicial, onCerrar }: { personalId: string; tipoInicial?: TipoDocumentoPersonal; onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const input = useRef<HTMLInputElement>(null)
  const [tipo, setTipo] = useState<TipoDocumentoPersonal | ''>(tipoInicial ?? '')
  const [periodo, setPeriodo] = useState(mesActual())
  const [observaciones, setObservaciones] = useState('')
  const [archivo, setArchivo] = useState<File | null>(null)
  const [intentado, setIntentado] = useState(false)
  const errores: Record<string, string> = {}
  if (!tipo) errores.tipo = 'Seleccione el tipo de documento'
  if (tipo === 'PLANILLA_PILA' && !periodo) errores.periodo = 'Indique el mes de la planilla'
  if (!archivo) errores.archivo = 'Seleccione el archivo'
  else if (!FORMATOS_ADJUNTO.includes(archivo.type)) errores.archivo = 'Formato no permitido: use PDF, JPG o PNG'
  else if (archivo.size > MAX_BYTES_ADJUNTO) errores.archivo = 'El archivo supera 10 MB'
  const m = useMutation({
    mutationFn: () => personalService.cargarDocumento(personalId, { tipo: tipo as TipoDocumentoPersonal, archivo: archivo!, periodo: tipo === 'PLANILLA_PILA' ? periodo : undefined, observaciones: observaciones.trim() || undefined }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documentos', personalId] })
      avisar('Documento cargado.')
      onCerrar()
    },
  })
  return (
    <Modal abierto onCerrar={onCerrar} titulo="Cargar documento" subtitulo="PDF, JPG o PNG · máx. 10 MB. Las fotos se guardan como PDF."
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>Cargar documento</Boton></>}>
      <Campo etiqueta="Tipo de documento" obligatorio error={intentado ? errores.tipo : undefined}>
        {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(TIPO_DOCUMENTO)} value={tipo} onChange={(e) => setTipo(e.target.value as TipoDocumentoPersonal)} />}
      </Campo>
      {tipo === 'PLANILLA_PILA' && (
        <Campo etiqueta="Mes de la planilla" obligatorio error={intentado ? errores.periodo : undefined}>{(p) => <Entrada {...p} type="month" max={mesActual()} value={periodo} onChange={(e) => setPeriodo(e.target.value)} />}</Campo>
      )}
      <Campo etiqueta="Observaciones" opcional>{(p) => <Entrada {...p} maxLength={500} value={observaciones} onChange={(e) => setObservaciones(e.target.value)} placeholder="Vigencia, entidad que expide…" />}</Campo>
      <Campo etiqueta="Archivo" obligatorio error={intentado ? errores.archivo : undefined}>
        {(p) => (
          <div className="campo">
            <span className="flex-1 min-w-0 truncate text-[14px]">{archivo ? `${archivo.name} · ${bytes(archivo.size)}` : 'Ningún archivo seleccionado'}</span>
            <Boton {...p} variante="suave" pequeno onClick={() => input.current?.click()}>Seleccionar</Boton>
            <input ref={input} type="file" hidden accept={EXTENSIONES_ADJUNTO} onChange={(e) => setArchivo(e.target.files?.[0] ?? null)} />
          </div>
        )}
      </Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}
