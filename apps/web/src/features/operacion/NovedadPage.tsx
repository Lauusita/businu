import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Check, CloudSun, Route, ShieldAlert, TriangleAlert, Wrench } from 'lucide-react'
import { useNavigate, useSearchParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, ErrorFormulario, Vacio } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Interruptor, Selector } from '../../components/ui/Campo'
import { CajaIcono } from '../../components/ui/CajaIcono'
import { Insignia } from '../../components/ui/Insignia'
import { SeveridadInsignia } from '../../components/ui/InsigniasEstado'
import { Chips, OpcionTarjeta } from '../../components/ui/Selecciones'
import { EncabezadoTarjeta, PasoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAbiertas } from '../../hooks/consultas'
import { comunicacionesService } from '../../services/modules/comunicaciones.service'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import { viajesService } from '../../services/modules/viajes.service'
import {
  COMPONENTE, CONDICION_CLIMATICA, CONDICION_MECANICA, EMBARCACION, EMBARCADERO, NIVEL_RIO, SEVERIDAD, TIPO_REGISTRO_INFRA,
  condicionDesdeDescripcion, nombreEmbarcadero, opciones,
  type CondicionClimatica, type CondicionMecanica, type NivelRio, type Severidad,
} from '../../types/enums'
import { codigoCorto, diaMes, hora, textoReal } from '../../utils/formato'
import { FormularioRegistroInfra } from '../infraestructura/FormularioRegistroInfra'

type Tipo = 'MECANICA' | 'OPERACIONAL' | 'CLIMATICA' | 'INFRAESTRUCTURA'
const TIPOS: { valor: Tipo; titulo: string; descripcion: string; icono: React.ReactNode; tono: 'dorado' | 'azul' | 'noche' | 'error' }[] = [
  { valor: 'MECANICA', titulo: 'Mecánica', descripcion: 'Fallas o anomalías de la embarcación', icono: <Wrench />, tono: 'dorado' },
  { valor: 'OPERACIONAL', titulo: 'Operacional', descripcion: 'Afecta recorridos, horarios o atención', icono: <Route />, tono: 'azul' },
  { valor: 'CLIMATICA', titulo: 'Climática', descripcion: 'Lluvia, viento, nivel del río', icono: <CloudSun />, tono: 'noche' },
  { valor: 'INFRAESTRUCTURA', titulo: 'Seguridad e infraestructura', descripcion: 'Embarcaderos y ayudas a la navegación', icono: <ShieldAlert />, tono: 'error' },
]

/**
 * Plantilla 19 · Registrar novedad. El backend registra las novedades en dos lugares:
 * - las del viaje (mecánicas, operacionales, climáticas) en la bitácora: PATCH /operaciones/{id};
 * - las de embarcaderos y ayudas en POST /infraestructura/registros (tipo NOVEDAD).
 */
export function NovedadPage() {
  const [params] = useSearchParams()
  const [tipo, setTipo] = useState<Tipo>((params.get('tipo') as Tipo) || 'MECANICA')
  return (
    <Pagina
      titulo="Registrar novedad"
      migas={[{ etiqueta: 'Operación', a: '/viajes' }, { etiqueta: 'Novedades' }, { etiqueta: 'Nueva' }]}
      descripcion="Novedades mecánicas, operacionales, climáticas y de seguridad."
    >
      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <PasoTarjeta numero={1} titulo="Tipo de novedad" subtitulo="Selecciona la categoría que mejor la describe" />
            <div className="grid gap-3 sm:grid-cols-2" role="radiogroup" aria-label="Tipo de novedad">
              {TIPOS.map((t) => (
                <OpcionTarjeta key={t.valor} seleccionada={tipo === t.valor} onClick={() => setTipo(t.valor)} titulo={t.titulo} descripcion={t.descripcion}
                  icono={<CajaIcono tono={t.tono === 'error' ? 'error' : t.tono}>{t.icono}</CajaIcono>} />
              ))}
            </div>
          </Tarjeta>
          {tipo === 'INFRAESTRUCTURA' ? (
            <Tarjeta>
              <PasoTarjeta numero={2} titulo="Descripción y evidencias" subtitulo="Queda como registro de infraestructura tipo «Novedad» (numeral 4.4)" />
              <FormularioRegistroInfra tipoFijo="NOVEDAD" />
            </Tarjeta>
          ) : (
            <NovedadDeViaje tipo={tipo} />
          )}
        </div>
        <NovedadesAbiertas />
      </div>
    </Pagina>
  )
}

function NovedadDeViaje({ tipo }: { tipo: Exclude<Tipo, 'INFRAESTRUCTURA'> }) {
  const navegar = useNavigate()
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const abiertas = useAbiertas()
  const [viajeId, setViajeId] = useState('')
  const [condicion, setCondicion] = useState<CondicionMecanica | ''>('')
  const [severidad, setSeveridad] = useState<Severidad | ''>('')
  const [descripcion, setDescripcion] = useState('')
  const [medidas, setMedidas] = useState('')
  const [clima, setClima] = useState<CondicionClimatica | ''>('')
  const [nivel, setNivel] = useState<NivelRio | ''>('')
  const [comunicar, setComunicar] = useState(false)
  const [intentado, setIntentado] = useState(false)

  const errores: Record<string, string> = {}
  if (!viajeId) errores.viaje = 'Seleccione el viaje en curso'
  if (tipo === 'MECANICA') {
    if (!condicion) errores.condicion = 'Seleccione la condición'
    if (!severidad) errores.severidad = 'Seleccione la severidad'
    if (condicion === 'OTRA' && !descripcion.trim()) errores.descripcion = 'Describa la condición «Otra»'
  } else if (!descripcion.trim()) errores.descripcion = 'Describa la novedad'
  if (tipo === 'CLIMATICA' && !clima && !nivel && !descripcion.trim()) errores.clima = 'Indique el clima, el nivel del río o una descripción'

  const guardar = useMutation({
    mutationFn: async () => {
      const op = await viajesService.obtener(viajeId) // estado actual: la lista se reemplaza completa
      const marca = `[${hora(new Date().toISOString())}]`
      const etiqueta = tipo === 'OPERACIONAL' ? 'Operacional' : 'Climática'
      const texto = [descripcion.trim(), medidas.trim() && `Medidas: ${medidas.trim()}`].filter(Boolean).join(' · ')
      const r = await viajesService.actualizar(viajeId, tipo === 'MECANICA'
        ? {
            condicionesMecanicas: [
              ...op.condicionesMecanicas.map((n) => ({ condicion: condicionDesdeDescripcion(n.condicion) ?? 'OTRA', severidad: n.severidad, detalle: n.detalle ?? null })),
              { condicion: condicion as string, severidad: severidad as Severidad, detalle: texto || null },
            ],
          }
        : {
            novedades: [textoReal(op.novedades), `${marca} ${etiqueta}: ${texto}`].filter(Boolean).join('\n'),
            ...(tipo === 'CLIMATICA' && clima ? { condicionClimatica: clima } : {}),
            ...(tipo === 'CLIMATICA' && nivel ? { nivelRio: nivel } : {}),
          })
      let envio: 'ENVIADO' | 'FALLIDO' | 'PENDIENTE' | null = null
      if (comunicar) {
        const c = await comunicacionesService.registrar({
          referenciaTipo: 'OPERACION', referenciaId: viajeId,
          asunto: `Novedad ${tipo.toLowerCase()} · viaje ${codigoCorto('V', viajeId)} · ${EMBARCACION[r.embarcacion.nombre]}`,
          mensaje: tipo === 'MECANICA' ? `${CONDICION_MECANICA[condicion as CondicionMecanica]} (${SEVERIDAD[severidad as Severidad]}). ${texto}` : texto,
        })
        envio = c.estado
      }
      return { r, envio }
    },
    onSuccess: ({ r, envio }) => {
      queryClient.setQueryData(['operacion', r.id], r)
      queryClient.invalidateQueries({ queryKey: ['operaciones'] })
      queryClient.invalidateQueries({ queryKey: ['comunicaciones', 'OPERACION', r.id] })
      avisar(envio === 'FALLIDO' ? 'Novedad registrada, pero el correo al supervisor falló: reinténtelo desde el viaje.' : 'Novedad registrada en la bitácora.', envio === 'FALLIDO' ? 'error' : 'exito')
      navegar(`/viajes/${r.id}`)
    },
  })

  return (
    <>
      <Tarjeta>
        <PasoTarjeta numero={2} titulo="Descripción" subtitulo="Qué pasó y en qué viaje" />
        {abiertas.isLoading ? <Cargando alto={60} /> : abiertas.error ? <ErrorCarga error={abiertas.error} /> : !abiertas.data?.length ? (
          <Alerta tono="aviso" titulo="No hay viajes en curso">Las novedades del viaje se registran en la bitácora de un viaje en curso. Para un embarcadero o una ayuda, elija «Seguridad e infraestructura».</Alerta>
        ) : (
          <Campo etiqueta="Viaje en curso" obligatorio error={intentado ? errores.viaje : undefined}>
            {(p) => (
              <Selector {...p} vacio="Seleccione…" value={viajeId} onChange={(e) => setViajeId(e.target.value)}
                opciones={abiertas.data.map((o) => ({ valor: o.id, etiqueta: `${codigoCorto('V', o.id)} · ${EMBARCACION[o.embarcacion]} · ${nombreEmbarcadero(o.embarcaderoOrigen)} → ${nombreEmbarcadero(o.embarcaderoDestino)} · ${hora(o.fechaHoraInicio)}` }))} />
            )}
          </Campo>
        )}
        {tipo === 'MECANICA' && (
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo etiqueta="Condición mecánica" obligatorio error={intentado ? errores.condicion : undefined}>
              {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(CONDICION_MECANICA)} value={condicion} onChange={(e) => setCondicion(e.target.value as CondicionMecanica)} />}
            </Campo>
            <div className="flex flex-col gap-1.5">
              <span className="etiqueta">Severidad <span className="text-error">*</span></span>
              <Chips etiqueta="Severidad" opciones={opciones(SEVERIDAD)} valor={severidad} onCambio={setSeveridad} />
              {intentado && errores.severidad && <p role="alert" className="m-0 text-[12px] text-error">{errores.severidad}</p>}
            </div>
          </div>
        )}
        {tipo === 'CLIMATICA' && (
          <div className="grid gap-4 sm:grid-cols-[2fr_1fr]">
            <div className="flex flex-col gap-1.5">
              <span className="etiqueta">Condición climática observada</span>
              <Chips etiqueta="Condición climática" opciones={opciones(CONDICION_CLIMATICA)} valor={clima} onCambio={setClima} />
            </div>
            <Campo etiqueta="Nivel del río">{(p) => <Selector {...p} vacio="Sin cambio" opciones={opciones(NIVEL_RIO)} value={nivel} onChange={(e) => setNivel(e.target.value as NivelRio)} />}</Campo>
          </div>
        )}
        <Campo etiqueta="Descripción de la novedad" obligatorio={tipo !== 'MECANICA' || condicion === 'OTRA'} error={intentado ? errores.descripcion ?? errores.clima : undefined}>
          {(p) => <AreaTexto {...p} maxLength={1500} value={descripcion} onChange={(e) => setDescripcion(e.target.value)} placeholder="Qué pasó, dónde y a qué hora" />}
        </Campo>
      </Tarjeta>
      <Tarjeta>
        <PasoTarjeta numero={3} titulo="Medidas adoptadas" subtitulo="Acciones inmediatas dentro del ámbito del operador" />
        <Campo etiqueta="Medidas adoptadas" opcional>{(p) => <AreaTexto {...p} maxLength={400} value={medidas} onChange={(e) => setMedidas(e.target.value)} />}</Campo>
        <Interruptor activo={comunicar} onCambio={setComunicar} etiqueta="¿Afecta la continuidad del servicio?" descripcion="Se enviará un correo al supervisor del contrato y quedará constancia (§7.1.12)" />
      </Tarjeta>
      <Tarjeta className="!flex-row flex-wrap items-center justify-end gap-3">
        {guardar.error && <div className="basis-full"><ErrorFormulario error={guardar.error} /></div>}
        <Boton variante="fantasma" onClick={() => navegar(-1)}>Cancelar</Boton>
        <Boton variante="institucional" icono={<Check className="size-4" />} cargando={guardar.isPending} disabled={!abiertas.data?.length}
          onClick={() => { setIntentado(true); if (!Object.keys(errores).length) guardar.mutate() }}>
          Registrar novedad
        </Boton>
      </Tarjeta>
    </>
  )
}

function NovedadesAbiertas() {
  const q = useQuery({
    queryKey: ['infraestructura', 'registros', { estado: 'ABIERTO', size: 6 }],
    queryFn: () => infraestructuraService.registros({ estado: 'ABIERTO' }, { size: 6 }),
  })
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Novedades abiertas" subtitulo="Embarcaderos y ayudas a la navegación" icono={<TriangleAlert />} tono="error"
        acciones={q.data?.page.totalElements ? <Insignia tono="err">{q.data.page.totalElements}</Insignia> : undefined} />
      {q.isLoading ? <Cargando /> : q.error ? <ErrorCarga error={q.error} /> : !q.data?.content.length ? <Vacio titulo="Sin novedades abiertas" /> : (
        <ul className="m-0 p-0 list-none flex flex-col divide-y divide-borde">
          {q.data.content.map((r) => (
            <li key={r.id} className="py-3 first:pt-0">
              <div className="flex items-center justify-between gap-2">
                <Insignia tono="warn">{TIPO_REGISTRO_INFRA[r.tipo]}</Insignia>
                <span className="text-[12px] text-texto-2">{diaMes(r.fechaHora)}</span>
              </div>
              <div className="font-bold text-[13.5px] mt-1.5">{COMPONENTE[r.componente].etiqueta} · {r.embarcadero ? EMBARCADERO[r.embarcadero] : r.ayuda?.codigo}</div>
              <div className="text-[12.5px] text-texto-2 line-clamp-2">{r.descripcion}</div>
              <div className="mt-1"><SeveridadInsignia severidad={r.severidad} /></div>
            </li>
          ))}
        </ul>
      )}
      <Boton a="/infraestructura" variante="secundario" className="w-full">Ver todas</Boton>
    </Tarjeta>
  )
}
