import { useQuery } from '@tanstack/react-query'
import { embarcacionesService } from '../services/modules/embarcaciones.service'
import { infraestructuraService } from '../services/modules/infraestructura.service'
import { personalService } from '../services/modules/personal.service'
import { viajesService } from '../services/modules/viajes.service'
import type { Embarcaderos } from '../types/enums'

/** Consultas que usan varias pantallas; comparten caché por su clave. */
export const claves = {
  embarcaciones: ['embarcaciones'] as const,
  hojaDeVida: (id: string) => ['hoja-de-vida', id] as const,
  motores: (id?: string) => ['motores', id ?? 'todos'] as const,
  abiertas: (e?: Embarcaderos) => ['operaciones', 'abiertas', e ?? 'todos'] as const,
  operaciones: ['operaciones'] as const,
  tablero: ['infraestructura', 'tablero'] as const,
  ayudas: ['infraestructura', 'ayudas'] as const,
  personal: ['personal'] as const,
  tripulacion: ['personal', 'tripulacion-disponible'] as const,
}

export const useEmbarcaciones = () => useQuery({ queryKey: claves.embarcaciones, queryFn: embarcacionesService.listar })
export const useHojaDeVida = (id?: string) =>
  useQuery({ queryKey: claves.hojaDeVida(id ?? ''), queryFn: () => embarcacionesService.hojaDeVida(id!), enabled: !!id })
export const useMotores = (embarcacionId?: string) =>
  useQuery({ queryKey: claves.motores(embarcacionId), queryFn: () => embarcacionesService.motores(embarcacionId), enabled: !!embarcacionId })
export const useAbiertas = (embarcadero?: Embarcaderos) =>
  useQuery({ queryKey: claves.abiertas(embarcadero), queryFn: () => viajesService.abiertas(embarcadero) })
export const useTablero = () => useQuery({ queryKey: claves.tablero, queryFn: infraestructuraService.tablero })
export const useAyudas = () => useQuery({ queryKey: claves.ayudas, queryFn: infraestructuraService.ayudas })
export const usePersonal = (habilitado = true) =>
  useQuery({ queryKey: claves.personal, queryFn: personalService.listar, enabled: habilitado })
export const useTripulacionDisponible = (habilitado = true) =>
  useQuery({ queryKey: claves.tripulacion, queryFn: personalService.tripulacionDisponible, enabled: habilitado })
