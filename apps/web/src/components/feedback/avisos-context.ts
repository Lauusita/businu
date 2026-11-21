import { createContext, useContext } from 'react'

export type TipoAviso = 'exito' | 'error' | 'info'
export interface AvisosContexto { avisar: (mensaje: string, tipo?: TipoAviso) => void }
export const AvisosCtx = createContext<AvisosContexto>({ avisar: () => {} })
export const useAvisos = () => useContext(AvisosCtx)
