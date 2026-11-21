import { Insignia, type Tono } from './Insignia'
import {
  ESTADO_ACTIVO, ESTADO_EMBARCACION, ESTADO_OPERACION, ESTADO_PQRS, ESTADO_SEGUIMIENTO, SEVERIDAD,
  type EstadoActivo, type EstadoEmbarcacion, type EstadoOperacion, type EstadoPqrs, type EstadoSeguimiento, type Severidad,
} from '../../types/enums'

/** Insignias de estado con el tono que usan las plantillas para cada valor del backend. */
const T_EMB: Record<EstadoEmbarcacion, Tono> = { OPERATIVA: 'ok', EN_MANTENIMIENTO: 'warn', FUERA_DE_SERVICIO: 'err' }
const T_OP: Record<EstadoOperacion, Tono> = { EN_CURSO: 'info', FINALIZADA: 'ok', SUSPENDIDA: 'err' }
const T_ACT: Record<EstadoActivo, Tono> = { OPERATIVO: 'ok', OPERATIVO_CON_RESTRICCIONES: 'warn', FUERA_DE_SERVICIO: 'err' }
const T_SEG: Record<EstadoSeguimiento, Tono> = { ABIERTO: 'warn', COMUNICADO: 'info', CERRADO: 'noche' }
const T_SEV: Record<Severidad, Tono> = { BAJA: 'info', MEDIA: 'warn', ALTA: 'err', CRITICA: 'err' }
const T_PQRS: Record<EstadoPqrs, Tono> = { RECIBIDA: 'warn', EN_GESTION: 'info', RESPONDIDA: 'ok', TRASLADADA: 'gold', CERRADA: 'noche' }

export const EstadoEmbarcacionInsignia = ({ estado }: { estado: EstadoEmbarcacion }) => <Insignia tono={T_EMB[estado]}>{ESTADO_EMBARCACION[estado]}</Insignia>
export const EstadoOperacionInsignia = ({ estado }: { estado: EstadoOperacion }) => <Insignia tono={T_OP[estado]}>{ESTADO_OPERACION[estado]}</Insignia>
export const EstadoActivoInsignia = ({ estado }: { estado: EstadoActivo }) => <Insignia tono={T_ACT[estado]}>{ESTADO_ACTIVO[estado]}</Insignia>
export const SeguimientoInsignia = ({ estado }: { estado: EstadoSeguimiento }) => <Insignia tono={T_SEG[estado]}>{ESTADO_SEGUIMIENTO[estado]}</Insignia>
export const SeveridadInsignia = ({ severidad }: { severidad: Severidad }) => <Insignia tono={T_SEV[severidad]}>{SEVERIDAD[severidad]}</Insignia>
export const EstadoPqrsInsignia = ({ estado }: { estado: EstadoPqrs }) => <Insignia tono={T_PQRS[estado]}>{ESTADO_PQRS[estado]}</Insignia>
