import type { CondicionMecanica, Severidad } from '../../types/enums'

export interface NovedadEditable { condicion: CondicionMecanica | ''; severidad: Severidad | ''; detalle: string }

/** Valida como NovedadMecanica.validar() del backend. Devuelve errores por fila. */
export function validarNovedades(lista: NovedadEditable[]): Record<number, string> {
  const errores: Record<number, string> = {}
  const vistas = new Set<string>()
  lista.forEach((n, i) => {
    if (!n.condicion || !n.severidad) errores[i] = 'Seleccione la condición y la severidad'
    else if (n.condicion === 'OTRA' && !n.detalle.trim()) errores[i] = 'Describa la condición «Otra»'
    else if (n.condicion !== 'OTRA' && vistas.has(n.condicion)) errores[i] = 'Esta condición ya está en la lista'
    if (n.condicion) vistas.add(n.condicion)
  })
  return errores
}

export const aDTO = (lista: NovedadEditable[]) =>
  lista.map((n) => ({ condicion: n.condicion as string, severidad: n.severidad as Severidad, detalle: n.detalle.trim() || null }))
