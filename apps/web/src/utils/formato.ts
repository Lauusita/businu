/**
 * Formatos en español de Colombia. El backend envía instantes en UTC; la operación ocurre en
 * Montería, así que todas las fechas y horas se muestran en America/Bogota (UTC−5, sin horario de verano).
 */
export const ZONA = 'America/Bogota'
const LOCALE = 'es-CO'

const fmt = (opciones: Intl.DateTimeFormatOptions) => new Intl.DateTimeFormat(LOCALE, { timeZone: ZONA, ...opciones })
const fFecha = fmt({ day: '2-digit', month: '2-digit', year: 'numeric' })
const fHora = fmt({ hour: 'numeric', minute: '2-digit', hour12: true })
const fFechaLarga = fmt({ weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
const fMes = fmt({ month: 'long', year: 'numeric' })
const fDiaMes = fmt({ day: '2-digit', month: '2-digit' })

const aFecha = (v: string | number | Date) => (v instanceof Date ? v : new Date(v))

export const fecha = (v?: string | null) => (v ? fFecha.format(aFecha(v)) : '—')
export const hora = (v?: string | null) => (v ? fHora.format(aFecha(v)) : '—')
export const fechaHora = (v?: string | null) => (v ? `${fFecha.format(aFecha(v))} · ${fHora.format(aFecha(v))}` : '—')
export const fechaLarga = (v?: string | null) => (v ? fFechaLarga.format(aFecha(v)) : '—')
export const diaMes = (v?: string | null) => (v ? fDiaMes.format(aFecha(v)) : '—')

/** LocalDate 'yyyy-MM-dd' (sin zona) → dd/MM/yyyy. */
export const fechaLocal = (v?: string | null) => {
  if (!v) return '—'
  const [a, m, d] = v.split('-')
  return `${d}/${m}/${a}`
}

/** 'yyyy-MM' → "noviembre de 2026". */
export const mesLargo = (ym: string) => {
  const [a, m] = ym.split('-').map(Number)
  return fMes.format(new Date(Date.UTC(a, m - 1, 15)))
}

/** Partes de "ahora" en Bogotá. */
function partesBogota(d = new Date()) {
  const p = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', {
      timeZone: ZONA, year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false,
    }).formatToParts(d).map((x) => [x.type, x.value]),
  )
  return { anio: p.year, mes: p.month, dia: p.day, hora: p.hour === '24' ? '00' : p.hour, minuto: p.minute }
}

/** Fecha de hoy en Bogotá, 'yyyy-MM-dd'. */
export const hoyISO = () => {
  const p = partesBogota()
  return `${p.anio}-${p.mes}-${p.dia}`
}
/** Mes actual en Bogotá, 'yyyy-MM'. */
export const mesActual = () => hoyISO().slice(0, 7)
/** Primer día del mes 'yyyy-MM' → 'yyyy-MM-01'. */
export const inicioMes = (ym = mesActual()) => `${ym}-01`
/** Último día del mes 'yyyy-MM'. */
export const finMes = (ym: string) => {
  const [a, m] = ym.split('-').map(Number)
  const d = new Date(Date.UTC(a, m, 0)).getUTCDate()
  return `${ym}-${String(d).padStart(2, '0')}`
}
/** Suma meses a 'yyyy-MM'. */
export const sumarMes = (ym: string, n: number) => {
  const [a, m] = ym.split('-').map(Number)
  const d = new Date(Date.UTC(a, m - 1 + n, 1))
  return `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, '0')}`
}

/** Valor para <input type="datetime-local"> con la hora actual de Bogotá. */
export const ahoraLocal = () => {
  const p = partesBogota()
  return `${p.anio}-${p.mes}-${p.dia}T${p.hora}:${p.minuto}`
}
/** Instant UTC → valor de <input type="datetime-local"> en Bogotá. */
export const aLocal = (iso: string) => {
  const p = partesBogota(new Date(iso))
  return `${p.anio}-${p.mes}-${p.dia}T${p.hora}:${p.minuto}`
}
/** <input type="datetime-local"> (hora de Bogotá) → Instant ISO UTC. */
export const localAInstant = (local: string) => new Date(`${local}:00-05:00`).toISOString()
/** <input type="date"> + "HH:mm" (Bogotá) → Instant ISO UTC. */
export const fechaHoraAInstant = (f: string, h: string) => new Date(`${f}T${h}:00-05:00`).toISOString()
/** Inicio y fin de un día de Bogotá como Instants. */
export const rangoDia = (f: string) => ({
  desde: new Date(`${f}T00:00:00-05:00`).toISOString(),
  hasta: new Date(`${f}T23:59:59.999-05:00`).toISOString(),
})

const nf0 = new Intl.NumberFormat(LOCALE, { maximumFractionDigits: 0 })
const nf1 = new Intl.NumberFormat(LOCALE, { maximumFractionDigits: 1 })
const nf2 = new Intl.NumberFormat(LOCALE, { maximumFractionDigits: 2 })
const cop = new Intl.NumberFormat(LOCALE, { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })

export const entero = (n?: number | null) => (n === undefined || n === null ? '—' : nf0.format(n))
export const decimal = (n?: number | null, d: 1 | 2 = 1) =>
  n === undefined || n === null ? '—' : (d === 1 ? nf1 : nf2).format(n)
export const pesos = (n?: number | null) => (n === undefined || n === null ? '—' : cop.format(n))
export const porcentaje = (parte: number, total: number) => (total > 0 ? `${nf0.format((parte / total) * 100)} %` : '—')

export const bytes = (b: number) =>
  b < 1024 ? `${b} B` : b < 1024 * 1024 ? `${nf0.format(b / 1024)} KB` : `${nf1.format(b / 1024 / 1024)} MB`

/** Iniciales para el avatar: "Carlos Méndez" → "CM". */
export const iniciales = (nombre: string) =>
  nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]!.toUpperCase())
    .join('')

/** Duración entre dos instantes en minutos u horas. */
export const duracion = (desde?: string | null, hasta?: string | null) => {
  if (!desde || !hasta) return '—'
  const min = Math.round((new Date(hasta).getTime() - new Date(desde).getTime()) / 60000)
  if (min < 60) return `${min} min`
  return `${Math.floor(min / 60)} h ${min % 60} min`
}

/** Identificador corto y legible de un registro con id UUID. */
export const codigoCorto = (prefijo: string, id: string) => `${prefijo}-${id.slice(0, 8).toUpperCase()}`

/**
 * El backend guarda textos por defecto ("Sin novedades", "Sin observaciones", "No aplica") cuando
 * el campo queda vacío. Para la interfaz eso equivale a no tener texto.
 */
const TEXTOS_POR_DEFECTO = new Set(['Sin novedades', 'Sin observaciones', 'No aplica'])
export const textoReal = (v?: string | null) => (v && !TEXTOS_POR_DEFECTO.has(v.trim()) ? v : undefined)

/**
 * Para los campos que arrancan en «ahora»: si la persona no cambió el valor se envía null y el
 * backend usa su hora exacta (con segundos). Así no se rechaza, por ejemplo, levantar una
 * suspensión en el mismo minuto en que se registró.
 */
export const instanteSiCambio = (valor: string, inicial: string) => (valor && valor !== inicial ? localAInstant(valor) : null)
