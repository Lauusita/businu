import { Plus, Trash2 } from 'lucide-react'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada, Selector } from '../../components/ui/Campo'
import { CONDICION_MECANICA, SEVERIDAD, opciones, type CondicionMecanica, type Severidad } from '../../types/enums'

import type { NovedadEditable } from './novedades'

/**
 * Lista de novedades mecánicas (NovedadMecanicaDTO). Lista vacía = «Sin novedad».
 * Reglas del backend: «Otra» exige detalle y una condición no se repite (salvo «Otra»).
 */
export function NovedadesMecanicas({ valor, onCambio, errores }: { valor: NovedadEditable[]; onCambio: (v: NovedadEditable[]) => void; errores?: Record<number, string> }) {
  const cambiar = (i: number, cambio: Partial<NovedadEditable>) => onCambio(valor.map((n, j) => (j === i ? { ...n, ...cambio } : n)))
  return (
    <div className="flex flex-col gap-3">
      {valor.length === 0 && <p className="m-0 text-[13px] text-texto-2">Sin novedad mecánica. Agregue una si se presentó alguna condición.</p>}
      {valor.map((n, i) => (
        <div key={i} className="grid gap-3 sm:grid-cols-[1.3fr_0.8fr_1.6fr_auto] items-start p-3 rounded-[12px] bg-fondo">
          <Campo etiqueta="Condición" obligatorio error={errores?.[i]}>
            {(p) => (
              <Selector {...p} vacio="Seleccione…" opciones={opciones(CONDICION_MECANICA)} value={n.condicion}
                onChange={(e) => cambiar(i, { condicion: e.target.value as CondicionMecanica })} />
            )}
          </Campo>
          <Campo etiqueta="Severidad" obligatorio>
            {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(SEVERIDAD)} value={n.severidad} onChange={(e) => cambiar(i, { severidad: e.target.value as Severidad })} />}
          </Campo>
          <Campo etiqueta="Detalle" obligatorio={n.condicion === 'OTRA'} opcional={n.condicion !== 'OTRA'}>
            {(p) => <Entrada {...p} maxLength={2000} value={n.detalle} onChange={(e) => cambiar(i, { detalle: e.target.value })} placeholder="Qué se observó" />}
          </Campo>
          <Boton variante="fantasma" soloIcono aria-label="Quitar novedad" className="sm:mt-[26px]" onClick={() => onCambio(valor.filter((_, j) => j !== i))}>
            <Trash2 className="size-4" />
          </Boton>
        </div>
      ))}
      <div>
        <Boton variante="suave" pequeno icono={<Plus className="size-3.5" />} onClick={() => onCambio([...valor, { condicion: '', severidad: '', detalle: '' }])}>
          Agregar novedad mecánica
        </Boton>
      </div>
    </div>
  )
}

