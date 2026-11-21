import type { LoginResponse } from '../../types/api'

/**
 * Sesión del usuario.
 *
 * El backend emite un JWT sin token de renovación. Se guarda en sessionStorage (no en localStorage):
 * se borra al cerrar la pestaña y no queda en el equipo, lo que importa en taquillas compartidas.
 * Se respeta la vigencia (expiresIn): un token vencido nunca se reutiliza. Si el navegador bloquea
 * el almacenamiento, la sesión vive solo en memoria mientras la página esté abierta.
 */
export interface DatosSesion {
  token: string
  venceEn: number // epoch ms
  personalId: string
  nombre: string
  rol: LoginResponse['rol']
}

const CLAVE = 'businu.sesion'
let memoria: DatosSesion | null = null

const vigente = (d: DatosSesion | null) => (d && d.token && Date.now() < d.venceEn ? d : null)

function leerAlmacenada(): DatosSesion | null {
  try {
    const crudo = sessionStorage.getItem(CLAVE)
    return crudo ? (JSON.parse(crudo) as DatosSesion) : null
  } catch {
    return null
  }
}

export const sesion = {
  actual(): DatosSesion | null {
    const d = vigente(memoria) ?? vigente(leerAlmacenada())
    if (!d) this.cerrar()
    return d
  },
  token(): string | null {
    return this.actual()?.token ?? null
  },
  guardar(r: LoginResponse): DatosSesion {
    const d: DatosSesion = {
      token: r.accessToken,
      // margen de 30 s para no enviar un token a punto de vencer
      venceEn: Date.now() + Math.max(0, r.expiresIn - 30) * 1000,
      personalId: r.personalId,
      nombre: r.nombre,
      rol: r.rol,
    }
    memoria = d
    try {
      sessionStorage.setItem(CLAVE, JSON.stringify(d))
    } catch {
      /* almacenamiento bloqueado: queda en memoria */
    }
    return d
  },
  cerrar() {
    memoria = null
    try {
      sessionStorage.removeItem(CLAVE)
    } catch {
      /* sin almacenamiento */
    }
  },
}
