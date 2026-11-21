import { keepPreviousData, useMutation, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Droplet, Eye, FileText, Fuel, Gauge, Plus, TrendingUp } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { GraficoBarras } from '../../components/charts/GraficoBarras'
import { GraficoLineas } from '../../components/charts/GraficoLineas'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Paginacion } from '../../components/tables/Paginacion'
import { Tabla } from '../../components/tables/Tabla'
import { Boton } from '../../components/ui/Boton'
import { Insignia } from '../../components/ui/Insignia'
import { Kpi } from '../../components/ui/Kpi'
import { Segmentado } from '../../components/ui/Selecciones'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useEmbarcaciones } from '../../hooks/consultas'
import { combustibleService } from '../../services/modules/combustible.service'
import { EMBARCACION, TIPO_COMBUSTIBLE } from '../../types/enums'
import { abrirEnPestana } from '../../utils/descargas'
import { bytes, decimal, diaMes, entero, fechaHora, inicioMes, mesActual, mesLargo, pesos, rangoDia } from '../../utils/formato'
import { BotonExportar } from '../informes/BotonExportar'
import { useAbastecimientosDesde } from './consultas'

const TAMANO = 10

/** Plantilla 18 · Historial de combustible (GET /abastecimiento por embarcación y consumo entre tanques llenos). */
export function CombustiblePage() {
  const emb = useEmbarcaciones()
  const { avisar } = useAvisos()
  const [embarcacionId, setEmbarcacionId] = useState('')
  const [pagina, setPagina] = useState(0)
  const id = embarcacionId || emb.data?.[0]?.id || ''
  const actual = emb.data?.find((e) => e.id === id)

  const lista = useQuery({
    queryKey: ['abastecimiento', id, pagina],
    queryFn: () => combustibleService.listar(id, { page: pagina, size: TAMANO }),
    enabled: !!id,
    placeholderData: keepPreviousData,
  })
  const consumo = useQuery({ queryKey: ['consumo', id], queryFn: () => combustibleService.consumo(id), enabled: !!id })
  const mes = useAbastecimientosDesde(actual ? [actual] : undefined, rangoDia(inicioMes()).desde)
  const soporte = useMutation({ mutationFn: combustibleService.urlSoporte, onSuccess: (r) => abrirEnPestana(r.url), onError: (e) => avisar(e.message, 'error') })

  const periodos = consumo.data ?? []
  const ultimo = periodos.at(-1)
  const recientes = [...(mes.todos ?? [])].reverse()

  return (
    <Pagina
      titulo="Historial de combustible"
      migas={[{ etiqueta: 'Combustible' }, { etiqueta: 'Historial' }]}
      descripcion="Abastecimientos registrados y consumo calculado entre tanques llenos (numeral 4.5)."
      acciones={<><BotonExportar conjunto="CONSUMO" etiqueta="Exportar consumo" /><BotonExportar conjunto="COMBUSTIBLE" /><Boton a="/combustible/nuevo" icono={<Plus className="size-4" />}>Registrar abastecimiento</Boton></>}
    >
      {emb.isLoading ? <Cargando /> : emb.error ? <Tarjeta><ErrorCarga error={emb.error} /></Tarjeta> : (
        <>
          <Segmentado etiqueta="Embarcación" valor={id} onCambio={(v) => { setEmbarcacionId(v); setPagina(0) }}
            opciones={(emb.data ?? []).map((e) => ({ valor: e.id, etiqueta: EMBARCACION[e.nombre] }))} className="self-start" />
          <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
            <Kpi icono={<Fuel />} cifra={mes.listo ? `${decimal(mes.galones)} gal` : '—'} etiqueta={`Suministrado en ${mesLargo(mesActual())}`} cargando={mes.cargando} />
            <Kpi icono={<Droplet />} cifra={mes.listo ? entero(mes.todos.length) : '—'} etiqueta="Abastecimientos del mes" cargando={mes.cargando} />
            <Kpi icono={<TrendingUp />} tono="dorado" cifra={ultimo?.galonesPorViaje !== undefined ? `${decimal(ultimo.galonesPorViaje, 2)} gal/viaje` : '—'} etiqueta="Consumo del último periodo" nota={ultimo ? `${entero(ultimo.viajes)} viajes · ${decimal(ultimo.galonesConsumidos)} gal` : 'Se calcula entre dos tanques llenos'} cargando={consumo.isLoading} />
            <Kpi icono={<Gauge />} tono="exito" cifra={ultimo?.galonesPorHoraMotor !== undefined ? `${decimal(ultimo.galonesPorHoraMotor, 2)} gal/h` : '—'} etiqueta="Consumo por hora de motor"
              nota={ultimo && !ultimo.horometroCompleto ? 'Faltan lecturas de horómetro' : undefined} cargando={consumo.isLoading} />
          </div>
          <div className="grid xl:grid-cols-[minmax(0,1.4fr)_minmax(0,1fr)] gap-5 items-start">
            <Tarjeta>
              <EncabezadoTarjeta titulo="Galones por abastecimiento" subtitulo={`${actual ? EMBARCACION[actual.nombre] : ''} · ${mesLargo(mesActual())}`} />
              {mes.cargando ? <Cargando /> : !recientes.length ? <Vacio titulo="Sin abastecimientos este mes" /> : (
                <GraficoBarras titulo="Galones por abastecimiento" sufijo=" gal" etiquetas={recientes.map((a) => diaMes(a.fechaHora))}
                  series={[{ nombre: 'Galones', valores: recientes.map((a) => Number(a.galones)), color: '#013996' }]} />
              )}
            </Tarjeta>
            <Tarjeta>
              <EncabezadoTarjeta titulo="Rendimiento por periodo" subtitulo="Galones por viaje entre tanques llenos (R37)" />
              {consumo.isLoading ? <Cargando /> : consumo.error ? <ErrorCarga error={consumo.error} /> : periodos.length < 1 ? (
                <Vacio titulo="Aún no hay periodos completos">Se necesitan dos abastecimientos con tanque lleno.</Vacio>
              ) : (
                <GraficoLineas titulo="Galones por viaje" sufijo=" gal/viaje" etiquetas={periodos.map((p) => diaMes(p.hasta))}
                  series={[{ nombre: 'Galones por viaje', valores: periodos.map((p) => p.galonesPorViaje ?? null), color: '#0A6EDC' }]} />
              )}
            </Tarjeta>
          </div>
          <Tarjeta sinRelleno>
            <Tabla
              columnas={[
                { clave: 'f', titulo: 'Fecha y hora', celda: (a) => fechaHora(a.fechaHora) },
                { clave: 't', titulo: 'Combustible', celda: (a) => TIPO_COMBUSTIBLE[a.tipoCombustible] },
                { clave: 'g', titulo: 'Cantidad', alinear: 'derecha', celda: (a) => <b>{decimal(a.galones, 2)} gal</b> },
                { clave: 'v', titulo: 'Valor', alinear: 'derecha', celda: (a) => pesos(a.valorTotal) },
                { clave: 'p', titulo: 'Precio/gal', alinear: 'derecha', celda: (a) => pesos(a.precioPorGalon) },
                { clave: 'l', titulo: 'Tanque', celda: (a) => (a.tanqueLleno ? <Insignia tono="info">Lleno</Insignia> : <Insignia tono="neutral">Parcial</Insignia>) },
                {
                  clave: 's', titulo: 'Soporte', celda: (a) => a.soporte ? (
                    <span className="inline-flex items-center gap-2 border border-borde rounded-[10px] px-2.5 py-1.5">
                      <FileText className="size-4 text-azul" aria-hidden />
                      <span className="text-[12.5px] max-w-[140px] truncate">{a.soporte.nombreOriginal}<span className="block text-texto-2">{bytes(a.soporte.tamanoBytes)}</span></span>
                      <Boton variante="fantasma" pequeno soloIcono aria-label="Ver soporte" cargando={soporte.isPending && soporte.variables === a.id} onClick={() => soporte.mutate(a.id)}><Eye className="size-4" /></Boton>
                    </span>
                  ) : <span className="text-advertencia font-bold">Sin soporte</span>,
                },
                { clave: 'o', titulo: 'Viaje', celda: (a) => (a.operacionId ? <Insignia tono="ok">Asociado</Insignia> : <Insignia tono="gold">Antes del próximo zarpe</Insignia>) },
              ]}
              filas={lista.data?.content}
              clave={(a) => a.id}
              cargando={lista.isLoading}
              error={lista.error}
              onReintentar={() => lista.refetch()}
              minimo={1040}
              vacio={<Vacio titulo="Sin abastecimientos registrados" accion={<Boton a="/combustible/nuevo" variante="secundario">Registrar abastecimiento</Boton>} />}
              pie={lista.data && <Paginacion pagina={pagina} totalPaginas={lista.data.page.totalPages} totalElementos={lista.data.page.totalElements} tamano={TAMANO} onCambio={setPagina} nombre="abastecimientos" />}
            />
          </Tarjeta>
        </>
      )}
    </Pagina>
  )
}
