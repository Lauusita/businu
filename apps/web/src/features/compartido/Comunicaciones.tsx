import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Mail, RefreshCw, Send } from 'lucide-react'
import { Cargando, ErrorCarga } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Boton } from '../../components/ui/Boton'
import { Insignia } from '../../components/ui/Insignia'
import { EncabezadoTarjeta, Tarjeta } from '../../components/ui/Tarjeta'
import { usePermiso } from '../../hooks/usePermiso'
import { comunicacionesService } from '../../services/modules/comunicaciones.service'
import { ESTADO_ENVIO, type TipoReferenciaFalla } from '../../types/enums'
import { fechaHora } from '../../utils/formato'
import { DialogoComunicar } from '../operacion/AccionesViaje'

/** Comunicaciones de fallas enviadas al supervisor del contrato para un registro (R50), con reintento. */
export function Comunicaciones({ referenciaTipo, referenciaId, asuntoSugerido }: { referenciaTipo: TipoReferenciaFalla; referenciaId: string; asuntoSugerido: string }) {
  const permitido = usePermiso('comunicaciones')
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const [abierto, setAbierto] = useState(false)
  const q = useQuery({
    queryKey: ['comunicaciones', referenciaTipo, referenciaId],
    queryFn: () => comunicacionesService.listar(referenciaTipo, referenciaId),
    enabled: permitido,
  })
  const reintentar = useMutation({
    mutationFn: comunicacionesService.reintentar,
    onSuccess: (r) => {
      queryClient.invalidateQueries({ queryKey: ['comunicaciones', referenciaTipo, referenciaId] })
      avisar(r.estado === 'ENVIADO' ? 'Correo enviado.' : `No se pudo enviar: ${r.error ?? 'error del servidor de correo'}`, r.estado === 'ENVIADO' ? 'exito' : 'error')
    },
    onError: (e) => avisar(e.message, 'error'),
  })
  if (!permitido) return null
  return (
    <Tarjeta>
      <EncabezadoTarjeta titulo="Comunicaciones al supervisor" icono={<Mail />} acciones={<Boton variante="suave" pequeno icono={<Send className="size-3.5" />} onClick={() => setAbierto(true)}>Comunicar</Boton>} />
      {q.isLoading ? <Cargando alto={60} /> : q.error ? <ErrorCarga error={q.error} /> : !q.data?.length ? (
        <p className="m-0 text-[13px] text-texto-2">Aún no se ha comunicado este registro.</p>
      ) : (
        <ul className="m-0 p-0 list-none flex flex-col gap-2">
          {q.data.map((c) => (
            <li key={c.id} className="rounded-[12px] bg-fondo px-3 py-2.5 text-[13px]">
              <div className="flex items-start justify-between gap-2">
                <b className="min-w-0 break-words">{c.asunto}</b>
                <Insignia tono={c.estado === 'ENVIADO' ? 'ok' : c.estado === 'FALLIDO' ? 'err' : 'warn'}>{ESTADO_ENVIO[c.estado]}</Insignia>
              </div>
              <div className="text-[12px] text-texto-2 mt-0.5">Para {c.destinatario} · {fechaHora(c.fechaEnvio ?? c.fechaRegistro)} · {c.intentos} intento(s)</div>
              {c.error && <div className="text-[12px] text-error mt-0.5">{c.error}</div>}
              {c.estado === 'FALLIDO' && (
                <Boton variante="secundario" pequeno className="mt-2" icono={<RefreshCw className="size-3.5" />} cargando={reintentar.isPending && reintentar.variables === c.id} onClick={() => reintentar.mutate(c.id)}>
                  Reintentar envío
                </Boton>
              )}
            </li>
          ))}
        </ul>
      )}
      <DialogoComunicar abierto={abierto} onCerrar={() => setAbierto(false)} referenciaTipo={referenciaTipo} referenciaId={referenciaId} asuntoSugerido={asuntoSugerido} />
    </Tarjeta>
  )
}
