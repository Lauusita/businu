import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada, Selector } from '../../components/ui/Campo'
import { Modal } from '../../components/ui/Modal'
import { ApiError } from '../../services/api/client'
import { personalService } from '../../services/modules/personal.service'
import { ROL, TIPO_IDENTIFICACION, TIPO_VINCULACION, opciones, usaSistema, type Rol } from '../../types/enums'

/** Validaciones de PersonalDTO, con los mismos mensajes del backend. */
const LETRAS = /^\p{L}[\p{L} '-]*$/u
const esquema = z
  .object({
    tipoIdentificacion: z.enum(['CC', 'CE', 'PPT', 'PASAPORTE'], { message: 'El tipo de identificación es obligatorio' }),
    numeroIdentificacion: z.string().trim().regex(/^[A-Za-z0-9]{5,20}$/, 'El número de identificación debe tener entre 5 y 20 letras o números, sin puntos ni espacios'),
    nombres: z.string().trim().min(2, 'Los nombres deben tener entre 2 y 80 caracteres').max(80, 'Los nombres deben tener entre 2 y 80 caracteres').regex(LETRAS, 'Los nombres solo pueden contener letras, espacios, apóstrofo y guion'),
    apellidos: z.string().trim().min(2, 'Los apellidos deben tener entre 2 y 80 caracteres').max(80, 'Los apellidos deben tener entre 2 y 80 caracteres').regex(LETRAS, 'Los apellidos solo pueden contener letras, espacios, apóstrofo y guion'),
    email: z.string().trim().min(1, 'El correo es obligatorio').max(120, 'El correo no puede superar 120 caracteres').email('El correo no es válido'),
    rol: z.enum(['SUPERVISOR', 'GUIA_TURISTICA', 'OPERADOR_EMBARCACION', 'TAQUILLERO'], { message: 'El rol es obligatorio' }),
    tipoVinculacion: z.enum(['LABORAL', 'PRESTACION_SERVICIOS'], { message: 'El tipo de vinculación es obligatorio' }),
    password: z.string().optional(),
  })
  .superRefine((d, ctx) => {
    if (!d.rol) return
    if (usaSistema(d.rol as Rol)) {
      if (!d.password || d.password.length < 8 || d.password.length > 72)
        ctx.addIssue({ code: 'custom', path: ['password'], message: 'La contraseña debe tener entre 8 y 72 caracteres' })
    }
  })
type Datos = z.infer<typeof esquema>

export function DialogoVincular({ onCerrar }: { onCerrar: () => void }) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const { register, handleSubmit, control, setError, formState: { errors } } = useForm<Datos>({ resolver: zodResolver(esquema), defaultValues: { tipoIdentificacion: 'CC', tipoVinculacion: 'LABORAL' } })
  const rol = useWatch({ control, name: 'rol' })
  const conAcceso = rol ? usaSistema(rol) : true

  const m = useMutation({
    mutationFn: (d: Datos) => personalService.crear({ ...d, password: usaSistema(d.rol) ? d.password : undefined }),
    onSuccess: (p) => {
      queryClient.invalidateQueries({ queryKey: ['personal'] })
      avisar(`${p.nombres} ${p.apellidos} quedó vinculado como ${ROL[p.rol].toLowerCase()}.`)
      onCerrar()
    },
    onError: (e) => {
      // Errores de validación del backend junto a su campo
      if (e instanceof ApiError) Object.entries(e.errores).forEach(([campo, msg]) => setError(campo as keyof Datos, { message: msg }))
    },
  })

  return (
    <Modal abierto onCerrar={onCerrar} titulo="Vincular colaborador" subtitulo="Equipo mínimo exigido por el anexo técnico (numeral 7.3)." ancho="lg"
      pie={<><Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton><Boton variante="institucional" cargando={m.isPending} onClick={handleSubmit((d) => m.mutate(d))}>Vincular</Boton></>}>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo etiqueta="Tipo de identificación" obligatorio error={errors.tipoIdentificacion?.message}>{(p) => <Selector {...p} {...register('tipoIdentificacion')} opciones={opciones(TIPO_IDENTIFICACION)} />}</Campo>
        <Campo etiqueta="Número de identificación" obligatorio error={errors.numeroIdentificacion?.message}>{(p) => <Entrada {...p} {...register('numeroIdentificacion')} autoComplete="off" />}</Campo>
        <Campo etiqueta="Nombres" obligatorio error={errors.nombres?.message}>{(p) => <Entrada {...p} {...register('nombres')} />}</Campo>
        <Campo etiqueta="Apellidos" obligatorio error={errors.apellidos?.message}>{(p) => <Entrada {...p} {...register('apellidos')} />}</Campo>
        <Campo etiqueta="Correo" obligatorio error={errors.email?.message}>{(p) => <Entrada {...p} {...register('email')} type="email" />}</Campo>
        <Campo etiqueta="Rol" obligatorio error={errors.rol?.message}>{(p) => <Selector {...p} {...register('rol')} vacio="Seleccione…" opciones={opciones(ROL)} />}</Campo>
        <Campo etiqueta="Tipo de vinculación" obligatorio error={errors.tipoVinculacion?.message}>{(p) => <Selector {...p} {...register('tipoVinculacion')} opciones={opciones(TIPO_VINCULACION)} />}</Campo>
        {conAcceso ? (
          <Campo etiqueta="Contraseña inicial" obligatorio ayuda="Entre 8 y 72 caracteres. La persona puede cambiarla con «¿Olvidaste tu contraseña?»." error={errors.password?.message}>
            {(p) => <Entrada {...p} {...register('password')} type="password" autoComplete="new-password" />}
          </Campo>
        ) : (
          <p className="m-0 self-end text-[12.5px] text-texto-2">Los guías de turismo no tienen acceso al sistema: no se les asigna contraseña.</p>
        )}
      </div>
      {m.error && !(m.error instanceof ApiError && Object.keys(m.error.errores).length) && <ErrorFormulario error={m.error} />}
    </Modal>
  )
}
