import { createContext, useContext } from 'react'

export const MenuMovilCtx = createContext<{ abrir: () => void }>({ abrir: () => {} })
export const useMenuMovil = () => useContext(MenuMovilCtx)
