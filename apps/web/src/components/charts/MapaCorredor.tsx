import { EMBARCADERO, ESTADO_ACTIVO, type Embarcaderos, type EstadoActivo } from '../../types/enums'

/**
 * Esquema del corredor del río Sinú (no a escala), como en las plantillas 02 y 09.
 * Las posiciones son fijas porque representan el orden del recorrido; el color de cada
 * embarcadero sale del tablero de infraestructura (GET /infraestructura/tablero).
 * El backend no reporta la posición de las embarcaciones, así que no se dibujan sobre el río.
 */
const NODOS: Record<Embarcaderos, { x: number; y: number; etiqueta: { x: number; y: number }; rol: string }> = {
  RANCHO_GRANDE: { x: 88, y: 215, etiqueta: { x: 30, y: 240 }, rol: 'Extremo del recorrido' },
  CENTRO_CALLE_22: { x: 380, y: 120, etiqueta: { x: 320, y: 50 }, rol: 'Estación intermedia' },
  CENTRO_VERDE: { x: 640, y: 180, etiqueta: { x: 600, y: 205 }, rol: 'Extremo del recorrido' },
}
const COLOR: Record<EstadoActivo, string> = { OPERATIVO: '#0E8250', OPERATIVO_CON_RESTRICCIONES: '#A85600', FUERA_DE_SERVICIO: '#C42F2F' }

export function MapaCorredor({ estados }: { estados?: Partial<Record<Embarcaderos, EstadoActivo>> }) {
  return (
    <div className="rounded-[14px] bg-[#EEF3EC] overflow-hidden">
      <svg viewBox="0 0 960 300" className="w-full h-auto block" role="img" aria-label="Esquema del corredor fluvial: Rancho Grande, Centro – Calle 22 y Centro Verde; Universidad de Córdoba en construcción">
        <path d="M-10 250 C 150 190, 260 80, 400 105 S 600 200, 700 175 S 880 70, 980 40" stroke="#C9DDF6" strokeWidth="46" fill="none" />
        <path d="M-10 250 C 150 190, 260 80, 400 105 S 600 200, 700 175 S 880 70, 980 40" stroke="#E5F0FC" strokeWidth="22" fill="none" />
        <path d="M88 215 C 200 160, 280 105, 380 120 S 560 190, 640 180" stroke="#051469" strokeWidth="3.5" fill="none" />
        <path d="M640 180 C 720 165, 790 120, 850 100" stroke="#5E6478" strokeWidth="2.5" strokeDasharray="7 6" fill="none" />
        <circle cx="850" cy="100" r="8" fill="#fff" stroke="#5E6478" strokeWidth="2" strokeDasharray="3 3" />
        <g>
          <rect x="785" y="118" width="150" height="44" rx="10" fill="#fff" />
          <text x="797" y="137" fontSize="13" fontWeight="700" fill="#051469">U. de Córdoba</text>
          <text x="797" y="153" fontSize="11" fontWeight="700" fill="#A85600">En construcción</text>
        </g>
        {(Object.keys(NODOS) as Embarcaderos[]).map((e) => {
          const n = NODOS[e]
          const estado = estados?.[e]
          return (
            <g key={e}>
              <circle cx={n.x} cy={n.y} r="11" fill="#fff" stroke="#051469" strokeWidth="4" />
              <rect x={n.etiqueta.x} y={n.etiqueta.y} width="170" height="44" rx="10" fill="#fff" />
              <circle cx={n.etiqueta.x + 14} cy={n.etiqueta.y + 16} r="4.5" fill={estado ? COLOR[estado] : '#8A90A6'} />
              <text x={n.etiqueta.x + 26} y={n.etiqueta.y + 20} fontSize="13" fontWeight="700" fill="#000">{EMBARCADERO[e]}</text>
              <text x={n.etiqueta.x + 14} y={n.etiqueta.y + 36} fontSize="11" fill="#5E6478">{estado ? ESTADO_ACTIVO[estado] : n.rol}</text>
            </g>
          )
        })}
        <text x="20" y="288" fontSize="11" fontWeight="700" fill="#013996">≈ 3,1 km entre estaciones extremas</text>
        <text x="940" y="288" fontSize="11" textAnchor="end" fill="#5E6478">+ ≈ 5,5 km hasta U. de Córdoba (proyectado)</text>
      </svg>
    </div>
  )
}
