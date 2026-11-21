import { keepPreviousData, useQueries, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { CalendarDays, Info, Plus, Ship, X } from 'lucide-react'
import { useNavigate, useSearchParams } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { Paginacion } from '../../components/tables/Paginacion'
import { Tabla, type Columna } from '../../components/tables/Tabla'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada, Selector } from '../../components/ui/Campo'
import { EstadoOperacionInsignia } from '../../components/ui/InsigniasEstado'
import { Pestanas } from '../../components/ui/Selecciones'
import { Tarjeta } from '../../components/ui/Tarjeta'
import { usePermiso } from '../../hooks/usePermiso'
import { viajesService } from '../../services/modules/viajes.service'
import type { FiltroOperaciones, OperacionResumen } from '../../types/api'
import {
  EMBARCACION, EMBARCADERO, nombreEmbarcadero, opciones, type Embarcaciones, type Embarcaderos, type EstadoOperacion,
} from '../../types/enums'
import { codigoCorto, entero, fecha, hora } from '../../utils/formato'
import { BotonExportar } from '../informes/BotonExportar'

const TAMANO = 10
type Pestana = 'TODOS' | EstadoOperacion
const PESTANAS: { valor: Pestana; nombre: string }[] = [
  { valor: 'TODOS', nombre: 'Todos' },
  { valor: 'EN_CURSO', nombre: 'En curso' },
  { valor: 'FINALIZADA', nombre: 'Finalizados' },
  { valor: 'SUSPENDIDA', nombre: 'Suspendidos' },
]

/** Plantilla 03 · Listado de viajes (GET /operaciones con filtros y paginación del backend). */
export function ViajesPage() {
  const navegar = useNavigate()
  const puedeRegistrar = usePermiso('viajes.registrar')
  const [params, setParams] = useSearchParams()
  const [pagina, setPagina] = useState(0)

  const pestana = (params.get('estado') as Pestana) || 'TODOS'
  const filtro: FiltroOperaciones = {
    embarcacion: (params.get('embarcacion') as Embarcaciones) || undefined,
    origen: (params.get('origen') as Embarcaderos) || undefined,
    destino: (params.get('destino') as Embarcaderos) || undefined,
    fecha: params.get('fecha') || undefined,
  }
  const cambiar = (clave: string, valor?: string) => {
    const p = new URLSearchParams(params)
    if (valor) p.set(clave, valor)
    else p.delete(clave)
    setParams(p, { replace: true })
    setPagina(0)
  }

  const consulta = useQuery({
    queryKey: ['operaciones', filtro, pestana, pagina],
    queryFn: () => viajesService.listar({ ...filtro, estado: pestana === 'TODOS' ? undefined : pestana }, { page: pagina, size: TAMANO }),
    placeholderData: keepPreviousData,
  })
  // Totales por estado con los mismos filtros (size=1: solo interesa totalElements)
  const conteos = useQueries({
    queries: PESTANAS.map((p) => ({
      queryKey: ['operaciones', 'conteo', filtro, p.valor],
      queryFn: () => viajesService.listar({ ...filtro, estado: p.valor === 'TODOS' ? undefined : p.valor }, { size: 1 }),
    })),
  })

  const columnas: Columna<OperacionResumen>[] = [
    { clave: 'id', titulo: 'Viaje', celda: (o) => <span className="font-bold text-azul">{codigoCorto('V', o.id)}</span> },
    { clave: 'fecha', titulo: 'Fecha', celda: (o) => fecha(o.fechaHoraInicio) },
    { clave: 'emb', titulo: 'Embarcación', celda: (o) => <span className="inline-flex items-center gap-1.5 font-bold"><Ship className="size-4 text-azul" aria-hidden />{EMBARCACION[o.embarcacion]}</span> },
    { clave: 'origen', titulo: 'Origen', celda: (o) => nombreEmbarcadero(o.embarcaderoOrigen) },
    { clave: 'destino', titulo: 'Destino', celda: (o) => nombreEmbarcadero(o.embarcaderoDestino) },
    { clave: 'salida', titulo: 'Salida', celda: (o) => hora(o.fechaHoraInicio) },
    { clave: 'llegada', titulo: 'Llegada', celda: (o) => (o.fechaHoraFin ? hora(o.fechaHoraFin) : '—') },
    { clave: 'pax', titulo: 'Pasaj.', alinear: 'derecha', celda: (o) => entero(o.totalUsuariosTransportados) },
    { clave: 'estado', titulo: 'Estado', celda: (o) => <EstadoOperacionInsignia estado={o.estado} /> },
  ]

  const hayFiltros = Object.values(filtro).some(Boolean)

  return (
    <Pagina
      titulo="Viajes"
      migas={[{ etiqueta: 'Operación' }, { etiqueta: 'Viajes' }]}
      descripcion="Bitácoras de viaje registradas durante la operación piloto (numeral 4.1 del anexo técnico)."
      acciones={
        <>
          <BotonExportar conjunto="VIAJES" />
          {puedeRegistrar && <Boton variante="secundario" a="/novedades/nueva">Registrar novedad</Boton>}
          {puedeRegistrar && <Boton a="/viajes/nuevo" icono={<Plus className="size-4" />}>Nuevo viaje</Boton>}
        </>
      }
    >
      <Pestanas
        valor={pestana}
        onCambio={(v) => cambiar('estado', v === 'TODOS' ? undefined : v)}
        opciones={PESTANAS.map((p, i) => ({
          valor: p.valor,
          etiqueta: `${p.nombre}${conteos[i]?.data ? ` (${entero(conteos[i].data.page.totalElements)})` : ''}`,
        }))}
      />
      <Tarjeta>
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-[1.2fr_1fr_1fr_1fr_auto] items-end">
          <Campo etiqueta="Fecha de operación">
            {(p) => <Entrada {...p} type="date" value={filtro.fecha ?? ''} onChange={(e) => cambiar('fecha', e.target.value)} icono={<CalendarDays />} />}
          </Campo>
          <Campo etiqueta="Embarcación">
            {(p) => <Selector {...p} vacio="Todas" opciones={opciones(EMBARCACION)} value={filtro.embarcacion ?? ''} onChange={(e) => cambiar('embarcacion', e.target.value)} />}
          </Campo>
          <Campo etiqueta="Embarcadero de origen">
            {(p) => <Selector {...p} vacio="Todos" opciones={opciones(EMBARCADERO)} value={filtro.origen ?? ''} onChange={(e) => cambiar('origen', e.target.value)} />}
          </Campo>
          <Campo etiqueta="Embarcadero de destino">
            {(p) => <Selector {...p} vacio="Todos" opciones={opciones(EMBARCADERO)} value={filtro.destino ?? ''} onChange={(e) => cambiar('destino', e.target.value)} />}
          </Campo>
          <Boton variante="fantasma" icono={<X className="size-4" />} disabled={!hayFiltros}
            onClick={() => { setParams(pestana === 'TODOS' ? {} : { estado: pestana }, { replace: true }); setPagina(0) }}>
            Limpiar
          </Boton>
        </div>
      </Tarjeta>
      <Tarjeta sinRelleno>
        <Tabla
          columnas={columnas}
          filas={consulta.data?.content}
          clave={(o) => o.id}
          cargando={consulta.isLoading}
          error={consulta.error}
          onReintentar={() => consulta.refetch()}
          onFila={(o) => navegar(`/viajes/${o.id}`)}
          filaActiva={(o) => o.estado === 'EN_CURSO'}
          minimo={980}
          pie={consulta.data && (
            <Paginacion pagina={pagina} totalPaginas={consulta.data.page.totalPages} totalElementos={consulta.data.page.totalElements}
              tamano={TAMANO} onCambio={setPagina} nombre="viajes" />
          )}
        />
      </Tarjeta>
      <p className="m-0 flex items-start gap-2 text-[12.5px] text-texto-2">
        <Info className="size-4 flex-none mt-px" aria-hidden />
        Cada fila corresponde a una bitácora de viaje. Haga clic en una fila para ver el detalle, las paradas, la tripulación y las novedades.
      </p>
    </Pagina>
  )
}
