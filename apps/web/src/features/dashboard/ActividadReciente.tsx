import { useQuery } from '@tanstack/react-query'
import { CircleCheck, CirclePause, Route } from 'lucide-react'
import { Link } from 'react-router'
import { Cargando, ErrorCarga, Vacio } from '../../components/feedback/Estados'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { viajesService } from '../../services/modules/viajes.service'
import { EMBARCACION, ESTADO_OPERACION, nombreEmbarcadero } from '../../types/enums'
import { codigoCorto, entero, fechaHora } from '../../utils/formato'

/** «Actividad operativa reciente»: los últimos viajes registrados. */
export function ActividadReciente() {
  const q = useQuery({ queryKey: ['operaciones', 'recientes'], queryFn: () => viajesService.listar({}, { size: 5 }) })
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Actividad operativa reciente" acciones={<Link to="/viajes" className="text-[13px] font-semibold">Ver historial</Link>} />
      {q.isLoading ? <Cargando /> : q.error ? <ErrorCarga error={q.error} onReintentar={() => q.refetch()} /> :
        !q.data?.content.length ? <Vacio titulo="Sin viajes registrados" /> : (
          <ol className="m-0 p-0 list-none flex flex-col">
            {q.data.content.map((o, i, arr) => {
              const Icono = o.estado === 'FINALIZADA' ? CircleCheck : o.estado === 'SUSPENDIDA' ? CirclePause : Route
              return (
                <li key={o.id} className="flex gap-3">
                  <div className="flex flex-col items-center">
                    <span className="size-7 rounded-lg bg-azul-suave text-azul flex items-center justify-center flex-none" aria-hidden><Icono className="size-4" /></span>
                    {i < arr.length - 1 && <span className="w-px flex-1 bg-borde my-1" aria-hidden />}
                  </div>
                  <div className="pb-4 min-w-0">
                    <Link to={`/viajes/${o.id}`} className="font-bold text-[13.5px] text-negro no-underline hover:text-azul">
                      Viaje {codigoCorto('V', o.id)} · {ESTADO_OPERACION[o.estado].toLowerCase()}
                    </Link>
                    <div className="text-[12.5px] text-texto-2">
                      {EMBARCACION[o.embarcacion]} · {nombreEmbarcadero(o.embarcaderoOrigen)} a {nombreEmbarcadero(o.embarcaderoDestino)} · {entero(o.totalUsuariosTransportados)} pasajeros
                    </div>
                    <div className="text-[12px] text-texto-3">{fechaHora(o.fechaHoraInicio)}</div>
                  </div>
                </li>
              )
            })}
          </ol>
        )}
    </Tarjeta>
  )
}
