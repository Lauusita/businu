import type { ReactNode } from 'react'
import { Cargando, ErrorCarga, Vacio } from '../feedback/Estados'
import { cn } from '../ui/cn'

export interface Columna<T> {
  clave: string
  titulo: ReactNode
  celda: (fila: T) => ReactNode
  alinear?: 'izquierda' | 'derecha' | 'centro'
  className?: string
}

/**
 * Tabla de la plantilla 03: encabezados en mayúsculas, filas de 12 px de relleno y desplazamiento
 * horizontal en pantallas pequeñas (sin perder columnas).
 */
export function Tabla<T>({ columnas, filas, clave, cargando, error, onReintentar, vacio, onFila, filaActiva, minimo = 720, pie }: {
  columnas: Columna<T>[]
  filas: T[] | undefined
  clave: (fila: T) => string
  cargando?: boolean
  error?: unknown
  onReintentar?: () => void
  vacio?: ReactNode
  onFila?: (fila: T) => void
  filaActiva?: (fila: T) => boolean
  minimo?: number
  pie?: ReactNode
}) {
  const alin = (a?: Columna<T>['alinear']) => (a === 'derecha' ? 'text-right' : a === 'centro' ? 'text-center' : 'text-left')
  return (
    <div className="min-w-0">
      <div className="overflow-x-auto">
        <table className="tabla" style={{ minWidth: minimo }}>
          <thead>
            <tr>
              {columnas.map((c) => (
                <th key={c.clave} scope="col" className={alin(c.alinear)}>{c.titulo}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {!cargando && !error && filas?.map((f) => (
              <tr
                key={clave(f)}
                onClick={onFila ? () => onFila(f) : undefined}
                className={cn(onFila && 'cursor-pointer', filaActiva?.(f) && '[&>td]:bg-azul-suave')}
              >
                {columnas.map((c) => (
                  <td key={c.clave} className={cn(alin(c.alinear), c.className)}>{c.celda(f)}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {cargando && <Cargando />}
      {!cargando && !!error && <ErrorCarga error={error} onReintentar={onReintentar} />}
      {!cargando && !error && filas && filas.length === 0 && (vacio ?? <Vacio>No hay registros para los filtros seleccionados.</Vacio>)}
      {pie}
    </div>
  )
}
