import { useQueries } from '@tanstack/react-query'
import { combustibleService } from '../../services/modules/combustible.service'
import type { AbastecimientoResponse, EmbarcacionResponse } from '../../types/api'

/**
 * Abastecimientos de cada embarcación desde una fecha. El backend lista por embarcación
 * (GET /abastecimiento?embarcacionId=), ordenado del más reciente al más antiguo; se piden
 * 200 registros (más de lo que se carga en un mes) y se filtran por fecha.
 */
export function useAbastecimientosDesde(embarcaciones: EmbarcacionResponse[] | undefined, desdeInstant: string, habilitado = true) {
  const consultas = useQueries({
    queries: (embarcaciones ?? []).map((e) => ({
      queryKey: ['abastecimiento', e.id, { size: 200 }],
      queryFn: () => combustibleService.listar(e.id, { size: 200 }),
      enabled: habilitado,
    })),
  })
  const cargando = consultas.some((c) => c.isLoading)
  const error = consultas.find((c) => c.error)?.error
  const porEmbarcacion = new Map<string, AbastecimientoResponse[]>()
  consultas.forEach((c, i) => {
    const e = embarcaciones?.[i]
    if (e && c.data) porEmbarcacion.set(e.id, c.data.content.filter((a) => a.fechaHora >= desdeInstant))
  })
  const todos = [...porEmbarcacion.values()].flat()
  const galones = todos.reduce((s, a) => s + Number(a.galones), 0)
  return { cargando, error, porEmbarcacion, todos, galones, listo: consultas.length > 0 && consultas.every((c) => c.data) }
}
