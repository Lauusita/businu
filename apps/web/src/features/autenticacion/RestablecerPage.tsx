import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { ArrowLeft, Check, Lock } from 'lucide-react'
import { Link, useSearchParams } from 'react-router'
import { z } from 'zod'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada } from '../../components/ui/Campo'
import { autenticacionService } from '../../services/modules/autenticacion.service'
import { MarcoAcceso } from './MarcoAcceso'

/** RestablecerContrasenaRequest: la contraseña nueva debe tener entre 8 y 72 caracteres. */
const esquema = z
  .object({
    nueva: z.string().min(8, 'Debe tener al menos 8 caracteres').max(72, 'No puede superar 72 caracteres'),
    confirmacion: z.string(),
  })
  .refine((d) => d.nueva === d.confirmacion, { path: ['confirmacion'], message: 'Las contraseñas no coinciden' })

/** Página del enlace del correo: /restablecer-contrasena?token=… → POST /auth/new-password. */
export function RestablecerPage() {
  const [params] = useSearchParams()
  const token = params.get('token') ?? ''
  const [listo, setListo] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<z.infer<typeof esquema>>({ resolver: zodResolver(esquema) })

  const enviar = handleSubmit(async (d) => {
    setError(null)
    try {
      await autenticacionService.restablecer(token, d.nueva)
      setListo(true)
    } catch (e) {
      setError(e)
    }
  })

  return (
    <MarcoAcceso>
      <div>
        <h1 className="m-0 font-titulo text-[30px] font-normal text-noche">Nueva contraseña</h1>
        <p className="m-0 mt-1 text-[14px] text-texto-2">Escribe tu contraseña nueva. Debe tener entre 8 y 72 caracteres.</p>
      </div>
      {!token ? (
        <Alerta tono="error" titulo="Enlace incompleto">Abre el enlace exactamente como llegó a tu correo o solicita uno nuevo.</Alerta>
      ) : listo ? (
        <Alerta tono="exito" titulo="Contraseña actualizada">Ya puedes iniciar sesión con tu contraseña nueva.</Alerta>
      ) : (
        <form onSubmit={enviar} noValidate className="flex flex-col gap-4">
          <Campo etiqueta="Contraseña nueva" obligatorio error={errors.nueva?.message}>
            {(p) => <Entrada {...p} {...register('nueva')} type="password" icono={<Lock />} autoComplete="new-password" />}
          </Campo>
          <Campo etiqueta="Confirmar contraseña" obligatorio error={errors.confirmacion?.message}>
            {(p) => <Entrada {...p} {...register('confirmacion')} type="password" icono={<Lock />} autoComplete="new-password" />}
          </Campo>
          <ErrorFormulario error={error} />
          <Boton type="submit" variante="institucional" cargando={isSubmitting} className="w-full h-[48px]" icono={<Check className="size-4" />}>
            Guardar contraseña
          </Boton>
        </form>
      )}
      <div className="flex justify-between gap-3 text-[13px] font-semibold">
        <Link to="/login" className="inline-flex items-center gap-1.5"><ArrowLeft className="size-4" /> Iniciar sesión</Link>
        {!listo && <Link to="/recuperar-contrasena">Solicitar otro enlace</Link>}
      </div>
    </MarcoAcceso>
  )
}
