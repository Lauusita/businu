import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Selector } from '../../components/ui/Campo'
import { Chips } from '../../components/ui/Selecciones'
import { Modal } from '../../components/ui/Modal'
import { useMotores } from '../../hooks/consultas'
import { comunicacionesService } from '../../services/modules/comunicaciones.service'
import { viajesService } from '../../services/modules/viajes.service'
import type { OperacionResponse } from '../../types/api'
import {
  CAUSA_SUSPENSION, CONDICION_CLIMATICA, EMBARCADERO, NIVEL_RIO, condicionDesdeDescripcion, embarcaderoDesdeDescripcion,
  opciones, type CausaSuspension, type CondicionClimatica, type Embarcaderos, type NivelRio,
} from '../../types/enums'
import { ahoraLocal, decimal, instanteSiCambio, localAInstant, textoReal } from '../../utils/formato'
import { NovedadesMecanicas } from './NovedadesMecanicas'
import { aDTO, validarNovedades, type NovedadEditable } from './novedades'

type Props = { op: OperacionResponse; abierto: boolean; onCerrar: () => void }

function useActualizarViaje(op: OperacionResponse, onCerrar: () => void, mensaje: string) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  return (r: OperacionResponse) => {
    queryClient.setQueryData(['operacion', op.id], r)
    queryClient.invalidateQueries({ queryKey: ['operaciones'] })
    queryClient.invalidateQueries({ queryKey: ['personal'] })
    queryClient.invalidateQueries({ queryKey: ['motores'] })
    avisar(mensaje)
    onCerrar()
  }
}

/** Lecturas del horómetro de cada motor: obligatorias al finalizar y al suspender (R37). */
function useLecturas(op: OperacionResponse) {
  const motores = useMotores(op.embarcacion.id)
  const [lecturas, setLecturas] = useState<Record<string, string>>({})
  const minimo = (motorId: string) => {
    const h = op.horometros?.find((x) => x.motorId === motorId)
    return h?.lecturaInicial ?? motores.data?.find((m) => m.id === motorId)?.horometroActual ?? 0
  }
  const errores = Object.fromEntries(
    (motores.data ?? []).flatMap((m) => {
      const v = lecturas[m.id]
      if (v === undefined || v === '') return [[m.id, 'Registre la lectura']]
      if (Number(v) < minimo(m.id)) return [[m.id, `No puede ser menor que ${decimal(minimo(m.id))} h`]]
      return []
    }),
  )
  const campos = (intentado: boolean) =>
    motores.data?.map((m) => (
      <Campo key={m.id} etiqueta={`Horómetro · ${m.marca} ${m.serial}`} obligatorio ayuda={`Al zarpar: ${decimal(minimo(m.id))} h`} error={intentado ? errores[m.id] : undefined}>
        {(p) => <Entrada {...p} type="number" min={0} step="0.1" inputMode="decimal" sufijo="h" value={lecturas[m.id] ?? ''} onChange={(e) => setLecturas((l) => ({ ...l, [m.id]: e.target.value }))} />}
      </Campo>
    ))
  return { cargando: motores.isLoading, errores, campos, valores: () => (motores.data ?? []).map((m) => ({ motorId: m.id, lectura: Number(lecturas[m.id]) })) }
}

export function DialogoFinalizar({ op, abierto, onCerrar }: Props) {
  const [fin, setFin] = useState(ahoraLocal())
  const [intentado, setIntentado] = useState(false)
  const lect = useLecturas(op)
  const listo = useActualizarViaje(op, onCerrar, 'Viaje finalizado.')
  const m = useMutation({ mutationFn: () => viajesService.finalizar(op.id, { fechaHoraFin: localAInstant(fin), lecturasHorometro: lect.valores() }), onSuccess: listo })
  const errorFin = !fin ? 'Indique la hora de llegada' : new Date(localAInstant(fin)) > new Date() ? 'No puede ser futura' : undefined
  const enviar = () => {
    setIntentado(true)
    if (errorFin || Object.keys(lect.errores).length) return
    m.mutate()
  }
  return (
    <Modal abierto={abierto} onCerrar={onCerrar} titulo="Finalizar viaje" subtitulo="Registre la llegada al destino y el horómetro de cada motor."
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={enviar}>Finalizar viaje</Boton></>}>
      {op.totalUsuariosTransportados === 0 && <Alerta tono="aviso">El viaje aún no tiene usuarios registrados: el backend no permite finalizarlo sin pasajeros.</Alerta>}
      <Campo etiqueta="Hora de llegada" obligatorio error={intentado ? errorFin : undefined}>
        {(p) => <Entrada {...p} type="datetime-local" value={fin} max={ahoraLocal()} onChange={(e) => setFin(e.target.value)} />}
      </Campo>
      <div className="grid gap-3 sm:grid-cols-2">{lect.campos(intentado)}</div>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}

export function DialogoSuspender({ op, abierto, onCerrar }: Props) {
  const [causa, setCausa] = useState<CausaSuspension | ''>('')
  const [detalle, setDetalle] = useState('')
  const [autoridad, setAutoridad] = useState('')
  const [inicialMomento] = useState(ahoraLocal)
  const [momento, setMomento] = useState(inicialMomento)
  const [intentado, setIntentado] = useState(false)
  const lect = useLecturas(op)
  const listo = useActualizarViaje(op, onCerrar, 'Viaje suspendido.')
  const m = useMutation({
    mutationFn: () => viajesService.suspender(op.id, {
      causa: causa as CausaSuspension, detalle: detalle.trim() || null, autoridad: autoridad.trim() || null,
      momento: instanteSiCambio(momento, inicialMomento), lecturasHorometro: lect.valores(),
    }),
    onSuccess: listo,
  })
  const errores: Record<string, string> = {}
  if (!causa) errores.causa = 'Seleccione la causa'
  if (causa === 'OTRA' && !detalle.trim()) errores.detalle = 'Describa la circunstancia'
  if (causa === 'INSTRUCCION_AUTORIDAD' && !autoridad.trim()) errores.autoridad = 'Indique la autoridad que dio la instrucción'
  const enviar = () => {
    setIntentado(true)
    if (Object.keys(errores).length || Object.keys(lect.errores).length) return
    m.mutate()
  }
  return (
    <Modal abierto={abierto} onCerrar={onCerrar} titulo="Suspender viaje" subtitulo="§7.1.7: la suspensión del viaje es definitiva y no bloquea la embarcación." ancho="lg"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="peligro" cargando={m.isPending} onClick={enviar}>Suspender viaje</Boton></>}>
      <Campo etiqueta="Causa" obligatorio error={intentado ? errores.causa : undefined}>
        {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(CAUSA_SUSPENSION)} value={causa} onChange={(e) => setCausa(e.target.value as CausaSuspension)} />}
      </Campo>
      <div className="grid gap-3 sm:grid-cols-2">
        <Campo etiqueta="Autoridad" obligatorio={causa === 'INSTRUCCION_AUTORIDAD'} opcional={causa !== 'INSTRUCCION_AUTORIDAD'} error={intentado ? errores.autoridad : undefined}>
          {(p) => <Entrada {...p} maxLength={150} value={autoridad} onChange={(e) => setAutoridad(e.target.value)} />}
        </Campo>
        <Campo etiqueta="Momento de la suspensión">
          {(p) => <Entrada {...p} type="datetime-local" value={momento} max={ahoraLocal()} onChange={(e) => setMomento(e.target.value)} />}
        </Campo>
      </div>
      <Campo etiqueta="Detalle" obligatorio={causa === 'OTRA'} opcional={causa !== 'OTRA'} error={intentado ? errores.detalle : undefined}>
        {(p) => <AreaTexto {...p} maxLength={1000} value={detalle} onChange={(e) => setDetalle(e.target.value)} />}
      </Campo>
      <div className="grid gap-3 sm:grid-cols-2">{lect.campos(intentado)}</div>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}

export function DialogoParada({ op, abierto, onCerrar }: Props) {
  const registradas = new Set(op.paradas.map((p) => embarcaderoDesdeDescripcion(p.embarcadero)))
  const disponibles = (Object.keys(EMBARCADERO) as Embarcaderos[]).filter((e) => !registradas.has(e))
  const [embarcadero, setEmbarcadero] = useState<Embarcaderos | ''>('')
  const [llegada, setLlegada] = useState(ahoraLocal())
  const [salida, setSalida] = useState(ahoraLocal())
  const [intentado, setIntentado] = useState(false)
  const listo = useActualizarViaje(op, onCerrar, 'Parada registrada.')
  const m = useMutation({
    mutationFn: () => viajesService.registrarParada(op.id, { embarcadero: embarcadero as Embarcaderos, horaLlegada: localAInstant(llegada), horaSalida: localAInstant(salida) }),
    onSuccess: listo,
  })
  const errores: Record<string, string> = {}
  if (!embarcadero) errores.embarcadero = 'Seleccione el embarcadero'
  if (llegada && salida && salida < llegada) errores.salida = 'La salida debe ser posterior a la llegada'
  if (salida && new Date(localAInstant(salida)) > new Date()) errores.salida = 'No puede ser futura'
  return (
    <Modal abierto={abierto} onCerrar={onCerrar} titulo="Registrar parada" subtitulo="Hora de ingreso y salida en un embarcadero intermedio."
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>Registrar parada</Boton></>}>
      <Campo etiqueta="Embarcadero" obligatorio error={intentado ? errores.embarcadero : undefined}>
        {(p) => <Selector {...p} vacio="Seleccione…" opciones={disponibles.map((e) => ({ valor: e, etiqueta: EMBARCADERO[e] }))} value={embarcadero} onChange={(e) => setEmbarcadero(e.target.value as Embarcaderos)} />}
      </Campo>
      <div className="grid gap-3 sm:grid-cols-2">
        <Campo etiqueta="Hora de ingreso" obligatorio>{(p) => <Entrada {...p} type="datetime-local" value={llegada} max={ahoraLocal()} onChange={(e) => setLlegada(e.target.value)} />}</Campo>
        <Campo etiqueta="Hora de salida" obligatorio error={intentado ? errores.salida : undefined}>{(p) => <Entrada {...p} type="datetime-local" value={salida} max={ahoraLocal()} onChange={(e) => setSalida(e.target.value)} />}</Campo>
      </div>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}

/** PATCH /operaciones/{id}: clima, nivel del río, condiciones mecánicas, novedades y observaciones. */
export function DialogoActualizar({ op, abierto, onCerrar }: Props) {
  const [clima, setClima] = useState<CondicionClimatica>(op.condicionClimatica)
  const [nivel, setNivel] = useState<NivelRio>(op.nivelRio)
  const [novedades, setNovedades] = useState<NovedadEditable[]>(() =>
    op.condicionesMecanicas.map((n) => ({ condicion: condicionDesdeDescripcion(n.condicion) ?? 'OTRA', severidad: n.severidad, detalle: n.detalle ?? '' })),
  )
  const [texto, setTexto] = useState(textoReal(op.novedades) ?? '')
  const [observaciones, setObservaciones] = useState(textoReal(op.observaciones) ?? '')
  const [intentado, setIntentado] = useState(false)
  const errNov = validarNovedades(novedades)
  const listo = useActualizarViaje(op, onCerrar, 'Bitácora actualizada.')
  const m = useMutation({
    mutationFn: () => viajesService.actualizar(op.id, {
      condicionClimatica: clima, nivelRio: nivel, condicionesMecanicas: aDTO(novedades), novedades: texto, observaciones,
    }),
    onSuccess: listo,
  })
  return (
    <Modal abierto={abierto} onCerrar={onCerrar} titulo="Actualizar condiciones y novedades" ancho="lg"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errNov).length) m.mutate() }}>Guardar cambios</Boton></>}>
      <div className="flex flex-col gap-2">
        <span className="etiqueta">Condición climática</span>
        <Chips etiqueta="Condición climática" opciones={opciones(CONDICION_CLIMATICA)} valor={clima} onCambio={setClima} />
      </div>
      <Campo etiqueta="Nivel del río">{(p) => <Selector {...p} opciones={opciones(NIVEL_RIO)} value={nivel} onChange={(e) => setNivel(e.target.value as NivelRio)} />}</Campo>
      <div className="flex flex-col gap-2">
        <span className="etiqueta">Condiciones mecánicas</span>
        <NovedadesMecanicas valor={novedades} onCambio={setNovedades} errores={intentado ? errNov : undefined} />
      </div>
      <Campo etiqueta="Novedades del recorrido" ayuda="Operacionales, climáticas o de seguridad">
        {(p) => <AreaTexto {...p} maxLength={2000} value={texto} onChange={(e) => setTexto(e.target.value)} />}
      </Campo>
      <Campo etiqueta="Observaciones" opcional>{(p) => <AreaTexto {...p} maxLength={2000} value={observaciones} onChange={(e) => setObservaciones(e.target.value)} />}</Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}

/** POST /comunicaciones: constancia de la falla o novedad enviada por correo al supervisor (R50). */
export function DialogoComunicar({ referenciaTipo, referenciaId, asuntoSugerido, abierto, onCerrar }: {
  referenciaTipo: 'OPERACION' | 'SUSPENSION' | 'MANTENIMIENTO'; referenciaId: string; asuntoSugerido: string; abierto: boolean; onCerrar: () => void
}) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [destinatario, setDestinatario] = useState('')
  const [asunto, setAsunto] = useState(asuntoSugerido)
  const [mensaje, setMensaje] = useState('')
  const [intentado, setIntentado] = useState(false)
  const m = useMutation({
    mutationFn: () => comunicacionesService.registrar({ referenciaTipo, referenciaId, destinatario: destinatario.trim() || null, asunto: asunto.trim(), mensaje: mensaje.trim() }),
    onSuccess: (r) => {
      queryClient.invalidateQueries({ queryKey: ['comunicaciones', referenciaTipo, referenciaId] })
      avisar(r.estado === 'ENVIADO' ? 'Correo enviado al supervisor.' : `La comunicación quedó registrada como ${r.estado.toLowerCase()}: ${r.error ?? 'puede reintentarla.'}`, r.estado === 'ENVIADO' ? 'exito' : 'error')
      onCerrar()
    },
  })
  const errores: Record<string, string> = {}
  if (!asunto.trim()) errores.asunto = 'Escriba el asunto'
  if (!mensaje.trim()) errores.mensaje = 'Escriba el mensaje'
  if (destinatario && !/^\S+@\S+\.\S+$/.test(destinatario)) errores.destinatario = 'Correo no válido'
  return (
    <Modal abierto={abierto} onCerrar={onCerrar} titulo="Comunicar al supervisor del contrato" subtitulo="Se envía por correo y queda constancia del envío (§7.1.12)."
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>Enviar</Boton></>}>
      <Campo etiqueta="Destinatario" opcional ayuda="Si lo deja vacío, se usa el correo del supervisor configurado" error={intentado ? errores.destinatario : undefined}>
        {(p) => <Entrada {...p} type="email" maxLength={150} value={destinatario} onChange={(e) => setDestinatario(e.target.value)} />}
      </Campo>
      <Campo etiqueta="Asunto" obligatorio error={intentado ? errores.asunto : undefined}>{(p) => <Entrada {...p} maxLength={200} value={asunto} onChange={(e) => setAsunto(e.target.value)} />}</Campo>
      <Campo etiqueta="Mensaje" obligatorio error={intentado ? errores.mensaje : undefined}>{(p) => <AreaTexto {...p} maxLength={5000} rows={6} value={mensaje} onChange={(e) => setMensaje(e.target.value)} />}</Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}
