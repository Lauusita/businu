import { useQuery } from '@tanstack/react-query'
import { History, Pencil, Plus, Trash2 } from 'lucide-react'
import { Cargando, ErrorCarga } from '../../components/feedback/Estados'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { usePermiso } from '../../hooks/usePermiso'
import { auditoriaService } from '../../services/modules/auditoria.service'
import type { TipoRegistroAuditado } from '../../types/enums'
import { fechaHora } from '../../utils/formato'

const ACCION = { CREACION: { texto: 'Registro creado', icono: Plus }, MODIFICACION: { texto: 'Registro modificado', icono: Pencil }, ELIMINACION: { texto: 'Registro eliminado', icono: Trash2 } }

/** «Trazabilidad del registro»: historial de cambios con autor y fecha (GET /auditoria, R5). Solo supervisor. */
export function Trazabilidad({ tipo, id }: { tipo: TipoRegistroAuditado; id: string }) {
  const permitido = usePermiso('auditoria')
  const q = useQuery({ queryKey: ['auditoria', tipo, id], queryFn: () => auditoriaService.historial(tipo, id), enabled: permitido })
  if (!permitido) return null
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Trazabilidad del registro" icono={<History />} />
      {q.isLoading ? <Cargando alto={60} /> : q.error ? <ErrorCarga error={q.error} /> : !q.data?.length ? (
        <p className="m-0 text-[13px] text-texto-2">Sin historial de cambios.</p>
      ) : (
        <ol className="m-0 p-0 list-none flex flex-col">
          {q.data.map((r, i, arr) => {
            const a = ACCION[r.accion]
            return (
              <li key={r.revision} className="flex gap-3">
                <div className="flex flex-col items-center">
                  <span className="size-7 rounded-lg bg-azul-suave text-azul flex items-center justify-center" aria-hidden><a.icono className="size-3.5" /></span>
                  {i < arr.length - 1 && <span className="w-px flex-1 bg-borde my-1" aria-hidden />}
                </div>
                <div className="pb-3 min-w-0">
                  <div className="font-bold text-[13.5px]">{a.texto}</div>
                  <div className="text-[12.5px] text-texto-2">{r.autor}</div>
                  {r.camposModificados.length > 0 && <div className="text-[12px] text-texto-2 break-words">Campos: {r.camposModificados.join(', ')}</div>}
                  <div className="text-[12px] text-texto-3">{fechaHora(r.fecha)}</div>
                </div>
              </li>
            )
          })}
        </ol>
      )}
    </Tarjeta>
  )
}
