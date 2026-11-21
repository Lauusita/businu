import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { ArrowRight, Eye, EyeOff, IdCard, Lock, ShieldCheck } from 'lucide-react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { z } from 'zod'
import { useAuth } from '../../app/providers/auth-context'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada } from '../../components/ui/Campo'
import { ApiError } from '../../services/api/client'
import { MarcoAcceso } from './MarcoAcceso'

/** LoginRequest: número de identificación y contraseña. El rol lo determina el backend. */
const esquema = z.object({
  numeroIdentificacion: z.string().trim().min(1, 'Ingrese su número de identificación'),
  contrasena: z.string().min(1, 'Ingrese su contraseña'),
})
type Datos = z.infer<typeof esquema>

export function LoginPage() {
  const { usuario, iniciarSesion, motivoCierre } = useAuth()
  const navegar = useNavigate()
  const location = useLocation()
  const [ver, setVer] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Datos>({ resolver: zodResolver(esquema) })

  const destino = (location.state as { desde?: string } | null)?.desde ?? '/'
  if (usuario) return <Navigate to={destino} replace />

  const enviar = handleSubmit(async (d) => {
    setError(null)
    try {
      await iniciarSesion(d)
      navegar(destino, { replace: true })
    } catch (e) {
      setError(e instanceof ApiError && e.status === 401 ? new ApiError(401, 'Número de identificación o contraseña incorrectos.') : e)
    }
  })

  return (
    <MarcoAcceso>
      <div>
        <h1 className="m-0 font-titulo text-[32px] font-normal text-noche">Bienvenido</h1>
        <p className="m-0 mt-1 text-[14px] text-texto-2">Ingresa con el usuario asignado por el supervisor operativo para continuar.</p>
      </div>
      {motivoCierre && <Alerta tono="aviso">{motivoCierre}</Alerta>}
      <form onSubmit={enviar} noValidate className="flex flex-col gap-4">
        <Campo etiqueta="Número de identificación" obligatorio error={errors.numeroIdentificacion?.message}>
          {(p) => <Entrada {...p} {...register('numeroIdentificacion')} icono={<IdCard />} placeholder="Ej.: 1067900001" autoComplete="username" inputMode="text" />}
        </Campo>
        <Campo etiqueta="Contraseña" obligatorio error={errors.contrasena?.message}>
          {(p) => (
            <Entrada
              {...p}
              {...register('contrasena')}
              type={ver ? 'text' : 'password'}
              icono={<Lock />}
              autoComplete="current-password"
              sufijo={
                <button type="button" onClick={() => setVer((v) => !v)} aria-label={ver ? 'Ocultar contraseña' : 'Mostrar contraseña'} className="flex text-noche cursor-pointer">
                  {ver ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              }
            />
          )}
        </Campo>
        <div className="flex justify-end">
          <Link to="/recuperar-contrasena" className="text-[13px] font-semibold">¿Olvidaste tu contraseña?</Link>
        </div>
        <ErrorFormulario error={error} />
        <Boton type="submit" variante="institucional" cargando={isSubmitting} className="w-full h-[48px] text-[15px]">
          Iniciar sesión <ArrowRight className="size-4" aria-hidden />
        </Boton>
      </form>
      <div className="flex items-start gap-2.5 rounded-[12px] bg-fondo px-3.5 py-3 text-[12.5px] text-texto-2">
        <ShieldCheck className="size-4 text-exito flex-none mt-0.5" aria-hidden />
        Acceso restringido a personal autorizado. La sesión se cierra al vencer el token o al cerrar la pestaña.
      </div>
    </MarcoAcceso>
  )
}
