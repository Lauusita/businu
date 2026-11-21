import { entero, porcentaje } from '../../utils/formato'

/** Dona con total al centro y leyenda con porcentajes (distribución por sexo). */
export function Dona({ partes, titulo, etiquetaTotal }: { partes: { nombre: string; valor: number; color: string }[]; titulo: string; etiquetaTotal: string }) {
  const total = partes.reduce((s, p) => s + p.valor, 0)
  const R = 54, C = 2 * Math.PI * R
  let acumulado = 0
  return (
    <div className="flex flex-wrap items-center gap-5">
      <svg viewBox="0 0 140 140" className="w-[140px] h-[140px] flex-none" role="img" aria-label={titulo}>
        <circle cx="70" cy="70" r={R} fill="none" stroke="#F4F6FB" strokeWidth="20" />
        {total > 0 && partes.map((p) => {
          const largo = (p.valor / total) * C
          const el = (
            <circle key={p.nombre} cx="70" cy="70" r={R} fill="none" stroke={p.color} strokeWidth="20"
              strokeDasharray={`${largo} ${C - largo}`} strokeDashoffset={-acumulado} transform="rotate(-90 70 70)">
              <title>{`${p.nombre}: ${entero(p.valor)}`}</title>
            </circle>
          )
          acumulado += largo
          return el
        })}
        <text x="70" y="70" textAnchor="middle" fontSize="20" fill="#051469" fontFamily="Huninn, sans-serif">{entero(total)}</text>
        <text x="70" y="88" textAnchor="middle" fontSize="10" fill="#5E6478">{etiquetaTotal}</text>
      </svg>
      <ul className="m-0 p-0 list-none flex-1 min-w-[150px] flex flex-col gap-2">
        {partes.map((p) => (
          <li key={p.nombre} className="flex items-center gap-2 text-[13px]">
            <span className="size-2 rounded-full flex-none" style={{ background: p.color }} aria-hidden />
            <span className="flex-1 text-texto-2">{p.nombre}</span>
            <b>{porcentaje(p.valor, total)}</b>
          </li>
        ))}
      </ul>
    </div>
  )
}
