import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { CircleCheck, LifeBuoy, Plus, Trash2, TriangleAlert, OctagonAlert } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Tabla } from '../../components/tables/Tabla'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Interruptor, Selector } from '../../components/ui/Campo'
import { EstadoActivoInsignia } from '../../components/ui/InsigniasEstado'
import { Kpi } from '../../components/ui/Kpi'
import { Confirmacion, Modal } from '../../components/ui/Modal'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAyudas, useTablero } from '../../hooks/consultas'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import type { AyudaResumen, CrearAyudaRequest } from '../../types/api'
import {
  COLOR_LUZ, EMBARCADERO, ESTRUCTURA_AYUDA, FUNCION_MARCA, opciones,
  type ColorLuz, type Embarcaderos, type EstructuraAyuda, type FuncionMarca,
} from '../../types/enums'
import { fecha, porcentaje } from '../../utils/formato'
import { FormularioRegistroInfra } from './FormularioRegistroInfra'
import { TablaRegistrosInfra } from './RegistrosInfra'

/** Plantilla 11 · Ayudas a la navegación (boyas, balizas y señales del Manual DIMAR). */
export function AyudasPage() {
  const ayudas = useAyudas()
  const tablero = useTablero()
  const [crear, setCrear] = useState(false)
  const [desactivar, setDesactivar] = useState<AyudaResumen | null>(null)
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()

  const estado = new Map((tablero.data ?? []).filter((t) => t.tipo === 'AYUDA').map((t) => [t.identificador, t]))
  const total = ayudas.data?.length ?? 0
  const contar = (e: string) => (ayudas.data ?? []).filter((a) => (estado.get(a.codigo)?.estadoActual ?? 'OPERATIVO') === e).length

  const baja = useMutation({
    mutationFn: (id: string) => infraestructuraService.desactivarAyuda(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['infraestructura'] })
      avisar('Ayuda desactivada.')
      setDesactivar(null)
    },
  })

  return (
    <Pagina
      titulo="Ayudas a la navegación"
      migas={[{ etiqueta: 'Embarcaderos e infraestructura', a: '/infraestructura' }, { etiqueta: 'Ayudas a la navegación' }]}
      descripcion="Condiciones de boyas y señales luminosas a lo largo del recorrido."
      acciones={<Boton variante="secundario" icono={<Plus className="size-4" />} onClick={() => setCrear(true)}>Agregar ayuda</Boton>}
    >
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Kpi icono={<LifeBuoy />} cifra={total} etiqueta="Ayudas registradas" cargando={ayudas.isLoading} />
        <Kpi icono={<CircleCheck />} tono="exito" cifra={contar('OPERATIVO')} etiqueta="Operativas" nota={total ? `${porcentaje(contar('OPERATIVO'), total)} del total` : undefined} cargando={ayudas.isLoading || tablero.isLoading} />
        <Kpi icono={<TriangleAlert />} tono="advertencia" cifra={contar('OPERATIVO_CON_RESTRICCIONES')} etiqueta="Con restricciones" cargando={ayudas.isLoading || tablero.isLoading} />
        <Kpi icono={<OctagonAlert />} tono="error" cifra={contar('FUERA_DE_SERVICIO')} etiqueta="Fuera de servicio" cargando={ayudas.isLoading || tablero.isLoading} />
      </div>
      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta sinRelleno>
            <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Ayudas registradas" subtitulo="Estado según el último registro de cada ayuda" /></div>
            <Tabla
              columnas={[
                { clave: 'cod', titulo: 'Código', celda: (a) => <b className="text-azul">{a.codigo}</b> },
                { clave: 'tipo', titulo: 'Estructura', celda: (a) => `${ESTRUCTURA_AYUDA[a.estructura]}${a.luminosa ? ' luminosa' : ''}` },
                { clave: 'func', titulo: 'Función', celda: (a) => FUNCION_MARCA[a.funcion].etiqueta },
                { clave: 'rev', titulo: 'Última revisión', celda: (a) => (estado.get(a.codigo)?.ultimaRevision ? fecha(estado.get(a.codigo)!.ultimaRevision) : '—') },
                { clave: 'cond', titulo: 'Condición', celda: (a) => <EstadoActivoInsignia estado={estado.get(a.codigo)?.estadoActual ?? 'OPERATIVO'} /> },
                { clave: 'acc', titulo: '', alinear: 'derecha', celda: (a) => <Boton variante="fantasma" pequeno soloIcono aria-label={`Desactivar ${a.codigo}`} onClick={() => setDesactivar(a)}><Trash2 className="size-4" /></Boton> },
              ]}
              filas={ayudas.data}
              clave={(a) => a.id}
              cargando={ayudas.isLoading}
              error={ayudas.error}
              onReintentar={() => ayudas.refetch()}
              minimo={720}
            />
          </Tarjeta>
          <Tarjeta sinRelleno>
            <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Registros de las ayudas" /></div>
            <TablaRegistrosInfra />
          </Tarjeta>
        </div>
        <Tarjeta>
          <EncabezadoTarjeta titulo="Registrar revisión" subtitulo="Boyas y señales luminosas del recorrido" icono={<LifeBuoy />} />
          {ayudas.data?.length ? <FormularioRegistroInfraAyuda /> : <p className="m-0 text-[13px] text-texto-2">Registre primero una ayuda a la navegación.</p>}
        </Tarjeta>
      </div>
      {crear && <DialogoCrearAyuda onCerrar={() => setCrear(false)} />}
      <Confirmacion
        abierto={!!desactivar}
        onCerrar={() => setDesactivar(null)}
        onConfirmar={() => desactivar && baja.mutate(desactivar.id)}
        titulo="Desactivar ayuda a la navegación"
        mensaje={<>La ayuda <b>{desactivar?.codigo}</b> dejará de aparecer en el tablero. Sus registros se conservan.</>}
        textoConfirmar="Desactivar"
        peligro
        cargando={baja.isPending}
        error={baja.error?.message}
      />
    </Pagina>
  )
}

function FormularioRegistroInfraAyuda() {
  const [ayudaId, setAyudaId] = useState('')
  const ayudas = useAyudas()
  return (
    <div className="flex flex-col gap-4">
      <Campo etiqueta="Ayuda a la navegación" obligatorio>
        {(p) => <Selector {...p} vacio="Seleccione…" value={ayudaId} onChange={(e) => setAyudaId(e.target.value)} opciones={(ayudas.data ?? []).map((a) => ({ valor: a.id, etiqueta: `${a.codigo} · ${ESTRUCTURA_AYUDA[a.estructura]}` }))} />}
      </Campo>
      {ayudaId && <FormularioRegistroInfra key={ayudaId} ayudaFija={ayudaId} compacto />}
    </div>
  )
}

/** CrearAyudaNavegacionRequest. El color de la luz debe coincidir con el que exige la función (Manual DIMAR). */
function DialogoCrearAyuda({ onCerrar }: { onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [d, setD] = useState<Partial<CrearAyudaRequest>>({ luminosa: false })
  const [intentado, setIntentado] = useState(false)
  const poner = <K extends keyof CrearAyudaRequest>(k: K, v: CrearAyudaRequest[K]) => setD((x) => ({ ...x, [k]: v }))
  const exigido = d.funcion ? FUNCION_MARCA[d.funcion].color : null

  const errores: Record<string, string> = {}
  if (!d.codigo?.trim()) errores.codigo = 'Escriba el código (p. ej. B-07)'
  if (!d.estructura) errores.estructura = 'Seleccione la estructura'
  if (!d.funcion) errores.funcion = 'Seleccione la función'
  if (d.estructura === 'OTRA' && !d.descripcion?.trim()) errores.descripcion = 'Describa la estructura'
  if (d.luminosa && !d.colorLuz) errores.colorLuz = 'Seleccione el color de la luz'
  if (d.luminosa && exigido && d.colorLuz && d.colorLuz !== exigido) errores.colorLuz = `Esta función exige luz ${COLOR_LUZ[exigido].toLowerCase()}`
  if (!d.tramoDesde || !d.tramoHasta) errores.tramo = 'Indique el tramo'
  if (!d.referenciaUbicacion?.trim()) errores.referencia = 'Describa la ubicación'
  const e = (k: string) => (intentado ? errores[k] : undefined)

  const m = useMutation({
    mutationFn: () => infraestructuraService.crearAyuda({
      codigo: d.codigo!.trim(), estructura: d.estructura!, funcion: d.funcion!, luminosa: !!d.luminosa,
      colorLuz: d.luminosa ? d.colorLuz ?? null : null, ritmoLuz: d.luminosa ? d.ritmoLuz?.trim() || null : null,
      descripcion: d.descripcion?.trim() || null, tramoDesde: d.tramoDesde!, tramoHasta: d.tramoHasta!,
      referenciaUbicacion: d.referenciaUbicacion!.trim(),
      latitud: d.latitud ?? null, longitud: d.longitud ?? null,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['infraestructura'] })
      avisar('Ayuda a la navegación registrada.')
      onCerrar()
    },
  })

  return (
    <Modal abierto onCerrar={onCerrar} titulo="Agregar ayuda a la navegación" ancho="lg"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>Registrar ayuda</Boton></>}>
      <div className="grid gap-3 sm:grid-cols-3">
        <Campo etiqueta="Código" obligatorio error={e('codigo')}>{(p) => <Entrada {...p} maxLength={20} value={d.codigo ?? ''} onChange={(ev) => poner('codigo', ev.target.value)} />}</Campo>
        <Campo etiqueta="Estructura" obligatorio error={e('estructura')}>{(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(ESTRUCTURA_AYUDA)} value={d.estructura ?? ''} onChange={(ev) => poner('estructura', ev.target.value as EstructuraAyuda)} />}</Campo>
        <Campo etiqueta="Función" obligatorio error={e('funcion')}>
          {(p) => <Selector {...p} vacio="Seleccione…" opciones={(Object.keys(FUNCION_MARCA) as FuncionMarca[]).map((f) => ({ valor: f, etiqueta: FUNCION_MARCA[f].etiqueta }))} value={d.funcion ?? ''} onChange={(ev) => poner('funcion', ev.target.value as FuncionMarca)} />}
        </Campo>
      </div>
      <Interruptor activo={!!d.luminosa} onCambio={(v) => poner('luminosa', v)} etiqueta="Señal luminosa" descripcion={exigido ? `La función elegida exige luz ${COLOR_LUZ[exigido].toLowerCase()}` : undefined} />
      {d.luminosa && (
        <div className="grid gap-3 sm:grid-cols-2">
          <Campo etiqueta="Color de la luz" obligatorio error={e('colorLuz')}>{(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(COLOR_LUZ)} value={d.colorLuz ?? ''} onChange={(ev) => poner('colorLuz', ev.target.value as ColorLuz)} />}</Campo>
          <Campo etiqueta="Ritmo" opcional>{(p) => <Entrada {...p} maxLength={40} placeholder="Fl. R 3 s" value={d.ritmoLuz ?? ''} onChange={(ev) => poner('ritmoLuz', ev.target.value)} />}</Campo>
        </div>
      )}
      <div className="grid gap-3 sm:grid-cols-2">
        <Campo etiqueta="Tramo desde" obligatorio error={e('tramo')}>{(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(EMBARCADERO)} value={d.tramoDesde ?? ''} onChange={(ev) => poner('tramoDesde', ev.target.value as Embarcaderos)} />}</Campo>
        <Campo etiqueta="Tramo hasta" obligatorio>{(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(EMBARCADERO)} value={d.tramoHasta ?? ''} onChange={(ev) => poner('tramoHasta', ev.target.value as Embarcaderos)} />}</Campo>
      </div>
      <Campo etiqueta="Referencia de ubicación" obligatorio error={e('referencia')}>{(p) => <Entrada {...p} maxLength={200} placeholder="Margen derecha, frente a…" value={d.referenciaUbicacion ?? ''} onChange={(ev) => poner('referenciaUbicacion', ev.target.value)} />}</Campo>
      <div className="grid gap-3 sm:grid-cols-2">
        <Campo etiqueta="Latitud" opcional>{(p) => <Entrada {...p} type="number" step="0.000001" min={-90} max={90} value={d.latitud ?? ''} onChange={(ev) => poner('latitud', ev.target.value === '' ? null : Number(ev.target.value))} />}</Campo>
        <Campo etiqueta="Longitud" opcional>{(p) => <Entrada {...p} type="number" step="0.000001" min={-180} max={180} value={d.longitud ?? ''} onChange={(ev) => poner('longitud', ev.target.value === '' ? null : Number(ev.target.value))} />}</Campo>
      </div>
      <Campo etiqueta="Descripción" obligatorio={d.estructura === 'OTRA'} opcional={d.estructura !== 'OTRA'} error={e('descripcion')}>
        {(p) => <AreaTexto {...p} maxLength={300} value={d.descripcion ?? ''} onChange={(ev) => poner('descripcion', ev.target.value)} />}
      </Campo>
      <ErrorFormulario error={m.error} />
    </Modal>
  )
}
