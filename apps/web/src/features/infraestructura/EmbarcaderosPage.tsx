import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Anchor, ArrowRight, ClipboardList, Construction, LifeBuoy, Megaphone, Plus } from 'lucide-react'
import { Pagina } from '../../app/layouts/Pagina'
import { MapaCorredor } from '../../components/charts/MapaCorredor'
import { Cargando, ErrorCarga } from '../../components/feedback/Estados'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { CajaIcono } from '../../components/ui/CajaIcono'
import { Insignia } from '../../components/ui/Insignia'
import { EstadoActivoInsignia } from '../../components/ui/InsigniasEstado'
import { Modal } from '../../components/ui/Modal'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useTablero } from '../../hooks/consultas'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import { EMBARCADERO, EMBARCADEROS, type Embarcaderos, type EstadoActivo } from '../../types/enums'
import { fecha } from '../../utils/formato'
import { FormularioRegistroInfra } from './FormularioRegistroInfra'
import { TablaRegistrosInfra } from './RegistrosInfra'

const ROL_EMBARCADERO: Record<Embarcaderos, string> = {
  RANCHO_GRANDE: 'Embarcadero · extremo del recorrido',
  CENTRO_CALLE_22: 'Embarcadero · estación intermedia',
  CENTRO_VERDE: 'Embarcadero · extremo del recorrido',
}

/** Plantilla 09 · Gestión de embarcaderos (tablero y registros de infraestructura, numeral 4.4). */
export function EmbarcaderosPage() {
  const tablero = useTablero()
  const pendientes = useQuery({ queryKey: ['infraestructura', 'pendientes'], queryFn: infraestructuraService.pendientesDeComunicar })
  const [registrar, setRegistrar] = useState<Embarcaderos | 'nuevo' | null>(null)

  const embarcaderos = new Map((tablero.data ?? []).filter((t) => t.tipo === 'EMBARCADERO').map((t) => [t.identificador as Embarcaderos, t]))
  const estados = Object.fromEntries([...embarcaderos].map(([k, v]) => [k, v.estadoActual])) as Partial<Record<Embarcaderos, EstadoActivo>>

  return (
    <Pagina
      titulo="Embarcaderos e infraestructura"
      migas={[{ etiqueta: 'Embarcaderos e infraestructura' }]}
      descripcion="Tres embarcaderos habilitados para la operación piloto y una estación en construcción."
      acciones={
        <>
          <Boton variante="secundario" a="/infraestructura/ayudas" icono={<LifeBuoy className="size-4" />}>Ayudas a la navegación</Boton>
          <Boton icono={<Plus className="size-4" />} onClick={() => setRegistrar('nuevo')}>Registrar revisión</Boton>
        </>
      }
    >
      {!!pendientes.data?.length && (
        <Alerta tono="error" titulo={`${pendientes.data.length} registro(s) sin comunicar al supervisor del contrato`}>
          Las novedades que afectan la operación deben comunicarse oportunamente (numeral 4.4). Ábralas en la tabla de registros para comunicarlas.
        </Alerta>
      )}
      <Tarjeta>
        <EncabezadoTarjeta titulo="Mapa del corredor" subtitulo="Esquema no a escala" />
        {tablero.error ? <ErrorCarga error={tablero.error} onReintentar={() => tablero.refetch()} /> : <MapaCorredor estados={estados} />}
      </Tarjeta>

      {tablero.isLoading ? <Cargando /> : (
        <div className="grid gap-4 md:grid-cols-3">
          {EMBARCADEROS.map((e) => {
            const t = embarcaderos.get(e)
            return (
              <Tarjeta key={e}>
                <div className="flex items-start gap-3">
                  <CajaIcono tono={!t || t.estadoActual === 'OPERATIVO' ? 'exito' : t.estadoActual === 'FUERA_DE_SERVICIO' ? 'error' : 'dorado'}><Anchor /></CajaIcono>
                  <div className="flex-1 min-w-0">
                    <h2 className="m-0 font-titulo text-[18px] font-normal text-noche">{EMBARCADERO[e]}</h2>
                    <p className="m-0 text-[12px] text-texto-2">{ROL_EMBARCADERO[e]}</p>
                  </div>
                  {t && <EstadoActivoInsignia estado={t.estadoActual} />}
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <Dato etiqueta="Última revisión">{t?.ultimaRevision ? fecha(t.ultimaRevision) : 'Sin registros'}</Dato>
                  <Dato etiqueta="Novedades abiertas"><span className={t?.registrosAbiertos ? 'text-error' : ''}>{t?.registrosAbiertos ?? 0}</span></Dato>
                </div>
                <div className="flex flex-wrap gap-2 mt-auto">
                  <Boton variante="secundario" pequeno a={`/infraestructura/embarcaderos/${e}`}>Ver detalle <ArrowRight className="size-3.5" /></Boton>
                  <Boton variante="suave" pequeno icono={<Plus className="size-3.5" />} onClick={() => setRegistrar(e)}>Registrar revisión</Boton>
                </div>
              </Tarjeta>
            )
          })}
        </div>
      )}

      <section className="rounded-tarjeta border-2 border-dashed border-[#C9D3E6] bg-white/60 p-5 flex flex-wrap items-start gap-4">
        <CajaIcono tono="dorado"><Construction /></CajaIcono>
        <div className="flex-1 min-w-[240px]">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="m-0 font-titulo text-[16px] font-normal text-noche">Estación de atraque · Universidad de Córdoba</h2>
            <Insignia tono="warn">En construcción</Insignia>
          </div>
          <p className="m-0 mt-1 text-[13px] text-texto-2">Infraestructura en construcción. No está habilitada para la operación piloto y no se registra en el sistema.</p>
        </div>
        <Insignia tono="neutral">No habilitada para operación</Insignia>
      </section>

      <Tarjeta sinRelleno>
        <div className="p-5 pb-3"><EncabezadoTarjeta titulo="Revisiones y novedades recientes" icono={<ClipboardList />}
          acciones={pendientes.data?.length ? <Insignia tono="err"><Megaphone className="size-3.5" /> {pendientes.data.length} sin comunicar</Insignia> : undefined} /></div>
        <TablaRegistrosInfra />
      </Tarjeta>

      <Modal abierto={registrar !== null} onCerrar={() => setRegistrar(null)} titulo="Registrar revisión o novedad" subtitulo="Embarcaderos y ayudas a la navegación (numeral 4.4)" ancho="lg">
        {registrar !== null && (
          <FormularioRegistroInfra key={registrar} embarcaderoFijo={registrar === 'nuevo' ? undefined : registrar} onGuardado={() => setRegistrar(null)} />
        )}
      </Modal>
    </Pagina>
  )
}
