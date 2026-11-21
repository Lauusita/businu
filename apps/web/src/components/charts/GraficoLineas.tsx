import { COLORES_SERIE, escalaY, nfEje } from './escala'

interface Serie { nombre: string; valores: (number | null)[]; color?: string }

/** Gráfico de líneas con área bajo la primera serie (pasajeros por día, rendimiento). */
export function GraficoLineas({ etiquetas, series, alto = 240, titulo, sufijo = '' }: {
  etiquetas: string[]; series: Serie[]; alto?: number; titulo: string; sufijo?: string
}) {
  const W = 640, H = alto, mi = 40, md = 12, ms = 12, mb = 28
  const valores = series.flatMap((s) => s.valores.filter((v): v is number => v !== null))
  const { max, marcas } = escalaY(Math.max(0, ...valores))
  const n = Math.max(1, etiquetas.length - 1)
  const x = (i: number) => mi + ((W - mi - md) * i) / n
  const y = (v: number) => ms + (H - ms - mb) * (1 - v / max)
  const cada = Math.max(1, Math.ceil(etiquetas.length / 12))
  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="w-full h-auto" role="img" aria-label={titulo}>
      {marcas.map((m) => (
        <g key={m}>
          <line x1={mi} x2={W - md} y1={y(m)} y2={y(m)} stroke="#E3E8F2" />
          <text x={mi - 8} y={y(m) + 4} textAnchor="end" fontSize="10" fill="#5E6478">{nfEje.format(m)}</text>
        </g>
      ))}
      {etiquetas.map((e, i) => i % cada === 0 && (
        <text key={i} x={x(i)} y={H - 8} textAnchor="middle" fontSize="10" fill="#5E6478">{e}</text>
      ))}
      {series.map((s, si) => {
        const color = s.color ?? COLORES_SERIE[si % COLORES_SERIE.length]
        const puntos = s.valores.map((v, i) => (v === null ? null : [x(i), y(v)] as const)).filter((p): p is readonly [number, number] => p !== null)
        if (!puntos.length) return null
        const d = puntos.map((p, i) => `${i ? 'L' : 'M'}${p[0]},${p[1]}`).join(' ')
        return (
          <g key={s.nombre}>
            {si === 0 && puntos.length > 1 && (
              <path d={`${d} L${puntos.at(-1)![0]},${y(0)} L${puntos[0]![0]},${y(0)} Z`} fill={color} opacity="0.08" />
            )}
            <path d={d} fill="none" stroke={color} strokeWidth="2.5" strokeLinejoin="round" />
            {s.valores.map((v, i) => v !== null && (
              <circle key={i} cx={x(i)} cy={y(v)} r="3.5" fill="#fff" stroke={color} strokeWidth="2">
                <title>{`${s.nombre} · ${etiquetas[i]}: ${nfEje.format(v)}${sufijo}`}</title>
              </circle>
            ))}
          </g>
        )
      })}
    </svg>
  )
}
