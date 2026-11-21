import { cn } from '../ui/cn'

/** Logotipo: Irish Grover, +7 % de espaciado, dorado sobre azul noche. */
export function Logo({ tamano = 40, className }: { tamano?: number; className?: string }) {
  return (
    <span className={cn('font-logo tracking-[.07em] leading-[1.1] text-dorado', className)} style={{ fontSize: tamano }}>
      Businú
    </span>
  )
}

const ONDA = 'q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0 q14 -12 28 0 t28 0'

/** Embarcación de la plantilla de inicio de sesión. */
export function IlustracionEmbarcacion({ color = '#051469', ondas = true, className }: { color?: string; ondas?: boolean; className?: string }) {
  return (
    <svg aria-hidden viewBox={ondas ? '0 0 512 280' : '40 30 420 130'} className={cn('w-full h-auto block', className)}>
      <rect x="150" y="38" width="190" height="16" rx="8" fill="#013996" />
      <rect x="120" y="54" width="250" height="56" rx="14" fill={color} />
      {[138, 170, 202, 234, 266, 298, 330].map((x, i) => (
        <rect key={x} x={x} y="68" width="22" height="18" rx="5" fill={i % 3 === 1 ? '#FEE72B' : '#EDC92A'} />
      ))}
      <path d="M70 110 H430 L398 150 H104 Z" fill={color} />
      <path d="M90 128 H416" stroke="#EDC92A" strokeWidth="4" />
      {ondas && [168, 194, 220, 246].map((y, i) => (
        <path key={y} d={`M-20 ${y} ${ONDA}`} stroke={color} strokeOpacity={0.9 - i * 0.2} strokeWidth={4 - i * 0.6} strokeLinecap="round" fill="none" />
      ))}
    </svg>
  )
}

/** Ondas decorativas del pie del menú lateral. */
export function Ondas({ color = '#EDC92A', className }: { color?: string; className?: string }) {
  return (
    <svg aria-hidden viewBox="0 0 240 40" className={cn('w-full h-auto block', className)} preserveAspectRatio="none">
      {[12, 24, 36].map((y, i) => (
        <path key={y} d={`M-10 ${y} ${ONDA}`} stroke={color} strokeOpacity={0.35 - i * 0.08} strokeWidth="2" fill="none" strokeLinecap="round" />
      ))}
    </svg>
  )
}
