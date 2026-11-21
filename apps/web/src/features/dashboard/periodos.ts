import { hoyISO, inicioMes } from '../../utils/formato'

export type Periodo = 'HOY' | 'SEMANA' | 'MES'
export const PERIODOS: { valor: Periodo; etiqueta: string }[] = [
  { valor: 'HOY', etiqueta: 'Hoy' },
  { valor: 'SEMANA', etiqueta: 'Semana' },
  { valor: 'MES', etiqueta: 'Mes' },
]

/** Rango de fechas (hora de Bogotá) del periodo; la semana empieza el lunes, como en el backend. */
export function rangoPeriodo(p: Periodo): { desde: string; hasta: string } {
  const hoy = hoyISO()
  if (p === 'HOY') return { desde: hoy, hasta: hoy }
  if (p === 'MES') return { desde: inicioMes(), hasta: hoy }
  const d = new Date(`${hoy}T12:00:00Z`)
  const lunes = new Date(d)
  lunes.setUTCDate(d.getUTCDate() - ((d.getUTCDay() + 6) % 7))
  return { desde: lunes.toISOString().slice(0, 10), hasta: hoy }
}
