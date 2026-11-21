import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import {
  Anchor, CircleCheck, ClipboardList, CloudSun, Flag, Fuel, Gauge, IdCard, Pencil, Printer, ShieldCheck, Ship, StopCircle,
  TriangleAlert, UserMinus, UserPlus, UsersRound,
} from 'lucide-react'
import { useParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { useAuth } from '../../app/providers/auth-context'
import { Cargando, ErrorCarga } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Alerta } from '../../components/ui/Alerta'
import { Avatar } from '../../components/ui/Avatar'
import { Boton } from '../../components/ui/Boton'
import { Selector } from '../../components/ui/Campo'
import { Insignia } from '../../components/ui/Insignia'
import { EstadoOperacionInsignia } from '../../components/ui/InsigniasEstado'
import { Modal } from '../../components/ui/Modal'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useTripulacionDisponible } from '../../hooks/consultas'
import { usePermiso } from '../../hooks/usePermiso'
import { usuariosService } from '../../services/modules/usuarios.service'
import { viajesService } from '../../services/modules/viajes.service'
import type { OperacionResponse } from '../../types/api'
import {
  CONDICION_CLIMATICA, EMBARCACION, EMBARCADERO, ESTADO_NAVE, GRUPO_ETARIO_CORTO, NIVEL_RIO, ROL, SEXO, nombreEmbarcadero,
  type Embarcaderos, type GrupoEtario, type Sexo,
} from '../../types/enums'
import { codigoCorto, decimal, duracion, entero, fecha, fechaHora, fechaLarga, hora, textoReal } from '../../utils/formato'
import { Comunicaciones } from '../compartido/Comunicaciones'
import { Trazabilidad } from '../compartido/Trazabilidad'
import { DialogoActualizar, DialogoFinalizar, DialogoParada, DialogoSuspender } from './AccionesViaje'

const VERIF: { clave: keyof OperacionResponse['verificacion']; etiqueta: string }[] = [
  { clave: 'aceiteMotor', etiqueta: 'Aceite de motor' },
  { clave: 'combustible', etiqueta: 'Combustible' },
  { clave: 'sistemaDireccion', etiqueta: 'Sistema de dirección' },
  { clave: 'bombaAchique', etiqueta: 'Bomba de achique' },
  { clave: 'chalecosSalvavidas', etiqueta: 'Chalecos salvavidas' },
  { clave: 'lucesNavegacion', etiqueta: 'Luces de navegación' },
  { clave: 'radioComunicacion', etiqueta: 'Radio de comunicación' },
]

type Dialogo = 'finalizar' | 'suspender' | 'parada' | 'actualizar' | 'tripulante' | null

/** Plantilla 05 · Detalle de viaje (GET /operaciones/{id}) y sus acciones mientras está en curso. */
export function DetalleViajePage() {
  const { id = '' } = useParams()
  const puedeEditar = usePermiso('viajes.registrar')
  const [dialogo, setDialogo] = useState<Dialogo>(null)
  const q = useQuery({ queryKey: ['operacion', id], queryFn: () => viajesService.obtener(id) })
  const pasajeros = useQuery({ queryKey: ['pasajeros', 'operacion', id], queryFn: () => usuariosService.porOperacion(id) })

  if (q.isLoading) return <Pagina titulo="Bitácora de viaje"><Cargando alto={320} /></Pagina>
  if (q.error || !q.data) return <Pagina titulo="Bitácora de viaje"><Tarjeta><ErrorCarga error={q.error} onReintentar={() => q.refetch()} /></Tarjeta></Pagina>
  const op = q.data
  const codigo = codigoCorto('V', op.id)
  const enCurso = op.estado === 'EN_CURSO'
  const editable = enCurso && puedeEditar
  const cerrar = () => setDialogo(null)
  const novedadesTexto = textoReal(op.novedades)
  const observaciones = textoReal(op.observaciones)
  const novedadesCount = op.condicionesMecanicas.length + (novedadesTexto ? 1 : 0)

  // Recorrido: origen, paradas intermedias y destino.
  const paradas = op.paradas
  const destinoRegistrado = paradas.some((p) => p.embarcadero === op.embarcaderoDestino)

  return (
    <Pagina
      titulo={`Bitácora ${codigo}`}
      migas={[{ etiqueta: 'Operación' }, { etiqueta: 'Viajes', a: '/viajes' }, { etiqueta: codigo }]}
      descripcion={enCurso ? 'Viaje en curso: registre las paradas, actualice las novedades y finalícelo al llegar.' : 'Consulta de la bitácora registrada · solo lectura.'}
      acciones={
        <>
          <Boton variante="secundario" icono={<Printer className="size-4" />} onClick={() => window.print()}>Imprimir</Boton>
          {editable && (
            <>
              <Boton variante="suave" icono={<Pencil className="size-4" />} onClick={() => setDialogo('actualizar')}>Actualizar</Boton>
              <Boton variante="secundario" icono={<Anchor className="size-4" />} onClick={() => setDialogo('parada')}>Registrar parada</Boton>
              <Boton variante="peligro" icono={<StopCircle className="size-4" />} onClick={() => setDialogo('suspender')}>Suspender</Boton>
              <Boton variante="institucional" icono={<Flag className="size-4" />} onClick={() => setDialogo('finalizar')}>Finalizar viaje</Boton>
            </>
          )}
        </>
      }
    >
      <Tarjeta>
        <div className="flex flex-wrap items-start gap-6">
          <div className="min-w-[220px]">
            <div className="flex items-center gap-2.5">
              <span className="font-titulo text-[24px] text-noche">{codigo}</span>
              <EstadoOperacionInsignia estado={op.estado} />
            </div>
            <div className="flex items-center gap-1.5 mt-1 text-[13px] text-texto-2">
              <Ship className="size-4 text-azul" aria-hidden /> <b className="text-negro">{EMBARCACION[op.embarcacion.nombre]}</b> · {fechaLarga(op.fechaHoraInicio)}
            </div>
          </div>
          <ol className="flex-1 min-w-[280px] m-0 p-0 list-none flex items-start justify-between gap-2 relative" aria-label="Recorrido">
            <span className="absolute left-[12%] right-[12%] top-[11px] h-0.5 bg-borde" aria-hidden />
            {paradas.map((p, i) => (
              <li key={p.embarcadero + i} className="relative flex flex-col items-center text-center min-w-0">
                <span className="size-6 rounded-full border-[3px] border-noche bg-white" aria-hidden />
                <b className="text-[12.5px] mt-1.5">{nombreEmbarcadero(p.embarcadero)}</b>
                {p.horaLlegada && i > 0 && <span className="text-[11.5px] text-texto-2">Ingreso {hora(p.horaLlegada)}</span>}
                {p.horaSalida && <span className="text-[11.5px] text-texto-2">Salida {hora(p.horaSalida)}</span>}
              </li>
            ))}
            {!destinoRegistrado && (
              <li className="relative flex flex-col items-center text-center">
                <span className={`size-6 rounded-full border-[3px] border-noche ${op.fechaHoraFin ? 'bg-noche' : 'bg-white border-dashed'}`} aria-hidden />
                <b className="text-[12.5px] mt-1.5">{nombreEmbarcadero(op.embarcaderoDestino)}</b>
                <span className="text-[11.5px] text-texto-2">{op.fechaHoraFin ? `Llegada ${hora(op.fechaHoraFin)}` : 'Pendiente'}</span>
              </li>
            )}
          </ol>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <Mini icono={<UsersRound className="size-4" />} etiqueta="Pasajeros" valor={entero(op.totalUsuariosTransportados)} />
          <Mini icono={<Gauge className="size-4" />} etiqueta="Duración" valor={op.fechaHoraFin ? duracion(op.fechaHoraInicio, op.fechaHoraFin) : 'En curso'} />
          <Mini icono={<TriangleAlert className="size-4" />} etiqueta="Novedades" valor={entero(novedadesCount)} />
          <Mini icono={<Gauge className="size-4" />} etiqueta="Horas de motor" valor={op.horasMotor !== undefined ? `${decimal(op.horasMotor)} h` : '—'} />
        </div>
      </Tarjeta>

      {op.suspension && (
        <Alerta tono="error" titulo={`Viaje suspendido · ${op.suspension.causaDescripcion}`}>
          {op.suspension.detalle}{op.suspension.autoridad && <> · Autoridad: <b>{op.suspension.autoridad}</b></>}
        </Alerta>
      )}

      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Información general" icono={<ClipboardList />} />
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              <Dato etiqueta="Fecha de operación">{fecha(op.fechaHoraInicio)}</Dato>
              <Dato etiqueta="Embarcación">{EMBARCACION[op.embarcacion.nombre]} · {op.embarcacion.patente}</Dato>
              <Dato etiqueta="Registrado por">{op.registradoPor ?? '—'}</Dato>
              <Dato etiqueta="Origen">{nombreEmbarcadero(op.embarcaderoOrigen)}</Dato>
              <Dato etiqueta="Destino">{nombreEmbarcadero(op.embarcaderoDestino)}</Dato>
              <Dato etiqueta="Horas de navegación">{op.horasNavegacion !== undefined ? `${decimal(op.horasNavegacion)} h` : 'En curso'}</Dato>
            </div>
          </Tarjeta>

          <Tarjeta sinRelleno>
            <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Horas de ingreso y salida por embarcadero" icono={<Anchor />} /></div>
            <div className="overflow-x-auto px-5 pb-5">
              <table className="tabla min-w-[520px] border border-borde rounded-[12px] overflow-hidden">
                <thead><tr><th>Embarcadero</th><th>Hora de ingreso</th><th>Hora de salida</th><th>Permanencia</th></tr></thead>
                <tbody>
                  {paradas.map((p, i) => (
                    <tr key={i}>
                      <td><span className="inline-flex items-center gap-2 font-bold"><CircleCheck className="size-5 text-white fill-exito" aria-hidden />{nombreEmbarcadero(p.embarcadero)}</span></td>
                      <td>{i === 0 ? '—' : hora(p.horaLlegada)}</td>
                      <td>{hora(p.horaSalida)}</td>
                      <td>{i === 0 ? '—' : duracion(p.horaLlegada, p.horaSalida)}</td>
                    </tr>
                  ))}
                  {!destinoRegistrado && (
                    <tr>
                      <td><span className="inline-flex items-center gap-2 font-bold">{op.fechaHoraFin ? <CircleCheck className="size-5 text-white fill-exito" aria-hidden /> : <span className="size-5 rounded-full border border-borde" aria-hidden />}{nombreEmbarcadero(op.embarcaderoDestino)}</span></td>
                      <td>{op.fechaHoraFin ? hora(op.fechaHoraFin) : '—'}</td><td>—</td><td>—</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </Tarjeta>

          <Tarjeta>
            <EncabezadoTarjeta titulo="Verificación previa a la navegación" icono={<ShieldCheck />} acciones={<Insignia tono={op.estadoNave === 'BUENO' ? 'ok' : 'warn'}>Estado general: {ESTADO_NAVE[op.estadoNave]}</Insignia>} />
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2.5">
              {VERIF.map((v) => (
                <span key={v.clave} className={`flex items-center gap-2 rounded-[10px] px-3 py-2.5 text-[13px] ${op.verificacion[v.clave] ? 'bg-exito-suave' : 'bg-advertencia-suave'}`}>
                  {op.verificacion[v.clave] ? <CircleCheck className="size-4 text-exito flex-none" aria-label="Conforme" /> : <TriangleAlert className="size-4 text-advertencia flex-none" aria-label="Revisar" />}
                  {v.etiqueta}
                </span>
              ))}
            </div>
            {textoReal(op.justificacionVerificacion) && (
              <p className="m-0 text-[13px] text-texto-2">Justificación: {op.justificacionVerificacion}</p>
            )}
          </Tarjeta>

          <Tarjeta>
            <EncabezadoTarjeta titulo="Condiciones de operación" icono={<CloudSun />} />
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <Dato etiqueta="Clima observado">{CONDICION_CLIMATICA[op.condicionClimatica]}</Dato>
              <Dato etiqueta="Nivel del río">{NIVEL_RIO[op.nivelRio]}</Dato>
              <Dato etiqueta="Condición mecánica">{op.sinNovedadesMecanicas ? 'Sin novedad' : `${op.condicionesMecanicas.length} novedad(es)`}</Dato>
              <Dato etiqueta="Pasajeros transportados">{entero(op.totalUsuariosTransportados)}</Dato>
            </div>
            {!!op.horometros?.length && (
              <div className="grid sm:grid-cols-2 gap-2">
                {op.horometros.map((h) => (
                  <div key={h.motorId} className="rounded-[10px] bg-fondo px-3 py-2 text-[13px]">
                    <b>Motor {h.serial}</b> · inicial {decimal(h.lecturaInicial)} h · final {decimal(h.lecturaFinal)} h{h.horas !== undefined && <> · <b>{decimal(h.horas)} h</b></>}
                  </div>
                ))}
              </div>
            )}
          </Tarjeta>

          <PasajerosRegistrados cargando={pasajeros.isLoading} error={pasajeros.error} datos={pasajeros.data} />
        </div>

        <div className="flex flex-col gap-5 min-w-0">
          <Tripulacion op={op} editable={editable} onAgregar={() => setDialogo('tripulante')} />
          <Tarjeta>
            <EncabezadoTarjeta titulo="Novedades del recorrido" icono={<TriangleAlert />} tono="advertencia"
              acciones={novedadesCount ? <Insignia tono="warn">{novedadesCount} registrada(s)</Insignia> : <Insignia tono="ok">Sin novedad</Insignia>} />
            {op.condicionesMecanicas.map((n, i) => (
              <div key={i} className="rounded-[12px] bg-advertencia-suave px-3.5 py-3 text-[13px]">
                <b>{n.condicion}</b> · {n.severidad.toLowerCase()}
                {n.detalle && <div className="text-texto-2">{n.detalle}</div>}
              </div>
            ))}
            {novedadesTexto && <p className="m-0 text-[13px] whitespace-pre-line">{novedadesTexto}</p>}
            {observaciones && <p className="m-0 text-[13px] text-texto-2">Observaciones: {observaciones}</p>}
            {!novedadesCount && !observaciones && <p className="m-0 text-[13px] text-texto-2">No se registraron novedades.</p>}
          </Tarjeta>
          <Tarjeta>
            <EncabezadoTarjeta titulo="Combustible" icono={<Fuel />} />
            {op.cargues?.length ? (
              <ul className="m-0 p-0 list-none flex flex-col gap-2 text-[13px]">
                {op.cargues.map((c) => (
                  <li key={c.abastecimientoId} className="rounded-[10px] bg-fondo px-3 py-2">
                    <b>{decimal(c.galones, 2)} gal</b> · {fechaHora(c.fechaHora)} {c.tanqueLleno && <Insignia tono="info" className="ml-1">Tanque lleno</Insignia>}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="m-0 text-[13px] text-texto-2">Sin cargue de combustible asociado a esta operación.</p>
            )}
          </Tarjeta>
          <Comunicaciones referenciaTipo="OPERACION" referenciaId={op.id} asuntoSugerido={`Novedad en el viaje ${codigo} · ${EMBARCACION[op.embarcacion.nombre]}`} />
          <Trazabilidad tipo="OPERACION" id={op.id} />
        </div>
      </div>

      {editable && (
        <>
          <DialogoFinalizar op={op} abierto={dialogo === 'finalizar'} onCerrar={cerrar} />
          <DialogoSuspender op={op} abierto={dialogo === 'suspender'} onCerrar={cerrar} />
          <DialogoParada op={op} abierto={dialogo === 'parada'} onCerrar={cerrar} />
          {dialogo === 'actualizar' && <DialogoActualizar op={op} abierto onCerrar={cerrar} />}
          {dialogo === 'tripulante' && <DialogoTripulante op={op} onCerrar={cerrar} />}
        </>
      )}
    </Pagina>
  )
}

function Mini({ icono, etiqueta, valor }: { icono: React.ReactNode; etiqueta: string; valor: string }) {
  return (
    <div className="rounded-[12px] bg-fondo px-3.5 py-2.5">
      <div className="flex items-center gap-1.5 text-[12px] text-texto-2">{icono}{etiqueta}</div>
      <div className="font-titulo text-[22px] text-noche">{valor}</div>
    </div>
  )
}

function Tripulacion({ op, editable, onAgregar }: { op: OperacionResponse; editable: boolean; onAgregar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const { usuario } = useAuth()
  const quitar = useMutation({
    mutationFn: (personalId: string) => viajesService.quitarTripulante(op.id, personalId),
    onSuccess: (r) => {
      queryClient.setQueryData(['operacion', op.id], r)
      queryClient.invalidateQueries({ queryKey: ['personal'] })
      avisar('Tripulante retirado del viaje.')
    },
    onError: (e) => avisar(e.message, 'error'),
  })
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Tripulación" icono={<IdCard />}
        acciones={editable ? <Boton variante="suave" pequeno icono={<UserPlus className="size-3.5" />} onClick={onAgregar}>Agregar</Boton> : undefined} />
      <ul className="m-0 p-0 list-none flex flex-col gap-3">
        {op.tripulacion.map((t) => (
          <li key={t.id} className="flex items-center gap-3">
            <Avatar nombre={t.nombre} />
            <div className="flex-1 min-w-0">
              <div className="font-bold text-[14px]">{t.nombre}</div>
              <div className="text-[12px] text-texto-2">{ROL[t.rol]}</div>
            </div>
            {editable && t.id !== usuario?.personalId && (
              <Boton variante="fantasma" pequeno soloIcono aria-label={`Retirar a ${t.nombre}`} cargando={quitar.isPending && quitar.variables === t.id} onClick={() => quitar.mutate(t.id)}>
                <UserMinus className="size-4" />
              </Boton>
            )}
          </li>
        ))}
      </ul>
      {editable && <p className="m-0 text-[12px] text-texto-2">El viaje debe conservar 2 operadores de embarcación y al menos un guía.</p>}
    </Tarjeta>
  )
}

function DialogoTripulante({ op, onCerrar }: { op: OperacionResponse; onCerrar: () => void }) {
  const disponibles = useTripulacionDisponible()
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [personalId, setPersonalId] = useState('')
  const m = useMutation({
    mutationFn: () => viajesService.agregarTripulante(op.id, personalId),
    onSuccess: (r) => {
      queryClient.setQueryData(['operacion', op.id], r)
      queryClient.invalidateQueries({ queryKey: ['personal'] })
      avisar('Tripulante agregado.')
      onCerrar()
    },
  })
  const opciones = (disponibles.data ?? []).filter((p) => !op.tripulacion.some((t) => t.id === p.id))
  return (
    <Modal abierto onCerrar={onCerrar} titulo="Agregar tripulante" ancho="sm"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" disabled={!personalId} cargando={m.isPending} onClick={() => m.mutate()}>Agregar</Boton></>}>
      {disponibles.isLoading ? <Cargando alto={60} /> : disponibles.error ? <ErrorCarga error={disponibles.error} /> : (
        <Selector aria-label="Tripulante" vacio="Seleccione…" value={personalId} onChange={(e) => setPersonalId(e.target.value)}
          opciones={opciones.map((p) => ({ valor: p.id, etiqueta: `${p.nombres} ${p.apellidos} · ${ROL[p.rol]}` }))} />
      )}
      {m.error && <p role="alert" className="m-0 text-[13px] text-error">{m.error.message}</p>}
    </Modal>
  )
}

function PasajerosRegistrados({ cargando, error, datos }: { cargando: boolean; error: unknown; datos?: Awaited<ReturnType<typeof usuariosService.porOperacion>> }) {
  return (
    <Tarjeta sinRelleno>
      <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Usuarios registrados en taquilla" icono={<UsersRound />} subtitulo="Abordajes de este viaje (numeral 4.2)" /></div>
      {cargando ? <Cargando alto={80} /> : error ? <ErrorCarga error={error} /> : !datos?.length ? (
        <p className="m-0 px-5 pb-5 text-[13px] text-texto-2">Aún no hay usuarios registrados para este viaje.</p>
      ) : (
        <div className="overflow-x-auto px-5 pb-5">
          <table className="tabla min-w-[640px] border border-borde">
            <thead><tr><th>Hora</th><th>Abordaje</th><th className="text-right">Usuarios</th><th>Por sexo</th><th>Por destino</th><th>Registró</th></tr></thead>
            <tbody>
              {datos.map((u) => (
                <tr key={u.id}>
                  <td>{hora(u.fechaHora)}</td>
                  <td>{u.embarcaderoAbordaje?.codigo ? EMBARCADERO[u.embarcaderoAbordaje.codigo] : '—'}</td>
                  <td className="text-right font-bold">{entero(u.usuariosTransportados)}</td>
                  <td>{Object.entries(u.porSexo ?? {}).map(([k, v]) => `${SEXO[k as Sexo]}: ${v}`).join(' · ')}</td>
                  <td>{Object.entries(u.porDestino ?? {}).filter(([, v]) => v).map(([k, v]) => `${EMBARCADERO[k as Embarcaderos]}: ${v}`).join(' · ')}</td>
                  <td>{u.registradoPor ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="m-0 mt-2 text-[12px] text-texto-2">
            Grupos etarios: {(['PRIMERA_INFANCIA', 'INFANCIA', 'ADOLESCENCIA', 'JUVENTUD', 'ADULTEZ', 'PERSONA_MAYOR'] as GrupoEtario[])
              .map((g) => `${GRUPO_ETARIO_CORTO[g]}: ${datos.reduce((s, u) => s + (u.porGrupoEtario?.[g] ?? 0), 0)}`).join(' · ')}
          </p>
        </div>
      )}
    </Tarjeta>
  )
}
