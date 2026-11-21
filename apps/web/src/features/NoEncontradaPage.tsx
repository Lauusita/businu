import { Compass } from 'lucide-react'
import { Pagina } from '../app/layouts/Pagina'
import { Vacio } from '../components/feedback/Estados'
import { Boton } from '../components/ui/Boton'
import { Tarjeta } from '../components/ui/Tarjeta'

export function NoEncontradaPage() {
  return (
    <Pagina titulo="Página no encontrada">
      <Tarjeta>
        <Vacio titulo="Esta dirección no existe" icono={<Compass className="size-5" />} accion={<Boton a="/" variante="secundario">Ir al panel</Boton>}>
          Revise el enlace o use el menú lateral para navegar.
        </Vacio>
      </Tarjeta>
    </Pagina>
  )
}
