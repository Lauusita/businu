import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { ArrowRight, Check, History, Minus, Plus, Route, Ship, UsersRound, X } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, ErrorFormulario, Vacio } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada, Selector } from '../../components/ui/Campo'
import { Insignia } from '../../components/ui/Insignia'
import { Chips } from '../../components/ui/Selecciones'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAbiertas } from '../../hooks/consultas'
import { usePermiso } from '../../hooks/usePermiso'
import { usuariosService } from '../../services/modules/usuarios.service'
import { viajesService } from '../../services/modules/viajes.service'
import type { UsuarioRequest } from '../../types/api'
import {
  EMBARCACION, EMBARCADERO, EMBARCADEROS, GRUPO_ETARIO, GRUPO_ETARIO_CORTO, SEXO, embarcaderoDesdeDescripcion, opciones,
  type Embarcaderos, type GrupoEtario, type Sexo,
} from '../../types/enums'
import { ahoraLocal, codigoCorto, entero, hora, hoyISO, localAInstant } from '../../utils/formato'

const GRUPOS = Object.keys(GRUPO_ETARIO) as GrupoEtario[]
const SEXOS: Sexo[] = ['FEMENINO', 'MASCULINO']
type Matriz = Record<Sexo, Record<GrupoEtario, number>>
const matrizVacia = (): Matriz => ({
  FEMENINO: Object.fromEntries(GRUPOS.map((g) => [g, 0])) as Record<GrupoEtario, number>,
  MASCULINO: Object.fromEntries(GRUPOS.map((g) => [g, 0])) as Record<GrupoEtario, number>,
})

/**
 * Plantilla 12 · Registro de usuarios en taquilla (POST /pasajeros, numeral 4.2).
 * El backend recibe totales por sexo, grupo etario y destino que deben sumar lo mismo.
 */
export function RegistroUsuariosPage() {
  const veCaracterizacion = usePermiso('usuarios.caracterizacion')
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [abordaje, setAbordaje] = useState<Embarcaderos | ''>('')
  const [operacionId, setOperacionId] = useState('')
  const [fechaHora, setFechaHora] = useState('')
  const abiertas = useAbiertas(abordaje || undefined)
  const op = useQuery({ queryKey: ['operacion', operacionId], queryFn: () => viajesService.obtener(operacionId), enabled: !!operacionId })

  // Registro individual (una persona)
  const [destino1, setDestino1] = useState<Embarcaderos | ''>('')
  const [grupo1, setGrupo1] = useState<GrupoEtario | ''>('')
  const [sexo1, setSexo1] = useState<Sexo | ''>('')
  // Registro por grupo
  const [matriz, setMatriz] = useState<Matriz>(matrizVacia)
  const [destinos, setDestinos] = useState<Partial<Record<Embarcaderos, number>>>({})

  const origen = op.data ? embarcaderoDesdeDescripcion(op.data.embarcaderoOrigen) : undefined
  // Destinos posibles: los embarcaderos que siguen al abordaje en el sentido del viaje
  const posibles = (() => {
    if (!op.data || !abordaje) return EMBARCADEROS.filter((e) => e !== abordaje)
    const fin = embarcaderoDesdeDescripcion(op.data.embarcaderoDestino)
    const io = EMBARCADEROS.indexOf(origen ?? abordaje), ifin = fin ? EMBARCADEROS.indexOf(fin) : -1, ia = EMBARCADEROS.indexOf(abordaje)
    const paso = ifin >= io ? 1 : -1
    return EMBARCADEROS.filter((_, i) => (paso === 1 ? i > ia && i <= ifin : i < ia && i >= ifin))
  })()

  const total = SEXOS.reduce((s, x) => s + GRUPOS.reduce((t, g) => t + matriz[x][g], 0), 0)
  const totalDestinos = Object.values(destinos).reduce((s, v) => s + (v ?? 0), 0)

  const registrar = useMutation({
    mutationFn: (d: UsuarioRequest) => usuariosService.registrar(d),
    onSuccess: (r) => {
      queryClient.invalidateQueries({ queryKey: ['pasajeros'] })
      queryClient.invalidateQueries({ queryKey: ['operacion', operacionId] })
      queryClient.invalidateQueries({ queryKey: ['operaciones'] })
      avisar(`${entero(r.usuariosTransportados)} usuario(s) registrado(s).`)
    },
  })

  const base = () => ({ fechaHora: fechaHora ? localAInstant(fechaHora) : null, operacionId, embarcaderoAbordaje: abordaje as Embarcaderos })
  const listoBase = !!abordaje && !!operacionId

  const registrarUno = () => {
    if (!listoBase || !destino1 || !grupo1 || !sexo1) return
    registrar.mutate(
      { ...base(), usuariosTransportados: 1, porSexo: { [sexo1]: 1 }, porGrupoEtario: { [grupo1]: 1 }, porDestino: { [destino1]: 1 } },
      { onSuccess: () => { setGrupo1(''); setSexo1('') } },
    )
  }
  const registrarGrupo = () => {
    if (!listoBase || !total || totalDestinos !== total) return
    const porSexo = Object.fromEntries(SEXOS.map((x) => [x, GRUPOS.reduce((t, g) => t + matriz[x][g], 0)]))
    const porGrupoEtario = Object.fromEntries(GRUPOS.map((g) => [g, matriz.FEMENINO[g] + matriz.MASCULINO[g]]))
    registrar.mutate(
      { ...base(), usuariosTransportados: total, porSexo, porGrupoEtario, porDestino: Object.fromEntries(Object.entries(destinos).filter(([, v]) => v)) },
      { onSuccess: () => { setMatriz(matrizVacia()); setDestinos({}) } },
    )
  }

  return (
    <Pagina
      titulo="Registro de usuarios"
      migas={[{ etiqueta: 'Usuarios' }, { etiqueta: 'Registro' }]}
      descripcion="Registro de las personas que abordan (numeral 4.2). Pensado para uso rápido en taquilla."
      acciones={veCaracterizacion && <Boton variante="secundario" a="/usuarios">Ver caracterización <ArrowRight className="size-4" /></Boton>}
    >
      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Embarcadero y viaje" subtitulo="Seleccione dónde está registrando y el viaje que está abordando"
              acciones={op.data && <Insignia tono="info">Viaje {codigoCorto('V', op.data.id)} · abordando</Insignia>} />
            <div className="grid gap-4 sm:grid-cols-3">
              <Campo etiqueta="Embarcadero de abordaje" obligatorio>
                {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(EMBARCADERO)} value={abordaje} onChange={(e) => { setAbordaje(e.target.value as Embarcaderos); setOperacionId(''); setDestinos({}); setDestino1('') }} />}
              </Campo>
              <Campo etiqueta="Viaje en curso" obligatorio ayuda={abordaje && !abiertas.isLoading && !abiertas.data?.length ? 'No hay viajes en curso que pasen por este embarcadero' : undefined}>
                {(p) => (
                  <Selector {...p} disabled={!abordaje} vacio={!abordaje ? 'Elija el embarcadero' : abiertas.isLoading ? 'Cargando…' : 'Seleccione…'} value={operacionId}
                    onChange={(e) => { setOperacionId(e.target.value); setDestinos({}); setDestino1('') }}
                    opciones={(abiertas.data ?? []).map((o) => ({ valor: o.id, etiqueta: `${codigoCorto('V', o.id)} · ${EMBARCACION[o.embarcacion]} · ${hora(o.fechaHoraInicio)}` }))} />
                )}
              </Campo>
              <Campo etiqueta="Fecha y hora" ayuda="Vacía = ahora">
                {(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={fechaHora} onChange={(e) => setFechaHora(e.target.value)} />}
              </Campo>
            </div>
            {abiertas.error && <ErrorCarga error={abiertas.error} />}
          </Tarjeta>

          <Tarjeta>
            <EncabezadoTarjeta titulo="Nuevo registro de usuario" subtitulo="Una persona: destino, grupo etario y sexo" />
            <div className="flex flex-col gap-1.5">
              <span className="etiqueta">Destino <span className="text-error">*</span></span>
              <Chips etiqueta="Destino" opciones={posibles.map((e) => ({ valor: e, etiqueta: EMBARCADERO[e] }))} valor={destino1} onCambio={setDestino1} />
            </div>
            <div className="flex flex-col gap-1.5">
              <span className="etiqueta">Edad o grupo etario <span className="text-error">*</span></span>
              <Chips etiqueta="Grupo etario" opciones={opciones(GRUPO_ETARIO)} valor={grupo1} onCambio={setGrupo1} />
            </div>
            <div className="flex flex-col gap-1.5">
              <span className="etiqueta">Sexo <span className="text-error">*</span></span>
              <Chips etiqueta="Sexo" opciones={SEXOS.map((s) => ({ valor: s, etiqueta: SEXO[s] }))} valor={sexo1} onCambio={setSexo1} />
            </div>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <Boton variante="fantasma" icono={<X className="size-4" />} onClick={() => { setDestino1(''); setGrupo1(''); setSexo1('') }}>Limpiar</Boton>
              <Boton variante="institucional" disabled={!listoBase || !destino1 || !grupo1 || !sexo1} cargando={registrar.isPending} onClick={registrarUno}>
                Registrar y continuar <ArrowRight className="size-4" />
              </Boton>
            </div>
          </Tarjeta>

          <Tarjeta>
            <EncabezadoTarjeta titulo="Registro rápido por grupo" icono={<UsersRound />} subtitulo="Para abordajes masivos: suma usuarios por grupo etario y sexo y registra el grupo completo." />
            <div className="grid gap-5 md:grid-cols-2">
              {SEXOS.map((s) => (
                <div key={s} className="flex flex-col gap-2">
                  <span className="text-[12px] font-extrabold tracking-[.06em] uppercase text-texto-2">{s === 'FEMENINO' ? 'Mujeres' : 'Hombres'}</span>
                  {GRUPOS.map((g) => (
                    <Contador key={g} etiqueta={GRUPO_ETARIO_CORTO[g]} valor={matriz[s][g]}
                      onCambio={(v) => setMatriz((m) => ({ ...m, [s]: { ...m[s], [g]: v } }))} />
                  ))}
                </div>
              ))}
            </div>
            <div className="flex flex-col gap-2 border-t border-borde pt-4">
              <span className="text-[12px] font-extrabold tracking-[.06em] uppercase text-texto-2">Destinos del grupo</span>
              <div className="grid gap-2 md:grid-cols-2">
                {posibles.map((d) => <Contador key={d} etiqueta={EMBARCADERO[d]} valor={destinos[d] ?? 0} onCambio={(v) => setDestinos((x) => ({ ...x, [d]: v }))} />)}
              </div>
              {posibles.length === 1 && total > 0 && totalDestinos !== total && (
                <Boton variante="suave" pequeno className="self-start" onClick={() => setDestinos({ [posibles[0]!]: total })}>Todos a {EMBARCADERO[posibles[0]!]}</Boton>
              )}
            </div>
            {total > 0 && totalDestinos !== total && <Alerta tono="aviso">Los destinos suman {totalDestinos} y el grupo tiene {total} usuarios: deben coincidir.</Alerta>}
            <div className="flex flex-wrap items-center justify-between gap-3">
              <b className="text-[14px]">Total del grupo: {entero(total)} usuarios</b>
              <Boton icono={<Check className="size-4" />} disabled={!listoBase || !total || totalDestinos !== total} cargando={registrar.isPending} onClick={registrarGrupo}>Registrar grupo</Boton>
            </div>
          </Tarjeta>
          <ErrorFormulario error={registrar.error} />
        </div>

        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Viaje en abordaje" icono={<Route />} />
            {!operacionId ? <p className="m-0 text-[13px] text-texto-2">Seleccione un viaje en curso.</p> : op.isLoading ? <Cargando alto={60} /> : op.error ? <ErrorCarga error={op.error} /> : op.data && (
              <>
                <div className="flex items-center gap-2 font-bold"><Ship className="size-4 text-azul" />{EMBARCACION[op.data.embarcacion.nombre]} · {codigoCorto('V', op.data.id)}</div>
                <div className="text-[13px] text-texto-2">{op.data.embarcaderoOrigen} → {op.data.embarcaderoDestino}</div>
                <div className="flex items-end justify-between">
                  <span className="text-[12px] text-texto-2">Registrados en el viaje</span>
                  <span className="font-titulo text-[28px] text-noche">{entero(op.data.totalUsuariosTransportados)}</span>
                </div>
              </>
            )}
          </Tarjeta>
          <HoyEnEmbarcadero embarcadero={abordaje || undefined} />
        </div>
      </div>
    </Pagina>
  )
}

function Contador({ etiqueta, valor, onCambio }: { etiqueta: string; valor: number; onCambio: (v: number) => void }) {
  return (
    <div className="flex items-center gap-3">
      <span className="flex-1 text-[13.5px]">{etiqueta}</span>
      <button type="button" aria-label={`Restar en ${etiqueta}`} disabled={valor === 0} onClick={() => onCambio(Math.max(0, valor - 1))}
        className="size-8 rounded-[10px] border border-borde bg-white text-noche flex items-center justify-center cursor-pointer disabled:opacity-40"><Minus className="size-4" /></button>
      <input aria-label={etiqueta} type="number" min={0} value={valor} onChange={(e) => onCambio(Math.max(0, Math.floor(Number(e.target.value) || 0)))}
        className="w-12 text-center font-titulo text-[18px] text-noche bg-transparent border-0 outline-none [appearance:textfield]" />
      <button type="button" aria-label={`Sumar en ${etiqueta}`} onClick={() => onCambio(valor + 1)}
        className="size-8 rounded-[10px] bg-azul text-white flex items-center justify-center cursor-pointer"><Plus className="size-4" /></button>
    </div>
  )
}

function HoyEnEmbarcadero({ embarcadero }: { embarcadero?: Embarcaderos }) {
  const q = useQuery({
    queryKey: ['pasajeros', { fecha: hoyISO(), puertoAbordaje: embarcadero }, 'hoy'],
    queryFn: () => usuariosService.listar({ fecha: hoyISO(), puertoAbordaje: embarcadero }, { size: 100 }),
    enabled: !!embarcadero,
  })
  const registros = q.data?.content ?? []
  const suma = (f: (u: (typeof registros)[number]) => number) => registros.reduce((s, u) => s + f(u), 0)
  return (
    <>
      <Tarjeta>
        <EncabezadoTarjeta titulo="Hoy en este embarcadero" icono={<UsersRound />} />
        {!embarcadero ? <p className="m-0 text-[13px] text-texto-2">Seleccione el embarcadero.</p> : q.isLoading ? <Cargando alto={60} /> : q.error ? <ErrorCarga error={q.error} /> : (
          <div className="grid grid-cols-2 gap-4">
            <Dato etiqueta="Usuarios registrados">{entero(suma((u) => u.usuariosTransportados))}</Dato>
            <Dato etiqueta="Registros de abordaje">{entero(registros.length)}</Dato>
            <Dato etiqueta="Mujeres">{entero(suma((u) => u.porSexo?.FEMENINO ?? 0))}</Dato>
            <Dato etiqueta="Hombres">{entero(suma((u) => u.porSexo?.MASCULINO ?? 0))}</Dato>
          </div>
        )}
      </Tarjeta>
      <Tarjeta sinRelleno>
        <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Últimos registros" icono={<History />} /></div>
        {!embarcadero ? null : !registros.length ? <Vacio titulo="Sin registros hoy" /> : (
          <div className="overflow-x-auto">
            <table className="tabla">
              <thead><tr><th>Hora</th><th className="text-right">Usuarios</th><th>Destinos</th></tr></thead>
              <tbody>
                {registros.slice(0, 8).map((u) => (
                  <tr key={u.id}>
                    <td>{hora(u.fechaHora)}</td>
                    <td className="text-right font-bold">{entero(u.usuariosTransportados)}</td>
                    <td>{Object.entries(u.porDestino ?? {}).filter(([, v]) => v).map(([k]) => EMBARCADERO[k as Embarcaderos]).join(', ')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Tarjeta>
    </>
  )
}
