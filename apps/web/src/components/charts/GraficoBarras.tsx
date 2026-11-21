import { COLORES_SERIE, escalaY, nfEje } from './escala'

interface Serie { nombre: string; valores: number[]; color?: string }

/** Barras verticales, agrupadas si hay varias series (franja horaria, comparativo por semana). */
export function GraficoBarras({ etiquetas, series, alto = 240, titulo, sufijo = '' }: {
  etiquetas: string[]; series: Serie[]; alto?: number; titulo: string; sufijo?: string
}) {
  const W = 640, H = alto, mi = 40, md = 8, ms = 12, mb = 28
  const { max, marcas } = escalaY(Math.max(0, ...series.flatMap((s) => s.valores)))
  const grupo = (W - mi - md) / Math.max(1, etiquetas.length)
  const ancho = Math.min(36, (grupo * 0.7) / series.length)
  const y = (v: number) => ms + (H - ms - mb) * (1 - v / max)
  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="w-full h-auto" role="img" aria-label={titulo}>
      {marcas.map((m) => (
        <g key={m}>
          <line x1={mi} x2={W - md} y1={y(m)} y2={y(m)} stroke="#E3E8F2" />
          <text x={mi - 8} y={y(m) + 4} textAnchor="end" fontSize="10" fill="#5E6478">{nfEje.format(m)}</text>
        </g>
      ))}
      {etiquetas.map((e, i) => {
        const cx = mi + grupo * i + grupo / 2
        const inicio = cx - (ancho * series.length) / 2
        return (
          <g key={e + i}>
            {series.map((s, si) => {
              const v = s.valores[i] ?? 0
              const alto = Math.max(v > 0 ? 2 : 0, y(0) - y(v))
              return (
                <rect key={s.nombre} x={inicio + si * ancho + 1} y={y(0) - alto} width={ancho - 2} height={alto} rx="3"
                  fill={s.color ?? COLORES_SERIE[si % COLORES_SERIE.length]}>
                  <title>{`${s.nombre} · ${e}: ${nfEje.format(v)}${sufijo}`}</title>
                </rect>
              )
            })}
            <text x={cx} y={H - 8} textAnchor="middle" fontSize="10" fill="#5E6478">{e}</text>
          </g>
        )
      })}
    </svg>
  )
}
