import { useMutation, useQueries, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Check, Fuel, History, Info, Ship } from 'lucide-react'
import { useNavigate } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { Cargando, ErrorCarga, ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { CargaArchivos } from '../../components/forms/CargaArchivos'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Interruptor, Selector } from '../../components/ui/Campo'
import { Segmentado } from '../../components/ui/Selecciones'
import { EncabezadoTarjeta, PasoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAbiertas, useEmbarcaciones } from '../../hooks/consultas'
import { combustibleService } from '../../services/modules/combustible.service'
import type { Soporte } from '../../types/api'
import { EMBARCACION, TIPO_COMBUSTIBLE, opciones, type TipoCombustible } from '../../types/enums'
import { ahoraLocal, decimal, fecha, instanteSiCambio, localAInstant, pesos } from '../../utils/formato'

/** Plantilla 17 · Registrar abastecimiento (POST /abastecimiento). El soporte es obligatorio. */
export function RegistroCombustiblePage() {
  const navegar = useNavigate()
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const emb = useEmbarcaciones()
  const abiertas = useAbiertas()
  const [embarcacionId, setEmbarcacionId] = useState('')
  const [inicial] = useState(ahoraLocal)
  const [fechaHora, setFechaHora] = useState(inicial)
  const [tipo, setTipo] = useState<TipoCombustible | ''>('')
  const [galones, setGalones] = useState('')
  const [valor, setValor] = useState('')
  const [tanqueLleno, setTanqueLleno] = useState(false)
  const [observaciones, setObservaciones] = useState('')
  const [soportes, setSoportes] = useState<Soporte[]>([])
  const [intentado, setIntentado] = useState(false)

  const id = embarcacionId || emb.data?.[0]?.id || ''
  const actual = emb.data?.find((e) => e.id === id)
  const enCurso = abiertas.data?.find((o) => o.embarcacion === actual?.nombre)
  const ultimos = useQueries({
    queries: (emb.data ?? []).map((e) => ({ queryKey: ['abastecimiento', e.id, 'ultimo'], queryFn: () => combustibleService.listar(e.id, { size: 1 }) })),
  })

  const errores: Record<string, string> = {}
  if (!id) errores.embarcacion = 'Seleccione la embarcación'
  if (enCurso) errores.embarcacion = 'La embarcación tiene un viaje en curso: el abastecimiento se hace sin pasajeros a bordo'
  if (fechaHora && new Date(localAInstant(fechaHora)) > new Date()) errores.fecha = 'No puede ser futura'
  if (!tipo) errores.tipo = 'Seleccione el tipo de combustible'
  if (!galones || Number(galones) <= 0) errores.galones = 'Debe ser mayor que 0'
  else if (!/^\d{1,6}(\.\d{1,2})?$/.test(galones)) errores.galones = 'Hasta 6 enteros y 2 decimales'
  if (valor === '' || Number(valor) < 0) errores.valor = 'Escriba el valor total (0 o más)'
  if (!soportes.length) errores.soporte = 'Adjunte la factura o el comprobante'
  const e = (k: string) => (intentado ? errores[k] : undefined)

  const m = useMutation({
    mutationFn: () => combustibleService.registrar({
      fechaHora: instanteSiCambio(fechaHora, inicial), embarcacionId: id, tipoCombustible: tipo as TipoCombustible,
      galones: Number(galones), valorTotal: Number(valor), tanqueLleno, soporteId: soportes[0]!.id, observaciones: observaciones.trim() || null,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['abastecimiento'] })
      queryClient.invalidateQueries({ queryKey: ['consumo'] })
      avisar('Abastecimiento registrado.')
      navegar('/combustible')
    },
  })

  if (emb.isLoading) return <Pagina titulo="Registrar abastecimiento"><Cargando alto={320} /></Pagina>
  if (emb.error) return <Pagina titulo="Registrar abastecimiento"><Tarjeta><ErrorCarga error={emb.error} /></Tarjeta></Pagina>

  return (
    <Pagina
      titulo="Registrar abastecimiento"
      migas={[{ etiqueta: 'Combustible', a: '/combustible' }, { etiqueta: 'Nuevo abastecimiento' }]}
      descripcion="Registro detallado del combustible utilizado en la operación (numeral 4.5)."
      acciones={<Boton variante="secundario" a="/combustible" icono={<History className="size-4" />}>Ver historial</Boton>}
    >
      <div className="grid xl:grid-cols-[minmax(0,1.65fr)_minmax(0,1fr)] gap-5 items-start">
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <PasoTarjeta numero={1} titulo="Datos del suministro" subtitulo="Fecha, embarcación y cantidad" />
            <div className="grid gap-4 sm:grid-cols-3">
              <Campo etiqueta="Fecha y hora del suministro" ayuda="Vacía = ahora" error={e('fecha')}>
                {(p) => <Entrada {...p} type="datetime-local" max={ahoraLocal()} value={fechaHora} onChange={(ev) => setFechaHora(ev.target.value)} />}
              </Campo>
              <div className="flex flex-col gap-1.5">
                <span className="etiqueta">Embarcación abastecida <span className="text-error">*</span></span>
                <Segmentado etiqueta="Embarcación" valor={id} onCambio={setEmbarcacionId} opciones={(emb.data ?? []).map((x) => ({ valor: x.id, etiqueta: EMBARCACION[x.nombre] }))} />
              </div>
              <Campo etiqueta="Tipo de combustible" obligatorio error={e('tipo')}>
                {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(TIPO_COMBUSTIBLE)} value={tipo} onChange={(ev) => setTipo(ev.target.value as TipoCombustible)} />}
              </Campo>
              <Campo etiqueta="Cantidad suministrada" obligatorio ayuda={galones && Number(galones) > 0 ? `Equivale a ≈ ${decimal(Number(galones) * 3.78541)} litros` : undefined} error={e('galones')}>
                {(p) => <Entrada {...p} type="number" min={0} step="0.01" inputMode="decimal" sufijo="gal" value={galones} onChange={(ev) => setGalones(ev.target.value)} />}
              </Campo>
              <Campo etiqueta="Valor total" obligatorio ayuda={galones && valor && Number(galones) > 0 ? `${pesos(Number(valor) / Number(galones))} por galón` : undefined} error={e('valor')}>
                {(p) => <Entrada {...p} type="number" min={0} step="1" inputMode="numeric" sufijo="COP" value={valor} onChange={(ev) => setValor(ev.target.value)} />}
              </Campo>
            </div>
            {e('embarcacion') && <Alerta tono="error">{e('embarcacion')}</Alerta>}
            <Interruptor activo={tanqueLleno} onCambio={setTanqueLleno} etiqueta="Tanque lleno" descripcion="El consumo se calcula entre dos abastecimientos con tanque lleno (R37)" />
          </Tarjeta>

          <Tarjeta>
            <PasoTarjeta numero={2} titulo="Soporte del abastecimiento" subtitulo="Factura, tirilla o comprobante del proveedor (obligatorio)" />
            <CargaArchivos carpeta="combustible" multiple={false} onCambio={setSoportes} titulo="Adjunta la foto o el PDF del soporte" />
            {e('soporte') && <p role="alert" className="m-0 text-[12px] text-error">{e('soporte')}</p>}
            <Alerta tono="info">El soporte se incluye en el informe mensual de combustible.</Alerta>
          </Tarjeta>

          <Tarjeta>
            <PasoTarjeta numero={3} titulo="Observaciones" />
            <Campo etiqueta="Observaciones o novedades del consumo" opcional>
              {(p) => <AreaTexto {...p} maxLength={2000} value={observaciones} onChange={(ev) => setObservaciones(ev.target.value)} placeholder="Ej.: mayor consumo por corriente fuerte en el tramo Calle 22 – Centro Verde…" />}
            </Campo>
          </Tarjeta>

          <Tarjeta className="!flex-row flex-wrap items-center justify-end gap-3">
            {m.error && <div className="basis-full"><ErrorFormulario error={m.error} /></div>}
            <Boton variante="fantasma" onClick={() => navegar('/combustible')}>Cancelar</Boton>
            <Boton variante="institucional" icono={<Check className="size-4" />} cargando={m.isPending} onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>
              Registrar abastecimiento
            </Boton>
          </Tarjeta>
        </div>
        <div className="flex flex-col gap-5 min-w-0">
          <Tarjeta>
            <EncabezadoTarjeta titulo="Último abastecimiento por embarcación" icono={<Fuel />} />
            {(emb.data ?? []).map((x, i) => {
              const a = ultimos[i]?.data?.content[0]
              return (
                <div key={x.id} className="rounded-[12px] bg-fondo px-3.5 py-3">
                  <div className="flex items-center justify-between gap-2">
                    <span className="inline-flex items-center gap-1.5 font-bold text-[13.5px]"><Ship className="size-4 text-azul" />{EMBARCACION[x.nombre]}</span>
                    <span className="text-[12px] text-texto-2">{a ? fecha(a.fechaHora) : ''}</span>
                  </div>
                  <div className="font-titulo text-[22px] text-noche">{ultimos[i]?.isLoading ? '…' : a ? `${decimal(a.galones, 2)} gal` : 'Sin registros'}</div>
                </div>
              )
            })}
          </Tarjeta>
          <Tarjeta>
            <EncabezadoTarjeta titulo="Recuerda" icono={<Info />} tono="dorado" />
            <p className="m-0 text-[13px] text-texto-2">El abastecimiento se realiza sin pasajeros a bordo y con el motor apagado (numeral 7.1.10). Queda asociado a la bitácora del siguiente viaje.</p>
          </Tarjeta>
        </div>
      </div>
    </Pagina>
  )
}
