import { api } from '../api/client'
import type { InformeMensualResponse, RedactarInformeRequest } from '../../types/api'
import type { ConjuntoExportacion } from '../../types/enums'

/** Informe mensual (numeral 4.7) y exportaciones (4.8). Solo SUPERVISOR. */
export const informesService = {
  obtener: (mes: string) => api.get<InformeMensualResponse>(`/informes/mensual/${mes}`),
  redactar: (mes: string, d: RedactarInformeRequest) => api.put<InformeMensualResponse>(`/informes/mensual/${mes}`, d),
  cerrar: (mes: string) => api.post<InformeMensualResponse>(`/informes/mensual/${mes}/cerrar`),
  entregar: (mes: string) => api.post<InformeMensualResponse>(`/informes/mensual/${mes}/entregar`),
  pdf: (mes: string) => api.descargar(`/informes/mensual/${mes}/pdf`),
  exportar: (conjunto: ConjuntoExportacion, f: { desde?: string; hasta?: string; formato: 'xlsx' | 'csv' }) =>
    api.descargar(`/exportaciones/${conjunto}`, f),
}
