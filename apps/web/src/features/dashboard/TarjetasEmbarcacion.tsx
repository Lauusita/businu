import { Ship } from 'lucide-react'
import { Link } from 'react-router'
import { Cargando, ErrorCarga } from '../../components/feedback/Estados'
import { EstadoEmbarcacionInsignia } from '../../components/ui/InsigniasEstado'
import { CajaIcono } from '../../components/ui/CajaIcono'
import { Dato, EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { useAbiertas, useEmbarcaciones, useHojaDeVida } from '../../hooks/consultas'
import type { EmbarcacionResponse } from '../../types/api'
import { EMBARCACION, nombreEmbarcadero, TIPO_MANTENIMIENTO } from '../../types/enums'
import { fecha, hora } from '../../utils/formato'

/** «Estado de las embarcaciones» (plantilla 02): estado, viaje en curso e intervenciones. */
export function EstadoEmbarcaciones() {
  const emb = useEmbarcaciones()
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Estado de las embarcaciones" subtitulo="Unidades de la operación piloto" />
      {emb.isLoading ? <Cargando /> : emb.error ? <ErrorCarga error={emb.error} onReintentar={() => emb.refetch()} /> : (
        <div className="flex flex-col gap-3">
          {emb.data?.map((e) => <FichaEmbarcacion key={e.id} e={e} />)}
        </div>
      )}
    </Tarjeta>
  )
}

function FichaEmbarcacion({ e }: { e: EmbarcacionResponse }) {
  const hv = useHojaDeVida(e.id)
  const abiertas = useAbiertas()
  const enCurso = abiertas.data?.find((o) => o.embarcacion === e.nombre)
  const ultima = hv.data?.intervenciones[0]
  const proxima = hv.data?.intervenciones.filter((i) => i.proximaIntervencion).sort((a, b) => a.proximaIntervencion!.localeCompare(b.proximaIntervencion!))[0]
  return (
    <Link to={`/embarcaciones/${e.id}`} className="block no-underline text-negro rounded-[14px] bg-fondo p-4 hover:bg-azul-suave transition-colors">
      <div className="flex items-start gap-3">
        <CajaIcono tono={e.estado === 'OPERATIVA' ? 'exito' : e.estado === 'EN_MANTENIMIENTO' ? 'dorado' : 'error'}><Ship /></CajaIcono>
        <div className="flex-1 min-w-0">
          <div className="font-titulo text-[18px] text-noche">{EMBARCACION[e.nombre]}</div>
          <div className="text-[12px] text-texto-2">Patente {e.patente} · {e.capacidadPasajeros} pasajeros</div>
        </div>
        <EstadoEmbarcacionInsignia estado={e.estado} />
      </div>
      <div className="grid grid-cols-2 gap-x-4 gap-y-3 mt-3">
        <Dato etiqueta="Viaje en curso">
          {enCurso ? `${nombreEmbarcadero(enCurso.embarcaderoOrigen)} · ${hora(enCurso.fechaHoraInicio)}` : 'Ninguno'}
        </Dato>
        <Dato etiqueta="Intervenciones">{hv.data ? hv.data.totalIntervenciones : '…'}</Dato>
        <Dato etiqueta="Último mantenimiento">{ultima ? `${fecha(ultima.fechaIntervencion)} · ${TIPO_MANTENIMIENTO[ultima.tipo]}` : hv.data ? 'Sin registros' : '…'}</Dato>
        <Dato etiqueta="Próxima intervención">{proxima ? `${fecha(proxima.proximaIntervencion)} · ${TIPO_MANTENIMIENTO[proxima.tipo]}` : hv.data ? 'Sin programar' : '…'}</Dato>
      </div>
    </Link>
  )
}
