/**
 * Contratos de la API, transcritos de los DTO del backend (com.lau.api.dto).
 * Instant → string ISO-8601 (UTC). LocalDate → 'yyyy-MM-dd'. YearMonth → 'yyyy-MM'. BigDecimal → number.
 * Los campos marcados como opcionales son los que el backend omite (@JsonInclude NON_NULL) o acepta vacíos.
 */
import type {
  AgrupacionSerie, AspectoAfectado, CausaSuspension, ColorLuz, ComponenteInfraestructura, CondicionClimatica,
  ConjuntoExportacion, Embarcaciones, Embarcaderos, EntidadCompetente, EstadoActivo, EstadoEmbarcacion, EstadoEnvio,
  EstadoInforme, EstadoNave, EstadoOperacion, EstadoPqrs, EstadoSeguimiento, EstructuraAyuda, FranjaHoraria,
  FuncionMarca, GrupoEtario, MedioComunicacion, NivelRio, RepuestoInsumo, Rol, Severidad, SistemaIntervenido, Sexo,
  TipoCombustible, TipoDocumentoPersonal, TipoIdentificacion, TipoMantenimiento, TipoPqrs, TipoRecomendacion,
  TipoReferenciaFalla, TipoRegistroInfraestructura, TipoVinculacion,
} from './enums'

export type UUID = string
export type Instant = string
export type FechaISO = string

/** Spring Data PagedModel. */
export interface Pagina<T> {
  content: T[]
  page: { size: number; number: number; totalElements: number; totalPages: number }
}

export interface ParametrosPagina {
  page?: number
  size?: number
  sort?: string
}

// ---------- Autenticación ----------
export interface LoginRequest { numeroIdentificacion: string; contrasena: string }
export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  personalId: UUID
  nombre: string
  rol: Rol
}

// ---------- Personal ----------
export interface PersonalResponse {
  id: UUID
  tipoIdentificacion: TipoIdentificacion
  numeroIdentificacion: string
  nombres: string
  apellidos: string
  email: string | null
  rol: Rol
}
export interface PersonalDTO {
  tipoIdentificacion: TipoIdentificacion
  numeroIdentificacion: string
  nombres: string
  apellidos: string
  email: string
  rol: Rol
  tipoVinculacion: TipoVinculacion
  password?: string
}

export interface DocumentoPersonalResponse {
  id: UUID
  tipo: TipoDocumentoPersonal
  descripcionTipo: string
  periodo?: string
  vigente: boolean
  nombreArchivo: string
  tamanoBytes: number
  sha256: string
  observaciones?: string
  cargadoEn: Instant
  registradoPor: string
}
export interface DocumentacionPersonaResponse {
  personalId: UUID
  nombre: string
  numeroIdentificacion: string
  rol: Rol
  faltantes: TipoDocumentoPersonal[]
  planillasPila: string[]
  documentos: DocumentoPersonalResponse[]
}

// ---------- Comunes ----------
export interface NovedadMecanicaDTO {
  /** En peticiones: nombre del enum (CondicionMecanica). En respuestas: su descripción. */
  condicion: string
  severidad: Severidad
  detalle?: string | null
}
export interface Soporte { id: UUID; nombreOriginal: string; tamanoBytes: number; sha256: string }

// ---------- Embarcaciones y motores ----------
export interface MotorDTO { id: UUID; marca: string; modelo: string; serial: string; potenciaHp: number }
export interface EmbarcacionResponse {
  id: UUID
  nombre: Embarcaciones
  patente: string
  capacidadPasajeros: number
  estado: EstadoEmbarcacion
  motores: MotorDTO[]
}
export interface MotorResponse {
  id: UUID
  embarcacion: { id: UUID; nombre: Embarcaciones; patente: string }
  marca: string
  modelo: string
  serial: string
  potenciaHp: number
  horometroActual: number | null
  intervaloServicioHoras: number
}

// ---------- Mantenimiento ----------
export interface MantenimientoResponse {
  id: UUID
  embarcacionId: UUID
  embarcacion: Embarcaciones
  motor?: { id: UUID; serial: string; modelo: string }
  tipo: TipoMantenimiento
  sistema: string
  fechaIntervencion: Instant
  descripcionActividades: string
  ejecutadoPor?: string
  responsable?: string
  registradoPor?: string
  sinRepuestos: boolean
  repuestos: { item: string; descripcion?: string; cantidad: number; unidad?: string; costo?: number }[]
  sinNovedades: boolean
  novedades: NovedadMecanicaDTO[]
  recomendaciones: { tipo: TipoRecomendacion; descripcion: string }[]
  proximaIntervencion?: Instant
  condicionProximaIntervencion?: string
  costoManoObra?: number
  costoRepuestos?: number
  costoTotal?: number
  soportes: Soporte[]
}
export interface CrearMantenimientoRequest {
  embarcacionId: UUID
  motorId?: UUID | null
  tipo: TipoMantenimiento
  sistema: SistemaIntervenido
  fechaIntervencion: Instant
  descripcionActividades: string
  ejecutadoPor?: string | null
  responsableId: UUID
  costoManoObra?: number | null
  repuestos?: { item: RepuestoInsumo; descripcion?: string | null; cantidad: number; unidad?: string | null; costo?: number | null }[]
  novedades?: NovedadMecanicaDTO[]
  recomendaciones?: { tipo: TipoRecomendacion; descripcion: string }[]
  proximaIntervencion?: Instant | null
  condicionProximaIntervencion?: string | null
  soporteIds?: UUID[]
}
export interface HojaDeVidaResponse {
  embarcacion: EmbarcacionResponse
  totalIntervenciones: number
  preventivos: number
  correctivos: number
  ultimaIntervencion?: Instant
  proximaIntervencion?: Instant
  costoTotalMantenimiento: number
  intervenciones: MantenimientoResponse[]
}

// ---------- Suspensiones ----------
export interface SuspensionResponse {
  id: UUID
  embarcacionId: UUID
  embarcacion: Embarcaciones
  causa: CausaSuspension
  causaDescripcion: string
  detalle?: string
  autoridad?: string
  fechaInicio: Instant
  fechaFin?: Instant
  vigente: boolean
  observacionLevantamiento?: string
  registradoPor?: string
  levantadaPor?: string
}

// ---------- Operación (bitácora) ----------
export interface VerificacionPrevia {
  aceiteMotor: boolean
  combustible: boolean
  sistemaDireccion: boolean
  bombaAchique: boolean
  lucesNavegacion: boolean
  chalecosSalvavidas: boolean
  radioComunicacion: boolean
}
export interface LecturaHorometro { motorId: UUID; lectura: number }
export interface CrearOperacionRequest {
  embarcacionId: UUID
  embarcaderoOrigen: Embarcaderos
  embarcaderoDestino: Embarcaderos
  fechaHoraInicio?: Instant | null
  estadoNave: EstadoNave
  verificacion: VerificacionPrevia
  justificacionVerificacion?: string | null
  condicionClimatica: CondicionClimatica
  condicionesMecanicas?: NovedadMecanicaDTO[]
  nivelRio: NivelRio
  tripulacionIds: UUID[]
  lecturasHorometro?: LecturaHorometro[]
}
export interface ActualizarOperacionRequest {
  condicionClimatica?: CondicionClimatica
  nivelRio?: NivelRio
  condicionesMecanicas?: NovedadMecanicaDTO[]
  novedades?: string
  observaciones?: string
}
export interface ParadaRequest { embarcadero: Embarcaderos; horaLlegada: Instant; horaSalida: Instant }
export interface FinalizarOperacionRequest { fechaHoraFin: Instant; lecturasHorometro: LecturaHorometro[] }
export interface SuspenderOperacionRequest {
  causa: CausaSuspension
  detalle?: string | null
  autoridad?: string | null
  momento?: Instant | null
  lecturasHorometro: LecturaHorometro[]
}
export interface OperacionResumen {
  id: UUID
  embarcacion: Embarcaciones
  embarcaderoOrigen: string
  embarcaderoDestino: string
  fechaHoraInicio: Instant
  fechaHoraFin: Instant | null
  estado: EstadoOperacion
  totalUsuariosTransportados: number
}
export interface OperacionResponse {
  id: UUID
  embarcacion: { id: UUID; nombre: Embarcaciones; patente: string }
  embarcaderoOrigen: string
  embarcaderoDestino: string
  fechaHoraInicio: Instant
  fechaHoraFin?: Instant
  estado: EstadoOperacion
  estadoNave: EstadoNave
  verificacion: VerificacionPrevia
  justificacionVerificacion?: string
  condicionClimatica: CondicionClimatica
  nivelRio: NivelRio
  tripulacion: { id: UUID; nombre: string; rol: Rol }[]
  paradas: { embarcadero: string; horaLlegada: Instant; horaSalida: Instant }[]
  sinNovedadesMecanicas: boolean
  condicionesMecanicas: NovedadMecanicaDTO[]
  novedades?: string
  observaciones?: string
  motivoSuspension?: string
  horometroFinal?: number
  totalUsuariosTransportados: number
  registradoPor?: string
  cargueCombustible: boolean
  suspension?: { causa: CausaSuspension; causaDescripcion: string; detalle?: string; autoridad?: string }
  horometros?: { motorId: UUID; serial: string; lecturaInicial?: number; lecturaFinal?: number; horas?: number }[]
  horasMotor?: number
  horasNavegacion?: number
  cargues?: { abastecimientoId: UUID; fechaHora: Instant; galones: number; tanqueLleno: boolean }[]
}
export interface FiltroOperaciones {
  embarcacion?: Embarcaciones
  origen?: Embarcaderos
  destino?: Embarcaderos
  estado?: EstadoOperacion
  fecha?: FechaISO
}

// ---------- Usuarios (pasajeros) ----------
export interface UsuarioRequest {
  fechaHora?: Instant | null
  operacionId: UUID
  embarcaderoAbordaje: Embarcaderos
  usuariosTransportados: number
  porSexo: Partial<Record<Sexo, number>>
  porGrupoEtario: Partial<Record<GrupoEtario, number>>
  porDestino: Partial<Record<Embarcaderos, number>>
}
export interface UsuarioResponse {
  id: UUID
  fechaHora: Instant
  embarcacion?: { id: UUID; nombre: Embarcaciones; patente: string }
  embarcaderoAbordaje?: { codigo: Embarcaderos; nombre: string }
  porDestino?: Partial<Record<Embarcaderos, number>>
  usuariosTransportados: number
  porSexo?: Partial<Record<Sexo, number>>
  porGrupoEtario?: Partial<Record<GrupoEtario, number>>
  registradoPor?: string
  creadoEn?: Instant
}
export interface FiltroUsuarios {
  embarcacion?: Embarcaciones
  puertoAbordaje?: Embarcaderos
  puertoDestino?: Embarcaderos
  fecha?: FechaISO
}
export interface ConsolidadoDemanda {
  desde: FechaISO
  hasta: FechaISO
  embarcacion?: Embarcaciones
  origen?: Embarcaderos
  agrupacion: AgrupacionSerie
  totalUsuarios: number
  viajes: number
  promedioPorViaje?: number
  suspendidos: { viajes: number; usuarios: number }
  porSexo: Record<Sexo, number>
  porGrupoEtario: Record<GrupoEtario, number>
  porOrigen: Record<Embarcaderos, number>
  porDestino: Record<Embarcaderos, number>
  origenDestino: { origen: Embarcaderos; destino: Embarcaderos; usuarios: number }[]
  porFranjaHoraria: { franja: FranjaHoraria; desde: string; hasta: string; usuarios: number }[]
  porHora: { hora: number; usuarios: number }[]
  serie: { fecha: FechaISO; usuarios: number; viajes: number }[]
}

// ---------- Combustible ----------
export interface AbastecimientoRequest {
  fechaHora?: Instant | null
  embarcacionId: UUID
  tipoCombustible: TipoCombustible
  galones: number
  valorTotal: number
  tanqueLleno: boolean
  soporteId: UUID
  observaciones?: string | null
}
export interface AbastecimientoResponse {
  id: UUID
  fechaHora: Instant
  embarcacionId: UUID
  embarcacion: Embarcaciones
  tipoCombustible: TipoCombustible
  galones: number
  valorTotal: number
  precioPorGalon?: number
  tanqueLleno: boolean
  soporte?: Soporte
  observaciones?: string
  operacionId?: UUID
}
export interface ConsumoPeriodo {
  abastecimientoInicialId: UUID
  abastecimientoFinalId: UUID
  desde: Instant
  hasta: Instant
  galonesConsumidos: number
  viajes: number
  horasNavegacion?: number
  horasMotor?: number
  horometroCompleto: boolean
  galonesPorViaje?: number
  galonesPorHoraNavegacion?: number
  galonesPorHoraMotor?: number
}

// ---------- Infraestructura ----------
export interface AyudaResumen { id: UUID; codigo: string; estructura: EstructuraAyuda; funcion: FuncionMarca; luminosa: boolean }
export interface CrearAyudaRequest {
  codigo: string
  estructura: EstructuraAyuda
  funcion: FuncionMarca
  luminosa: boolean
  colorLuz?: ColorLuz | null
  ritmoLuz?: string | null
  descripcion?: string | null
  tramoDesde: Embarcaderos
  tramoHasta: Embarcaderos
  referenciaUbicacion: string
  latitud?: number | null
  longitud?: number | null
}
export interface CrearRegistroInfraRequest {
  fechaHora?: Instant | null
  embarcadero?: Embarcaderos | null
  ayudaId?: UUID | null
  tipo: TipoRegistroInfraestructura
  componente: ComponenteInfraestructura
  estadoObservado: EstadoActivo
  severidad: Severidad
  aspectosAfectados?: AspectoAfectado[]
  descripcion: string
  accionesRealizadas?: string | null
  requiereIntervencionExterna: boolean
  entidadCompetente?: EntidadCompetente | null
  evidenciaIds?: UUID[]
}
export interface RegistroInfraResponse {
  id: UUID
  fechaHora: Instant
  embarcadero?: Embarcaderos
  ayuda?: AyudaResumen
  tipo: TipoRegistroInfraestructura
  componente: ComponenteInfraestructura
  estadoObservado: EstadoActivo
  severidad: Severidad
  aspectosAfectados?: AspectoAfectado[]
  descripcion: string
  accionesRealizadas?: string
  requiereIntervencionExterna: boolean
  entidadCompetente?: EntidadCompetente
  estado: EstadoSeguimiento
  pendienteDeComunicar: boolean
  horasSinComunicar?: number
  comunicacion?: { fecha: Instant; medio: MedioComunicacion; referencia?: string; comunicadoPor?: string }
  cierre?: { fecha: Instant; observacion: string; cerradoPor?: string }
  registradoPor?: string
  evidencias?: Soporte[]
}
export interface EstadoInfraestructura {
  tipo: 'EMBARCADERO' | 'AYUDA'
  identificador: string
  estadoActual: EstadoActivo
  ultimaRevision: Instant | null
  registrosAbiertos: number
}
export interface FiltroRegistrosInfra {
  estado?: EstadoSeguimiento
  embarcadero?: Embarcaderos
  ayudaId?: UUID
  tipo?: TipoRegistroInfraestructura
  desde?: Instant
  hasta?: Instant
}

// ---------- Comunicaciones ----------
export interface ComunicacionRequest {
  referenciaTipo: TipoReferenciaFalla
  referenciaId: UUID
  destinatario?: string | null
  asunto: string
  mensaje: string
}
export interface ComunicacionResponse {
  id: UUID
  referenciaTipo: TipoReferenciaFalla
  referenciaId: UUID
  destinatario: string
  asunto: string
  estado: EstadoEnvio
  fechaRegistro: Instant
  fechaEnvio?: Instant
  messageId?: string
  error?: string
  intentos: number
  registradoPor?: string
}

// ---------- PQRS ----------
export interface PqrsResponse {
  id: UUID
  radicado: string
  fechaRadicacion: Instant
  tipo: TipoPqrs
  descripcion: string
  embarcadero?: Embarcaderos
  fechaHecho?: FechaISO
  nombre?: string
  correo?: string
  telefono?: string
  autorizaDatos: boolean
  estado: EstadoPqrs
  respuesta?: string
  entidadTraslado?: string
  observacionInterna?: string
  fechaRespuesta?: Instant
  gestionadoPor?: string
}
export interface GestionarPqrsRequest {
  estado: EstadoPqrs
  respuesta?: string | null
  entidadTraslado?: string | null
  observacionInterna?: string | null
}

// ---------- Informe mensual ----------
export interface Tabla { nombre: string; columnas: string[]; filas: unknown[][] }
export interface InformeMensualDatos {
  periodo: string
  desde: FechaISO
  hasta: FechaISO
  generadoEn: Instant
  viajes: {
    total: number; finalizados: number; suspendidos: number; enCurso: number; horasNavegacion: number
    porEmbarcacion: Record<string, number>
    porRecorrido: { origen: string; destino: string; viajes: number; pasajeros: number }[]
  }
  demanda: ConsolidadoDemanda
  combustible: { abastecimientos: number; galones: number; valorTotal: number; galonesPorEmbarcacion: Record<string, number> }
  embarcaciones: { nombre: string; estado: string; suspendida: boolean; viajes: number; novedadesMecanicas: number; mantenimientos: number }[]
  alertasMantenimiento: string[]
  infraestructura: { registrosDelMes: number; porTipo: Record<string, number>; abiertos: number; pendientesDeComunicar: number }
  seguridadSocial: { personalActivo: number; conPlanilla: number; sinPlanilla: string[] }
  pqrs: { total: number; porTipo: Record<string, number>; porEstado: Record<string, number> }
  tablas: Record<string, Tabla>
}
export interface InformeMensualResponse {
  periodo: string
  estado: EstadoInforme
  resumenActividades?: string
  conclusiones?: string
  recomendaciones?: string
  otrosDocumentos?: string
  fechaCierre?: Instant
  cerradoPor?: string
  fechaEntrega?: Instant
  entregadoPor?: string
  huellaDatos?: string
  datos?: InformeMensualDatos
}
export interface RedactarInformeRequest {
  resumenActividades?: string | null
  conclusiones?: string | null
  recomendaciones?: string | null
  otrosDocumentos?: string | null
}

// ---------- Auditoría ----------
export interface RevisionResponse {
  revision: number
  fecha: Instant
  accion: 'CREACION' | 'MODIFICACION' | 'ELIMINACION'
  autorId: UUID | null
  autor: string
  camposModificados: string[]
}

export type { ConjuntoExportacion }
