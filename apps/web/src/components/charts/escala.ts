/** Escala "bonita" para el eje Y: máximo redondeado y 4 divisiones. */
export function escalaY(maximo: number, divisiones = 4) {
  if (maximo <= 0) return { max: divisiones, paso: 1, marcas: Array.from({ length: divisiones + 1 }, (_, i) => i) }
  const crudo = maximo / divisiones
  const mag = 10 ** Math.floor(Math.log10(crudo))
  const norm = crudo / mag
  const paso = (norm <= 1 ? 1 : norm <= 2 ? 2 : norm <= 2.5 ? 2.5 : norm <= 5 ? 5 : 10) * mag
  const max = paso * divisiones
  return { max, paso, marcas: Array.from({ length: divisiones + 1 }, (_, i) => +(i * paso).toFixed(6)) }
}

export const nfEje = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 1 })

/** Paleta de series: azul claro y dorado, como en las plantillas (Betancí / Yapel). */
export const COLORES_SERIE = ['#0A6EDC', '#EDC92A', '#051469', '#013996', '#0E8250', '#A85600']
