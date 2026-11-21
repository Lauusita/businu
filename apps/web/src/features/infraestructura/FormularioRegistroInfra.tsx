import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Check } from 'lucide-react'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { CargaArchivos } from '../../components/forms/CargaArchivos'
import { Alerta } from '../../components/ui/Alerta'
import { Boton } from '../../components/ui/Boton'
import { AreaTexto, Campo, Entrada, Interruptor, Selector } from '../../components/ui/Campo'
import { Chips, Segmentado } from '../../components/ui/Selecciones'
import { useAyudas } from '../../hooks/consultas'
import { infraestructuraService } from '../../services/modules/infraestructura.service'
import type { RegistroInfraResponse, Soporte } from '../../types/api'
import {
  ASPECTO_AFECTADO, COMPONENTE, EMBARCADERO, ENTIDAD_COMPETENTE, ESTADO_ACTIVO, ESTRUCTURA_AYUDA, SEVERIDAD, TIPO_REGISTRO_INFRA,
  componentesPara, opciones,
  type AspectoAfectado, type ComponenteInfraestructura, type Embarcaderos, type EntidadCompetente, type EstadoActivo,
  type Severidad, type TipoRegistroInfraestructura,
} from '../../types/enums'
import { ahoraLocal, instanteSiCambio, localAInstant } from '../../utils/formato'

/**
 * CrearRegistroInfraestructuraRequest (numeral 4.4). Exactamente uno de embarcadero o ayuda; el
 * componente debe corresponder al tipo de activo (y a una ayuda luminosa si es de luz).
 */
export function FormularioRegistroInfra({ tipoFijo, embarcaderoFijo, ayudaFija, onGuardado, compacto }: {
  tipoFijo?: TipoRegistroInfraestructura
  embarcaderoFijo?: Embarcaderos
  ayudaFija?: string
  onGuardado?: (r: RegistroInfraResponse) => void
  compacto?: boolean
}) {
  const queryClient = useQueryClient()
  const { avisar } = useAvisos()
  const ayudas = useAyudas()
  const [ambito, setAmbito] = useState<'EMBARCADERO' | 'AYUDA'>(ayudaFija ? 'AYUDA' : 'EMBARCADERO')
  const [embarcadero, setEmbarcadero] = useState<Embarcaderos | ''>(embarcaderoFijo ?? '')
  const [ayudaId, setAyudaId] = useState(ayudaFija ?? '')
  const [tipo, setTipo] = useState<TipoRegistroInfraestructura | ''>(tipoFijo ?? '')
  const [componente, setComponente] = useState<ComponenteInfraestructura | ''>('')
  const [estado, setEstado] = useState<EstadoActivo | ''>('')
  const [severidad, setSeveridad] = useState<Severidad | ''>('')
  const [aspectos, setAspectos] = useState<AspectoAfectado[]>([])
  const [descripcion, setDescripcion] = useState('')
  const [acciones, setAcciones] = useState('')
  const [externa, setExterna] = useState(false)
  const [entidad, setEntidad] = useState<EntidadCompetente | ''>('')
  const [inicial] = useState(ahoraLocal)
  const [fechaHora, setFechaHora] = useState(inicial)
  const [evidencias, setEvidencias] = useState<Soporte[]>([])
  const [intentado, setIntentado] = useState(false)
  const [version, setVersion] = useState(0) // reinicia la zona de carga tras guardar

  const ayuda = ayudas.data?.find((a) => a.id === ayudaId)
  const componentes = componentesPara(ambito, ambito === 'AYUDA' ? ayuda?.luminosa ?? true : true)

  const errores: Record<string, string> = {}
  if (ambito === 'EMBARCADERO' && !embarcadero) errores.objetivo = 'Seleccione el embarcadero'
  if (ambito === 'AYUDA' && !ayudaId) errores.objetivo = 'Seleccione la ayuda a la navegación'
  if (!tipo) errores.tipo = 'Seleccione el tipo de registro'
  if (!componente) errores.componente = 'Seleccione el componente'
  else if (!componentes.includes(componente)) errores.componente = 'Este componente no aplica al activo seleccionado'
  if (!estado) errores.estado = 'Seleccione la condición observada'
  if (!severidad) errores.severidad = 'Seleccione la severidad'
  if (!descripcion.trim()) errores.descripcion = 'Describa lo observado'
  // TipoRegistroInfraestructura.esRutinario(): solo revisión y mantenimiento pueden ir sin aspectos
  const rutinario = tipo === 'REVISION' || tipo === 'MANTENIMIENTO'
  if (tipo && !rutinario && !aspectos.length) errores.aspectos = 'Indique a qué afecta: operación, navegación, accesibilidad o seguridad'
  if (externa && !entidad) errores.entidad = 'Indique la entidad que debe intervenir'
  if (fechaHora && new Date(localAInstant(fechaHora)) > new Date()) errores.fecha = 'No puede ser futura'
  const e = (k: string) => (intentado ? errores[k] : undefined)

  const m = useMutation({
    mutationFn: () => infraestructuraService.registrar({
      fechaHora: instanteSiCambio(fechaHora, inicial),
      embarcadero: ambito === 'EMBARCADERO' ? (embarcadero as Embarcaderos) : null,
      ayudaId: ambito === 'AYUDA' ? ayudaId : null,
      tipo: tipo as TipoRegistroInfraestructura,
      componente: componente as ComponenteInfraestructura,
      estadoObservado: estado as EstadoActivo,
      severidad: severidad as Severidad,
      aspectosAfectados: aspectos,
      descripcion: descripcion.trim(),
      accionesRealizadas: acciones.trim() || null,
      requiereIntervencionExterna: externa,
      entidadCompetente: externa ? (entidad as EntidadCompetente) : null,
      evidenciaIds: evidencias.map((x) => x.id),
    }),
    onSuccess: (r) => {
      queryClient.invalidateQueries({ queryKey: ['infraestructura'] })
      avisar(r.pendienteDeComunicar ? 'Registro guardado. Queda pendiente comunicarlo al supervisor del contrato.' : 'Registro guardado.')
      setComponente(''); setEstado(''); setSeveridad(''); setAspectos([]); setDescripcion(''); setAcciones(''); setExterna(false); setEntidad(''); setIntentado(false); setEvidencias([]); setVersion((v) => v + 1)
      onGuardado?.(r)
    },
  })

  const columnas = compacto ? '' : 'sm:grid-cols-2'
  return (
    <div className="flex flex-col gap-4">
      {!embarcaderoFijo && !ayudaFija && (
        <Segmentado etiqueta="Tipo de activo" valor={ambito} onCambio={(v) => { setAmbito(v); setComponente('') }}
          opciones={[{ valor: 'EMBARCADERO', etiqueta: 'Embarcadero' }, { valor: 'AYUDA', etiqueta: 'Ayuda a la navegación' }]} />
      )}
      <div className={`grid gap-4 ${columnas}`}>
        {!embarcaderoFijo && !ayudaFija && (ambito === 'EMBARCADERO' ? (
          <Campo etiqueta="Embarcadero" obligatorio error={e('objetivo')}>
            {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(EMBARCADERO)} value={embarcadero} onChange={(ev) => setEmbarcadero(ev.target.value as Embarcaderos)} />}
          </Campo>
        ) : (
          <Campo etiqueta="Ayuda a la navegación" obligatorio error={e('objetivo')}>
            {(p) => (
              <Selector {...p} vacio={ayudas.isLoading ? 'Cargando…' : 'Seleccione…'} value={ayudaId} onChange={(ev) => { setAyudaId(ev.target.value); setComponente('') }}
                opciones={(ayudas.data ?? []).map((a) => ({ valor: a.id, etiqueta: `${a.codigo} · ${ESTRUCTURA_AYUDA[a.estructura]}${a.luminosa ? ' luminosa' : ''}` }))} />
            )}
          </Campo>
        ))}
        {!tipoFijo && (
          <Campo etiqueta="Tipo de registro" obligatorio error={e('tipo')}>
            {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(TIPO_REGISTRO_INFRA)} value={tipo} onChange={(ev) => setTipo(ev.target.value as TipoRegistroInfraestructura)} />}
          </Campo>
        )}
        <Campo etiqueta="Componente" obligatorio error={e('componente')}>
          {(p) => <Selector {...p} vacio="Seleccione…" opciones={componentes.map((c) => ({ valor: c, etiqueta: COMPONENTE[c].etiqueta }))} value={componente} onChange={(ev) => setComponente(ev.target.value as ComponenteInfraestructura)} />}
        </Campo>
        <Campo etiqueta="Fecha y hora" error={e('fecha')}>
          {(p) => <Entrada {...p} type="datetime-local" value={fechaHora} max={ahoraLocal()} onChange={(ev) => setFechaHora(ev.target.value)} />}
        </Campo>
      </div>
      <div className="flex flex-col gap-1.5">
        <span className="etiqueta">Condición observada <span className="text-error">*</span></span>
        <Chips etiqueta="Condición observada" opciones={opciones(ESTADO_ACTIVO)} valor={estado} onCambio={setEstado} />
        {e('estado') && <p role="alert" className="m-0 text-[12px] text-error">{e('estado')}</p>}
      </div>
      <div className="flex flex-col gap-1.5">
        <span className="etiqueta">Severidad <span className="text-error">*</span></span>
        <Chips etiqueta="Severidad" opciones={opciones(SEVERIDAD)} valor={severidad} onCambio={setSeveridad} />
        {e('severidad') && <p role="alert" className="m-0 text-[12px] text-error">{e('severidad')}</p>}
      </div>
      <div className="flex flex-col gap-1.5">
        <span className="etiqueta">Aspectos afectados {tipo && !rutinario ? <span className="text-error" aria-hidden>*</span> : <span className="text-texto-3 font-normal">(opcional)</span>}</span>
        <Chips multiple etiqueta="Aspectos afectados" opciones={opciones(ASPECTO_AFECTADO)} valor={aspectos}
          onCambio={(v) => setAspectos((a) => (a.includes(v) ? a.filter((x) => x !== v) : [...a, v]))} />
        {e('aspectos') && <p role="alert" className="m-0 text-[12px] text-error">{e('aspectos')}</p>}
      </div>
      <Campo etiqueta="Descripción" obligatorio error={e('descripcion')}>
        {(p) => <AreaTexto {...p} maxLength={2000} value={descripcion} onChange={(ev) => setDescripcion(ev.target.value)} placeholder="Describe lo observado…" />}
      </Campo>
      <Campo etiqueta="Acciones realizadas" opcional>
        {(p) => <AreaTexto {...p} maxLength={2000} value={acciones} onChange={(ev) => setAcciones(ev.target.value)} />}
      </Campo>
      <Interruptor activo={externa} onCambio={setExterna} etiqueta="Requiere intervención de otra entidad" descripcion="Las intervenciones que no corresponden al operador se registran y se comunican al supervisor del contrato (numeral 4.4)" />
      {externa && (
        <Campo etiqueta="Entidad competente" obligatorio error={e('entidad')}>
          {(p) => <Selector {...p} vacio="Seleccione…" opciones={opciones(ENTIDAD_COMPETENTE).filter((o) => o.valor !== 'NO_APLICA')} value={entidad} onChange={(ev) => setEntidad(ev.target.value as EntidadCompetente)} />}
        </Campo>
      )}
      <div className="flex flex-col gap-1.5">
        <span className="etiqueta">Evidencias</span>
        <CargaArchivos key={version} carpeta="infraestructura" onCambio={setEvidencias} titulo="Adjunta fotografías o documentos" />
      </div>
      {(estado === 'FUERA_DE_SERVICIO' || severidad === 'ALTA' || severidad === 'CRITICA') && (
        <Alerta tono="dorado">Si la condición afecta la navegación segura, comuníquela al supervisor del contrato (numeral 4.4).</Alerta>
      )}
      <ErrorFormulario error={m.error} />
      <Boton variante="primario" className="w-full sm:w-auto sm:self-end" icono={<Check className="size-4" />} cargando={m.isPending}
        onClick={() => { setIntentado(true); if (!Object.keys(errores).length) m.mutate() }}>
        Guardar registro
      </Boton>
    </div>
  )
}
