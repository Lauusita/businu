import { useState } from 'react'
import { Download } from 'lucide-react'
import { ErrorFormulario } from '../../components/feedback/Estados'
import { useAvisos } from '../../components/feedback/avisos-context'
import { Boton } from '../../components/ui/Boton'
import { Campo, Entrada } from '../../components/ui/Campo'
import { Modal } from '../../components/ui/Modal'
import { Segmentado } from '../../components/ui/Selecciones'
import { usePermiso } from '../../hooks/usePermiso'
import { informesService } from '../../services/modules/informes.service'
import { CONJUNTO_EXPORTACION, type ConjuntoExportacion } from '../../types/enums'
import { guardarArchivo } from '../../utils/descargas'
import { hoyISO, inicioMes } from '../../utils/formato'

/**
 * Botón «Exportar» de las plantillas: GET /exportaciones/{conjunto} (Excel o CSV, solo SUPERVISOR).
 * Sin fechas, el backend exporta el mes en curso.
 */
export function BotonExportar({ conjunto, etiqueta = 'Exportar' }: { conjunto: ConjuntoExportacion; etiqueta?: string }) {
  const permitido = usePermiso('exportar')
  const [abierto, setAbierto] = useState(false)
  if (!permitido) return null
  return (
    <>
      <Boton variante="secundario" icono={<Download className="size-4" />} onClick={() => setAbierto(true)}>{etiqueta}</Boton>
      <DialogoExportar abierto={abierto} onCerrar={() => setAbierto(false)} conjunto={conjunto} />
    </>
  )
}

export function DialogoExportar({ abierto, onCerrar, conjunto }: { abierto: boolean; onCerrar: () => void; conjunto: ConjuntoExportacion }) {
  const { avisar } = useAvisos()
  const [desde, setDesde] = useState(inicioMes())
  const [hasta, setHasta] = useState(hoyISO())
  const [formato, setFormato] = useState<'xlsx' | 'csv'>('xlsx')
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<unknown>(null)

  const descargar = async () => {
    setCargando(true)
    setError(null)
    try {
      const r = await informesService.exportar(conjunto, { desde, hasta, formato })
      guardarArchivo(r.blob, r.nombre ?? `businu-${conjunto.toLowerCase()}-${desde}_${hasta}.${formato}`)
      avisar('Archivo descargado.')
      onCerrar()
    } catch (e) {
      setError(e)
    } finally {
      setCargando(false)
    }
  }

  return (
    <Modal
      abierto={abierto}
      onCerrar={onCerrar}
      titulo={`Exportar · ${CONJUNTO_EXPORTACION[conjunto]}`}
      subtitulo="Numeral 4.8: los registros del periodo en Excel o CSV."
      ancho="sm"
      pie={
        <>
          <Boton variante="fantasma" onClick={onCerrar}>Cancelar</Boton>
          <Boton variante="institucional" icono={<Download className="size-4" />} cargando={cargando} onClick={descargar} disabled={!desde || !hasta || desde > hasta}>
            Descargar
          </Boton>
        </>
      }
    >
      <div className="grid grid-cols-2 gap-3">
        <Campo etiqueta="Desde" obligatorio>{(p) => <Entrada {...p} type="date" value={desde} max={hasta} onChange={(e) => setDesde(e.target.value)} />}</Campo>
        <Campo etiqueta="Hasta" obligatorio>{(p) => <Entrada {...p} type="date" value={hasta} min={desde} onChange={(e) => setHasta(e.target.value)} />}</Campo>
      </div>
      <div className="flex items-center gap-3">
        <span className="etiqueta">Formato</span>
        <Segmentado etiqueta="Formato" valor={formato} onCambio={setFormato} opciones={[{ valor: 'xlsx', etiqueta: 'Excel' }, { valor: 'csv', etiqueta: 'CSV' }]} />
      </div>
      <ErrorFormulario error={error} />
    </Modal>
  )
}
