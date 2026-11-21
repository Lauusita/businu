import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Selector } from '../../components/ui/Campo'
import { Modal } from '../../components/ui/Modal'
import { embarcacionesService } from '../../services/modules/embarcaciones.service'
import type { MotorResponse, SuspensionResponse } from '../../types/api'
import { CAUSA_SUSPENSION, opciones, type CausaSuspension } from '../../types/enums'
import { ahoraLocal, decimal, instanteSiCambio } from '../../utils/formato'

/** Suspensión de una embarcación que no está navegando (SuspenderEmbarcacionRequest, §7.1.7). */
export function DialogoSuspenderEmbarcacion({ embarcacionId, nombre, onCerrar }: { embarcacionId: string; nombre: string; onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [causa, setCausa] = useState<CausaSuspension | ''>('')
  const [detalle, setDetalle] = useState('')
  const [autoridad, setAutoridad] = useState('')
  const [inicial] = useState(ahoraLocal)
  const [inicio, setInicio] = useState(inicial)
  const [intentado, setIntentado] = useState(false)
  const errores: Record<string, string> = {}
  if (!causa) errores.causa = 'Seleccione la causa'
  if (causa === 'OTRA' && !detalle.trim()) errores.detalle = 'Describa la circunstancia'
  if (causa === 'INSTRUCCION_AUTORIDAD' && !autoridad.trim()) errores.autoridad = 'Indique la autoridad'
  const m = useMutation({
    mutationFn: () => embarcacionesService.suspender(embarcacionId, { causa: causa as CausaSuspension, detalle: detalle.trim() || null, autoridad: autoridad.trim() || null, fechaInicio: instanteSiCambio(inicio, inicial) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suspensiones', embarcacionId] })
      queryClient.invalidateQueries({ queryKey: ['embarcaciones'] })
      avisar(`${nombre} quedó suspendida.`)
      onCerrar()
    },
  })
  return (
    <Modal abierto onCerrar={onCerrar} titulo={`Suspender ${nombre}`} subtitulo="La embarcación no podrá zarpar hasta que se levante la suspensión."
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="peligro" cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>Suspender</Boton></>}>
      <Campo etiqueta="Causa" obligatorio error={intentado ? errores.causa : undefined}>
        {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(CAUSA_SUSPENSION)} value={causa} onChange={(e) => setCausa(e.target.value as CausaSuspension)} />}
      </Campo>
      <div className="grid gap-3 sm:grid-cols-2">
        <Campo etiqueta="Autoridad" obligatorio={causa === 'INSTRUCCION_AUTORIDAD'} opcional={causa !== 'INSTRUCCION_AUTORIDAD'} error={intentado ? errores.autoridad : undefined}>
          {(p) => <Entrada {...p} maxLength={150} value={autoridad} onChange={(e) => setAutoridad(e.target.value)} />}
        </Campo>
        <Campo etiqueta="Desde">{(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={inicio} onChange={(e) => setInicio(e.target.value)} />}</Campo>
      </div>
      <Campo etiqueta="Detalle" obligatorio={causa === 'OTRA'} opcional={causa !== 'OTRA'} error={intentado ? errores.detalle : undefined}>
        {(p) => <AreaTexto {...p} maxLength={1000} value={detalle} onChange={(e) => setDetalle(e.target.value)} />}
      </Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}

export function DialogoLevantarSuspension({ suspension, onCerrar }: { suspension: SuspensionResponse; onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [observacion, setObservacion] = useState('')
  const [inicial] = useState(ahoraLocal)
  const [fecha, setFecha] = useState(inicial)
  const m = useMutation({
    mutationFn: () => embarcacionesService.levantarSuspension(suspension.embarcacionId, suspension.id, { observacion: observacion.trim(), fecha: instanteSiCambio(fecha, inicial) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['suspensiones', suspension.embarcacionId] })
      queryClient.invalidateQueries({ queryKey: ['embarcaciones'] })
      avisar('Suspensión levantada: la embarcación puede volver a operar.')
      onCerrar()
    },
  })
  return (
    <Modal abierto onCerrar={onCerrar} titulo="Levantar suspensión" ancho="sm"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" disabled={!observacion.trim()} cargando={m.isPending} onClick={() => m.mutate()}>Levantar suspensión</Boton></>}>
      <Campo etiqueta="Por qué se restablece la operación" obligatorio>{(p) => <AreaTexto {...p} maxLength={1000} value={observacion} onChange={(e) => setObservacion(e.target.value)} />}</Campo>
      <Campo etiqueta="Fecha">{(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={fecha} onChange={(e) => setFecha(e.target.value)} />}</Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}

/** PATCH /motores/{id}/horometro (solo supervisor). La lectura no puede bajar. */
export function DialogoHorometro({ motor, onCerrar }: { motor: MotorResponse; onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [lectura, setLectura] = useState('')
  const minimo = motor.horometroActual ?? 0
  const error = lectura === '' ? 'Escriba la lectura' : Number(lectura) < minimo ? `No puede ser menor que ${decimal(minimo)} h` : undefined
  const [intentado, setIntentado] = useState(false)
  const m = useMutation({
    mutationFn: () => embarcacionesService.registrarHorometro(motor.id, Number(lectura)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['motores'] })
      avisar('Lectura del horómetro registrada.')
      onCerrar()
    },
  })
  return (
    <Modal abierto onCerrar={onCerrar} titulo={`Horómetro · motor ${motor.serial}`} ancho="sm"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={() => { setIntentado(true); if (!error) m.mutate() }}>Registrar lectura</Boton></>}>
      <Campo etiqueta="Lectura actual" obligatorio ayuda={`Última lectura: ${decimal(motor.horometroActual)} h`} error={intentado ? error : undefined}>
        {(p) => <Entrada {...p} type="number" step="0.1" min={minimo} sufijo="h" value={lectura} onChange={(e) => setLectura(e.target.value)} />}
      </Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}
