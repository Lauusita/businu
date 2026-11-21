import { useQueries } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { IdCard, MessageSquare, Search, ShieldCheck, Ship, UserPlus } from 'lucide-react'
import { useNavigate } from 'react-router'
import { Pagina } from '../../app/layouts/Pagina'
import { Tabla } from '../../components/tables/Tabla'
import { Alerta } from '../../components/ui/Alerta'
import { Avatar } from '../../components/ui/Avatar'
import { Boton } from '../../components/ui/Boton'
import { Entrada, Selector } from '../../components/ui/Campo'
import { CajaIcono } from '../../components/ui/CajaIcono'
import { Insignia } from '../../components/ui/Insignia'
import { Tarjeta } from '../../components/ui/Tarjeta'
import { usePersonal, useTripulacionDisponible } from '../../hooks/consultas'
import { personalService } from '../../services/modules/personal.service'
import type { DocumentacionPersonaResponse, PersonalResponse } from '../../types/api'
import { ROL, opciones, type Rol } from '../../types/enums'
import { mesActual } from '../../utils/formato'
import { DialogoVincular } from './DialogoVincular'

const ICONO: Record<Rol, React.ReactNode> = {
  SUPERVISOR: <ShieldCheck />, OPERADOR_EMBARCACION: <Ship />, GUIA_TURISTICA: <MessageSquare />, TAQUILLERO: <IdCard />,
}

/** Plantilla 14 · Gestión del personal (GET /personal y documentación por persona, numeral 4.6). */
export function PersonalPage() {
  const navegar = useNavigate()
  const personal = usePersonal()
  const disponibles = useTripulacionDisponible()
  const [vincular, setVincular] = useState(false)
  const [buscar, setBuscar] = useState('')
  const [rol, setRol] = useState<Rol | ''>('')
  const [doc, setDoc] = useState<'' | 'COMPLETA' | 'PENDIENTE'>('')

  const docs = useQueries({
    queries: (personal.data ?? []).map((p) => ({ queryKey: ['documentos', p.id, false], queryFn: () => personalService.documentacion(p.id) })),
  })
  const docPorId = useMemo(() => {
    const m = new Map<string, DocumentacionPersonaResponse>()
    docs.forEach((d) => d.data && m.set(d.data.personalId, d.data))
    return m
  }, [docs])
  const disponiblesIds = new Set(disponibles.data?.map((p) => p.id))

  const filas = (personal.data ?? []).filter((p) => {
    const texto = `${p.nombres} ${p.apellidos} ${p.numeroIdentificacion}`.toLowerCase()
    if (buscar && !texto.includes(buscar.toLowerCase())) return false
    if (rol && p.rol !== rol) return false
    const d = docPorId.get(p.id)
    if (doc === 'COMPLETA' && (!d || d.faltantes.length)) return false
    if (doc === 'PENDIENTE' && (!d || !d.faltantes.length)) return false
    return true
  })
  const mes = mesActual()
  const sinPila = (personal.data ?? []).filter((p) => docPorId.get(p.id) && !docPorId.get(p.id)!.planillasPila.includes(mes))
  const conFaltantes = (personal.data ?? []).filter((p) => docPorId.get(p.id)?.faltantes.length)

  return (
    <Pagina
      titulo="Personal"
      migas={[{ etiqueta: 'Personal' }]}
      descripcion="Equipo de la operación (numeral 7.3) y su documentación (numeral 4.6)."
      acciones={<Boton icono={<UserPlus className="size-4" />} onClick={() => setVincular(true)}>Vincular colaborador</Boton>}
    >
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        {(Object.keys(ROL) as Rol[]).map((r) => (
          <Tarjeta key={r}>
            <div className="flex items-center gap-3">
              <CajaIcono tono={r === 'GUIA_TURISTICA' ? 'dorado' : r === 'TAQUILLERO' ? 'exito' : 'azul'}>{ICONO[r]}</CajaIcono>
              <div className="min-w-0">
                <div className="text-[12.5px] text-texto-2 leading-tight">{ROL[r]}</div>
                <div className="font-titulo text-[24px] text-noche">{personal.data ? personal.data.filter((p) => p.rol === r).length : '—'}</div>
              </div>
            </div>
          </Tarjeta>
        ))}
      </div>

      {(conFaltantes.length > 0 || sinPila.length > 0) && (
        <div className="grid gap-3 md:grid-cols-2">
          {conFaltantes.length > 0 && <Alerta tono="aviso" titulo={`${conFaltantes.length} colaborador(es) con documentos pendientes`}>{conFaltantes.slice(0, 3).map((p) => `${p.nombres} ${p.apellidos}`).join(', ')}{conFaltantes.length > 3 && '…'}</Alerta>}
          {sinPila.length > 0 && <Alerta tono="dorado" titulo={`Planilla de seguridad social (PILA) de ${mes} pendiente`}>{sinPila.length} colaborador(es) sin planilla cargada. Requerida para el informe mensual.</Alerta>}
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-[1.6fr_1fr_1fr]">
        <Entrada aria-label="Buscar por nombre o documento" placeholder="Buscar por nombre o documento" icono={<Search />} value={buscar} onChange={(e) => setBuscar(e.target.value)} />
        <Selector aria-label="Rol" vacio="Todos los roles" opciones={opciones(ROL)} value={rol} onChange={(e) => setRol(e.target.value as Rol)} />
        <Selector aria-label="Documentación" vacio="Documentación: todas" value={doc} onChange={(e) => setDoc(e.target.value as typeof doc)}
          opciones={[{ valor: 'COMPLETA', etiqueta: 'Documentación completa' }, { valor: 'PENDIENTE', etiqueta: 'Con documentos pendientes' }]} />
      </div>

      <Tarjeta sinRelleno>
        <Tabla<PersonalResponse>
          columnas={[
            { clave: 'n', titulo: 'Colaborador', celda: (p) => <span className="inline-flex items-center gap-2.5 font-bold"><Avatar nombre={`${p.nombres} ${p.apellidos}`} tamano={30} />{p.nombres} {p.apellidos}</span> },
            { clave: 'i', titulo: 'Identificación', celda: (p) => `${p.tipoIdentificacion} ${p.numeroIdentificacion}` },
            { clave: 'r', titulo: 'Rol', celda: (p) => ROL[p.rol] },
            {
              clave: 'd', titulo: 'Disponibilidad', celda: (p) =>
                p.rol === 'OPERADOR_EMBARCACION' || p.rol === 'GUIA_TURISTICA'
                  ? disponiblesIds.has(p.id) ? <Insignia tono="ok">Disponible</Insignia> : <Insignia tono="neutral">En viaje o retirado</Insignia>
                  : <span className="text-texto-2">No aplica</span>,
            },
            {
              clave: 'doc', titulo: 'Documentación', celda: (p) => {
                const d = docPorId.get(p.id)
                if (!d) return <span className="text-texto-3">…</span>
                return d.faltantes.length ? <Insignia tono="warn">{d.faltantes.length} pendiente(s)</Insignia> : <Insignia tono="ok">Completa</Insignia>
              },
            },
            {
              clave: 'pila', titulo: 'Seguridad social', celda: (p) => {
                const d = docPorId.get(p.id)
                if (!d) return <span className="text-texto-3">…</span>
                return d.planillasPila.includes(mes) ? <Insignia tono="ok">Al día</Insignia> : <Insignia tono="warn">PILA pendiente</Insignia>
              },
            },
            { clave: 'c', titulo: 'Correo', celda: (p) => p.email ?? '—' },
          ]}
          filas={filas}
          clave={(p) => p.id}
          cargando={personal.isLoading}
          error={personal.error}
          onReintentar={() => personal.refetch()}
          onFila={(p) => navegar(`/personal/${p.id}`)}
          minimo={1080}
          pie={personal.data && <p className="m-0 px-4 py-3 border-t border-borde text-[13px] text-texto-2">Mostrando {filas.length} de {personal.data.length} colaboradores</p>}
        />
      </Tarjeta>
      {vincular && <DialogoVincular onCerrar={() => setVincular(false)} />}
    </Pagina>
  )
}
