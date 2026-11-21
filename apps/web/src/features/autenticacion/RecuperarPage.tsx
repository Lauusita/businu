import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { ArrowLeft, IdCard, Send } from 'lucide-react'
import { Link } from 'react-router'
import { z } from 'zod'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada } from '../../components/ui/Campo'
import { autenticacionService } from '../../services/modules/autenticacion.service'
import { MarcoAcceso } from './MarcoAcceso'

const esquema = z.object({ numeroIdentificacion: z.string().trim().min(1, 'Ingrese su número de identificación') })

/** POST /auth/request-password-change: el backend responde igual exista o no la cuenta. */
export function RecuperarPage() {
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<unknown>(null)
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<z.infer<typeof esquema>>({ resolver: zodResolver(esquema) })

  const enviar = handleSubmit(async (d) => {
    setError(null)
    try {
      const r = await autenticacionService.solicitarRecuperacion(d.numeroIdentificacion)
      setMensaje(r.mensaje)
    } catch (e) {
      setError(e)
    }
  })

  return (
    <MarcoAcceso>
      <div>
        <h1 className="m-0 font-titulo text-[30px] font-normal text-noche">Recuperar contraseña</h1>
        <p className="m-0 mt-1 text-[14px] text-texto-2">
          Te enviaremos un enlace al correo registrado. Vence en 30 minutos y solo se puede usar una vez.
        </p>
      </div>
      {mensaje ? (
        <Alerta tono="exito" titulo="Solicitud recibida">{mensaje}</Alerta>
      ) : (
        <form onSubmit={enviar} noValidate className="flex flex-col gap-4">
          <Campo etiqueta="Número de identificación" obligatorio error={errors.numeroIdentificacion?.message}>
            {(p) => <Entrada {...p} {...register('numeroIdentificacion')} icono={<IdCard />} autoComplete="username" />}
          </Campo>
          <ErrorFormulario error={error} />
          <Boton type="submit" variante="institucional" cargando={isSubmitting} className="w-full h-[48px]" icono={<Send className="size-4" />}>
            Enviar enlace
          </Boton>
        </form>
      )}
      <Link to="/login" className="inline-flex items-center gap-1.5 text-[13px] font-semibold"><ArrowLeft className="size-4" /> Volver al inicio de sesión</Link>
    </MarcoAcceso>
  )
}
