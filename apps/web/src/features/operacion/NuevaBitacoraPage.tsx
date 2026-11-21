import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState, type ReactNode } from 'react'
import { Check, CircleCheck, Info } from 'lucide-react'
import { useNavigate } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { useAuth } from '../../app/providers/auth-context'
import { Cargando, ErrorCarga, ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Alerta } from '../../components/ui/Alerta'
import { Avatar } from '../../components/ui/Avatar'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Casilla, Entrada, Selector } from '../../components/ui/Campo'
import { cn } from '../../components/ui/cn'
import { Insignia } from '../../components/ui/Insignia'
import { Chips, Segmentado } from '../../components/ui/Selecciones'
import { PasoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useEmbarcaciones, useMotores, useTripulacionDisponible } from '../../hooks/consultas'
import { viajesService } from '../../services/modules/viajes.service'
import type { CrearOperacionRequest, VerificacionPrevia } from '../../types/api'
import {
  CONDICION_CLIMATICA, EMBARCACION, EMBARCADERO, ESTADO_EMBARCACION, ESTADO_NAVE, NIVEL_RIO, ROL, opciones,
  type CondicionClimatica, type Embarcaderos, type EstadoNave, type NivelRio,
} from '../../types/enums'
import { ahoraLocal, decimal, instanteSiCambio, localAInstant } from '../../utils/formato'
import { NovedadesMecanicas } from './NovedadesMecanicas'
import { aDTO, validarNovedades, type NovedadEditable } from './novedades'

/** Los siete puntos de VerificacionPreviaDTO, agrupados como en la plantilla 04. */
const VERIFICACION: { grupo: string; items: { clave: keyof VerificacionPrevia; etiqueta: string }[] }[] = [
  { grupo: 'Motor y fluidos', items: [
    { clave: 'aceiteMotor', etiqueta: 'Aceite de motor' },
    { clave: 'combustible', etiqueta: 'Combustible suficiente' },
    { clave: 'sistemaDireccion', etiqueta: 'Sistema de dirección' },
    { clave: 'bombaAchique', etiqueta: 'Bomba de achique' },
  ] },
  { grupo: 'Seguridad y comunicaciones', items: [
    { clave: 'chalecosSalvavidas', etiqueta: 'Chalecos salvavidas a bordo' },
    { clave: 'lucesNavegacion', etiqueta: 'Luces de navegación' },
    { clave: 'radioComunicacion', etiqueta: 'Radio de comunicación' },
  ] },
]
const VERIF_INICIAL: VerificacionPrevia = {
  aceiteMotor: true, combustible: true, sistemaDireccion: true, bombaAchique: true, lucesNavegacion: true, chalecosSalvavidas: true, radioComunicacion: true,
}
const SECCIONES = ['Información general', 'Tripulación', 'Recorrido', 'Verificación previa', 'Condiciones de operación'] as const

/** Plantilla 04 · Nueva bitácora de viaje (POST /operaciones). */
export function NuevaBitacoraPage() {
  const { usuario } = useAuth()
  const navegar = useNavigate()
  const { avisar } = useAvisos()
  const queryClient = useQueryClient()
  const embarcaciones = useEmbarcaciones()
  const tripulacion = useTripulacionDisponible()

  const [embarcacionId, setEmbarcacionId] = useState('')
  const [inicioInicial] = useState(ahoraLocal)
  const [inicio, setInicio] = useState(inicioInicial)
  const [tripulantes, setTripulantes] = useState<string[]>([])
  const [origen, setOrigen] = useState<Embarcaderos | ''>('')
  const [destino, setDestino] = useState<Embarcaderos | ''>('')
  const [estadoNave, setEstadoNave] = useState<EstadoNave>('BUENO')
  const [verificacion, setVerificacion] = useState<VerificacionPrevia>(VERIF_INICIAL)
  const [justificacion, setJustificacion] = useState('')
  const [clima, setClima] = useState<CondicionClimatica | ''>('')
  const [nivelRio, setNivelRio] = useState<NivelRio | ''>('')
  const [novedades, setNovedades] = useState<NovedadEditable[]>([])
  const [horometros, setHorometros] = useState<Record<string, string>>({})
  const [intentado, setIntentado] = useState(false)

  const motores = useMotores(embarcacionId || undefined)
  const embarcacion = embarcaciones.data?.find((e) => e.id === embarcacionId)
  const soyOperador = usuario?.rol === 'OPERADOR_EMBARCACION'

  // El operador que registra va a bordo: el backend lo agrega a la tripulación.
  const seleccion = useMemo(() => tripulacion.data?.filter((p) => tripulantes.includes(p.id)) ?? [], [tripulacion.data, tripulantes])
  const operadores = seleccion.filter((p) => p.rol === 'OPERADOR_EMBARCACION').length + (soyOperador && !tripulantes.includes(usuario!.personalId) ? 1 : 0)
  const guias = seleccion.filter((p) => p.rol === 'GUIA_TURISTICA').length
  const todoConforme = Object.values(verificacion).every(Boolean)

  const errores = useMemo(() => {
    const e: Record<string, string> = {}
    if (!embarcacionId) e.embarcacion = 'Seleccione la embarcación'
    else if (embarcacion && embarcacion.estado !== 'OPERATIVA') e.embarcacion = `La embarcación está ${ESTADO_EMBARCACION[embarcacion.estado].toLowerCase()}`
    if (inicio && new Date(localAInstant(inicio)) > new Date()) e.inicio = 'La hora de zarpe no puede ser futura'
    if (operadores < 2) e.tripulacion = 'Se requieren 2 operadores de embarcación'
    else if (guias < 1) e.tripulacion = 'La tripulación debe incluir un guía de turismo (§7.1.4)'
    if (!origen) e.origen = 'Seleccione el embarcadero de origen'
    if (!destino) e.destino = 'Seleccione el embarcadero de destino'
    if (origen && destino && origen === destino) e.destino = 'El destino debe ser distinto del origen'
    if (estadoNave === 'MALO') e.estadoNave = 'La embarcación no puede zarpar en estado malo'
    if (!verificacion.chalecosSalvavidas) e.verificacion = 'No es posible zarpar sin los chalecos salvavidas requeridos'
    else if (!todoConforme && !justificacion.trim()) e.justificacion = 'Hay puntos sin cumplir: indique la justificación para zarpar'
    if (!clima) e.clima = 'Seleccione la condición climática'
    if (!nivelRio) e.nivelRio = 'Seleccione el nivel del río'
    motores.data?.forEach((m) => {
      const v = horometros[m.id]
      if ((v === undefined || v === '') && m.horometroActual === null) e[`h-${m.id}`] = 'Registre la lectura inicial'
      else if (v && m.horometroActual !== null && Number(v) < m.horometroActual) e[`h-${m.id}`] = `No puede ser menor que ${decimal(m.horometroActual)} h`
    })
    const nov = validarNovedades(novedades)
    if (Object.keys(nov).length) e.novedades = 'Revise las novedades mecánicas'
    return { campos: e, novedades: nov }
  }, [embarcacionId, embarcacion, inicio, operadores, guias, origen, destino, estadoNave, verificacion, todoConforme, justificacion, clima, nivelRio, motores.data, horometros, novedades])
  const err = (k: string) => (intentado ? errores.campos[k] : undefined)

  const completas = [
    !!embarcacionId && !errores.campos.embarcacion,
    !errores.campos.tripulacion,
    !!origen && !!destino && !errores.campos.destino,
    !errores.campos.verificacion && !errores.campos.justificacion && !errores.campos.estadoNave,
    !!clima && !!nivelRio && !errores.campos.novedades,
  ]

  const crear = useMutation({
    mutationFn: (d: CrearOperacionRequest) => viajesService.crear(d),
    onSuccess: (op) => {
      queryClient.invalidateQueries({ queryKey: ['operaciones'] })
      queryClient.invalidateQueries({ queryKey: ['personal'] })
      avisar('Bitácora registrada: el viaje quedó en curso.')
      navegar(`/viajes/${op.id}`, { replace: true })
    },
  })

  const guardar = () => {
    setIntentado(true)
    if (Object.keys(errores.campos).length) return
    crear.mutate({
      embarcacionId,
      embarcaderoOrigen: origen as Embarcaderos,
      embarcaderoDestino: destino as Embarcaderos,
      fechaHoraInicio: instanteSiCambio(inicio, inicioInicial),
      estadoNave,
      verificacion,
      justificacionVerificacion: todoConforme ? null : justificacion.trim(),
      condicionClimatica: clima as CondicionClimatica,
      nivelRio: nivelRio as NivelRio,
      condicionesMecanicas: aDTO(novedades),
      tripulacionIds: tripulantes.length ? tripulantes : [usuario!.personalId],
      lecturasHorometro: Object.entries(horometros).filter(([, v]) => v !== '').map(([motorId, v]) => ({ motorId, lectura: Number(v) })),
    })
  }

  if (embarcaciones.isLoading || tripulacion.isLoading) return <Pagina titulo="Nueva bitácora de viaje"><Cargando alto={320} /></Pagina>
  if (embarcaciones.error || tripulacion.error) {
    return <Pagina titulo="Nueva bitácora de viaje"><Tarjeta><ErrorCarga error={embarcaciones.error ?? tripulacion.error} onReintentar={() => { embarcaciones.refetch(); tripulacion.refetch() }} /></Tarjeta></Pagina>
  }

  return (
    <Pagina
      titulo="Nueva bitácora de viaje"
      migas={[{ etiqueta: 'Operación' }, { etiqueta: 'Viajes', a: '/viajes' }, { etiqueta: 'Nueva bitácora' }]}
      descripcion={<>Registra la información del zarpe. Los campos con <span className="text-error">*</span> son obligatorios.</>}
    >
      <div className="grid lg:grid-cols-[280px_minmax(0,1fr)] gap-5 items-start">
        <Tarjeta className="lg:sticky lg:top-[96px]">
          <div>
            <h2 className="m-0 font-titulo text-[16px] font-normal text-noche">Secciones de la bitácora</h2>
            <p className="m-0 mt-0.5 text-[12px] text-texto-2">{completas.filter(Boolean).length} de {SECCIONES.length} secciones completas</p>
            <div className="h-1.5 mt-2 rounded-full bg-fondo overflow-hidden" aria-hidden>
              <div className="h-full bg-exito rounded-full transition-all" style={{ width: `${(completas.filter(Boolean).length / SECCIONES.length) * 100}%` }} />
            </div>
          </div>
          <ol className="m-0 p-0 list-none flex flex-col gap-1">
            {SECCIONES.map((s, i) => (
              <li key={s}>
                <a href={`#seccion-${i + 1}`} className="flex items-center gap-2.5 px-2.5 py-2 rounded-[10px] no-underline text-negro hover:bg-fondo text-[13.5px]">
                  {completas[i]
                    ? <CircleCheck className="size-[22px] text-white fill-exito" aria-label="Completa" />
                    : <span className="size-[22px] rounded-full border border-borde text-[12px] text-texto-2 flex items-center justify-center">{i + 1}</span>}
                  {s}
                </a>
              </li>
            ))}
          </ol>
          <Alerta tono="dorado">Se diligencia una bitácora por cada operación realizada (numeral 4.1). Los pasajeros se registran en taquilla.</Alerta>
        </Tarjeta>

        <div className="flex flex-col gap-5 min-w-0">
          <Seccion n={1} titulo="Información general del viaje" subtitulo="Datos de identificación de la operación">
            <div className="grid gap-4 sm:grid-cols-3">
              <Campo etiqueta="Embarcación" obligatorio error={err('embarcacion')}>
                {(p) => (
                  <Selector {...p} vacio="Seleccione…" value={embarcacionId}
                    opciones={(embarcaciones.data ?? []).map((e) => ({ valor: e.id, etiqueta: `${EMBARCACION[e.nombre]} · ${ESTADO_EMBARCACION[e.estado]}` }))}
                    onChange={(e) => { setEmbarcacionId(e.target.value); setHorometros({}) }} />
                )}
              </Campo>
              <Campo etiqueta="Fecha y hora de zarpe" ayuda="Si la deja vacía, se toma la hora actual" error={err('inicio')}>
                {(p) => <Entrada {...p} type="datetime-local" value={inicio} max={ahoraLocal()} onChange={(e) => setInicio(e.target.value)} />}
              </Campo>
              <Campo etiqueta="Registrado por">{(p) => <Entrada {...p} bloqueado value={usuario?.nombre ?? ''} />}</Campo>
            </div>
          </Seccion>

          <Seccion n={2} titulo="Personal y tripulación" subtitulo="Operadores de embarcación y guías activos que no están en otro viaje en curso">
            {soyOperador && <Alerta tono="info">Usted queda a bordo automáticamente como operador de embarcación.</Alerta>}
            {!tripulacion.data?.length ? (
              <Alerta tono="aviso">No hay tripulantes disponibles en este momento.</Alerta>
            ) : (
              <div className="grid gap-3 sm:grid-cols-2">
                {(['OPERADOR_EMBARCACION', 'GUIA_TURISTICA'] as const).map((rol) => (
                  <fieldset key={rol} className="m-0 p-3 rounded-[12px] border border-borde">
                    <legend className="px-1 font-titulo text-[13px] text-azul">{ROL[rol]}</legend>
                    <div className="flex flex-col gap-2">
                      {tripulacion.data!.filter((p) => p.rol === rol).map((p) => (
                        <label key={p.id} className="flex items-center gap-2.5 cursor-pointer text-[14px]">
                          <input type="checkbox" className="size-[18px] accent-azul" checked={tripulantes.includes(p.id) || (soyOperador && p.id === usuario?.personalId)}
                            disabled={soyOperador && p.id === usuario?.personalId}
                            onChange={(e) => setTripulantes((t) => (e.target.checked ? [...t, p.id] : t.filter((x) => x !== p.id)))} />
                          <Avatar nombre={`${p.nombres} ${p.apellidos}`} tamano={28} />
                          {p.nombres} {p.apellidos}
                        </label>
                      ))}
                    </div>
                  </fieldset>
                ))}
              </div>
            )}
            <div className="flex flex-wrap gap-2">
              <Insignia tono={operadores >= 2 ? 'ok' : 'warn'}>{operadores} de 2 operadores</Insignia>
              <Insignia tono={guias >= 1 ? 'ok' : 'warn'}>{guias} guía(s)</Insignia>
            </div>
            {err('tripulacion') && <p role="alert" className="m-0 text-[12px] text-error">{err('tripulacion')}</p>}
          </Seccion>

          <Seccion n={3} titulo="Recorrido" subtitulo="Las paradas intermedias se registran desde el detalle del viaje">
            <div className="grid gap-4 sm:grid-cols-2">
              <Campo etiqueta="Embarcadero de origen" obligatorio error={err('origen')}>
                {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(EMBARCADERO)} value={origen} onChange={(e) => setOrigen(e.target.value as Embarcaderos)} />}
              </Campo>
              <Campo etiqueta="Embarcadero de destino" obligatorio error={err('destino')}>
                {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(EMBARCADERO)} value={destino} onChange={(e) => setDestino(e.target.value as Embarcaderos)} />}
              </Campo>
            </div>
          </Seccion>

          <Seccion n={4} titulo="Verificación previa a la navegación" subtitulo="Estado general de la embarcación y puntos de control antes de zarpar">
            <div className="flex flex-wrap items-center gap-3">
              <span className="etiqueta">Estado general de la embarcación <span className="text-error">*</span></span>
              <Segmentado etiqueta="Estado general" valor={estadoNave} onCambio={setEstadoNave} opciones={opciones(ESTADO_NAVE)} />
            </div>
            {err('estadoNave') && <p role="alert" className="m-0 text-[12px] text-error">{err('estadoNave')}</p>}
            <div className="grid gap-3 md:grid-cols-2">
              {VERIFICACION.map((g) => (
                <div key={g.grupo} className="rounded-[12px] bg-fondo p-3.5 flex flex-col gap-2.5">
                  <span className="font-titulo text-[13px] text-azul">{g.grupo}</span>
                  {g.items.map((it) => (
                    <div key={it.clave} className="flex items-center justify-between gap-2">
                      <Casilla etiqueta={it.etiqueta} checked={verificacion[it.clave]} onChange={(e) => setVerificacion((v) => ({ ...v, [it.clave]: e.target.checked }))} />
                      <Insignia tono={verificacion[it.clave] ? 'ok' : 'warn'}>{verificacion[it.clave] ? 'Conforme' : 'Revisar'}</Insignia>
                    </div>
                  ))}
                </div>
              ))}
            </div>
            {err('verificacion') && <Alerta tono="error">{err('verificacion')}</Alerta>}
            {!todoConforme && (
              <Campo etiqueta="Justificación para zarpar" obligatorio error={err('justificacion')}>
                {(p) => <AreaTexto {...p} maxLength={1000} value={justificacion} onChange={(e) => setJustificacion(e.target.value)} placeholder="Explique por qué es seguro zarpar con los puntos marcados como «Revisar»" />}
              </Campo>
            )}
            {!!motores.data?.length && (
              <div className="grid gap-4 sm:grid-cols-2">
                {motores.data.map((m) => (
                  <Campo key={m.id} etiqueta={`Horómetro inicial · ${m.marca} ${m.serial}`} obligatorio={m.horometroActual === null}
                    ayuda={m.horometroActual !== null ? `Última lectura: ${decimal(m.horometroActual)} h. Déjelo vacío si no cambió.` : undefined}
                    error={err(`h-${m.id}`)}>
                    {(p) => <Entrada {...p} type="number" min={0} step="0.1" inputMode="decimal" sufijo="h" value={horometros[m.id] ?? ''} onChange={(e) => setHorometros((h) => ({ ...h, [m.id]: e.target.value }))} />}
                  </Campo>
                ))}
              </div>
            )}
          </Seccion>

          <Seccion n={5} titulo="Condiciones de operación" subtitulo="Clima, nivel del río y condiciones mecánicas al zarpar">
            <div className="flex flex-col gap-2">
              <span className="etiqueta">Condiciones climáticas observadas <span className="text-error">*</span></span>
              <Chips etiqueta="Condición climática" opciones={opciones(CONDICION_CLIMATICA)} valor={clima} onCambio={setClima} />
              {err('clima') && <p role="alert" className="m-0 text-[12px] text-error">{err('clima')}</p>}
            </div>
            <div className="grid gap-4 sm:grid-cols-3">
              <Campo etiqueta="Nivel del río" obligatorio error={err('nivelRio')}>
                {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(NIVEL_RIO)} value={nivelRio} onChange={(e) => setNivelRio(e.target.value as NivelRio)} />}
              </Campo>
            </div>
            <div className="flex flex-col gap-2">
              <span className="etiqueta">Condiciones mecánicas y operacionales</span>
              <NovedadesMecanicas valor={novedades} onCambio={setNovedades} errores={intentado ? errores.novedades : undefined} />
            </div>
            <Alerta tono="info">Los abastecimientos de combustible hechos desde el viaje anterior quedan asociados a esta bitácora automáticamente (R16).</Alerta>
          </Seccion>

          <Tarjeta className="!flex-row flex-wrap items-center gap-3 sm:sticky sm:bottom-3 z-10 shadow-flotante">
            <p className="m-0 flex-1 min-w-[220px] flex items-start gap-2 text-[12.5px] text-texto-2">
              <Info className="size-4 flex-none mt-px" aria-hidden />
              Al guardar, el viaje queda en curso. Las paradas, novedades y el cierre se registran desde su detalle.
            </p>
            {intentado && Object.keys(errores.campos).length > 0 && <span className="text-[12.5px] text-error font-bold">Revise {Object.keys(errores.campos).length} campo(s)</span>}
            <Boton variante="fantasma" onClick={() => navegar('/viajes')}>Cancelar</Boton>
            <Boton variante="institucional" icono={<Check className="size-4" />} cargando={crear.isPending} onClick={guardar}>Guardar bitácora</Boton>
            {crear.error && <div className="basis-full"><ErrorFormulario error={crear.error} /></div>}
          </Tarjeta>
        </div>
      </div>
    </Pagina>
  )
}

function Seccion({ n, titulo, subtitulo, children }: { n: number; titulo: string; subtitulo?: string; children: ReactNode }) {
  return (
    <Tarjeta id={`seccion-${n}`} className={cn('scroll-mt-[96px]')}>
      <PasoTarjeta numero={n} titulo={titulo} subtitulo={subtitulo} />
      {children}
    </Tarjeta>
  )
}
