/**
 * Enumeraciones del backend (com.lau.api.dominio.enums). El backend las serializa por su nombre;
 * las etiquetas son las descripciones definidas en cada enum de Java.
 */

const opciones = <T extends string>(etiquetas: Record<T, string>) =>
  (Object.keys(etiquetas) as T[]).map((valor) => ({ valor, etiqueta: etiquetas[valor] }))

// ---------- Personal ----------
export type Rol = 'SUPERVISOR' | 'GUIA_TURISTICA' | 'OPERADOR_EMBARCACION' | 'TAQUILLERO'
export const ROL: Record<Rol, string> = {
  SUPERVISOR: 'Supervisor operativo',
  OPERADOR_EMBARCACION: 'Operador de embarcación',
  GUIA_TURISTICA: 'Guía de turismo a bordo',
  TAQUILLERO: 'Operador de taquilla',
}
export const ROLES = opciones(ROL)
/** Rol.usaSistema(): los guías no tienen acceso. */
export const usaSistema = (rol: Rol) => rol !== 'GUIA_TURISTICA'

export type TipoIdentificacion = 'CC' | 'CE' | 'PPT' | 'PASAPORTE'
export const TIPO_IDENTIFICACION: Record<TipoIdentificacion, string> = {
  CC: 'Cédula de ciudadanía',
  CE: 'Cédula de extranjería',
  PPT: 'Permiso por protección temporal',
  PASAPORTE: 'Pasaporte',
}

export type TipoVinculacion = 'LABORAL' | 'PRESTACION_SERVICIOS'
export const TIPO_VINCULACION: Record<TipoVinculacion, string> = {
  LABORAL: 'Laboral',
  PRESTACION_SERVICIOS: 'Prestación de servicios',
}

export type TipoDocumentoPersonal =
  | 'CEDULA' | 'RUT' | 'HOJA_VIDA_FUNCION_PUBLICA' | 'CERTIFICADO_ACADEMICO' | 'CERTIFICADO_EXPERIENCIA'
  | 'LICENCIA_TRIPULANTE' | 'CERTIFICADO_COMPETENCIA' | 'AFILIACION_SEGURIDAD_SOCIAL' | 'PLANILLA_PILA' | 'OTRO'
export const TIPO_DOCUMENTO: Record<TipoDocumentoPersonal, string> = {
  CEDULA: 'Documento de identidad',
  RUT: 'RUT',
  HOJA_VIDA_FUNCION_PUBLICA: 'Hoja de vida (formato de función pública)',
  CERTIFICADO_ACADEMICO: 'Certificado de formación académica',
  CERTIFICADO_EXPERIENCIA: 'Certificados de experiencia laboral o relacionada',
  LICENCIA_TRIPULANTE: 'Licencia o permiso de tripulante',
  CERTIFICADO_COMPETENCIA: 'Certificaciones o competencias laborales del cargo',
  AFILIACION_SEGURIDAD_SOCIAL: 'Afiliación al Sistema de Seguridad Social Integral',
  PLANILLA_PILA: 'Planilla de pago de seguridad social (PILA)',
  OTRO: 'Otro documento de idoneidad',
}

// ---------- Operación ----------
export type Embarcaciones = 'BETANCI' | 'YAPEL'
export const EMBARCACION: Record<Embarcaciones, string> = { BETANCI: 'Betancí', YAPEL: 'Yapel' }

export type Embarcaderos = 'RANCHO_GRANDE' | 'CENTRO_CALLE_22' | 'CENTRO_VERDE'
export const EMBARCADERO: Record<Embarcaderos, string> = {
  RANCHO_GRANDE: 'Rancho Grande',
  CENTRO_CALLE_22: 'Centro – Calle 22',
  CENTRO_VERDE: 'Centro Verde',
}
/** Orden del recorrido sobre el río. */
export const EMBARCADEROS: Embarcaderos[] = ['RANCHO_GRANDE', 'CENTRO_CALLE_22', 'CENTRO_VERDE']

export type EstadoEmbarcacion = 'OPERATIVA' | 'EN_MANTENIMIENTO' | 'FUERA_DE_SERVICIO'
export const ESTADO_EMBARCACION: Record<EstadoEmbarcacion, string> = {
  OPERATIVA: 'Operativa',
  EN_MANTENIMIENTO: 'En mantenimiento',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
}

export type EstadoOperacion = 'EN_CURSO' | 'FINALIZADA' | 'SUSPENDIDA'
export const ESTADO_OPERACION: Record<EstadoOperacion, string> = {
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  SUSPENDIDA: 'Suspendida',
}

export type EstadoNave = 'BUENO' | 'REGULAR' | 'MALO'
export const ESTADO_NAVE: Record<EstadoNave, string> = { BUENO: 'Bueno', REGULAR: 'Regular', MALO: 'Malo' }

export type NivelRio = 'BAJO' | 'NORMAL' | 'ALTO'
export const NIVEL_RIO: Record<NivelRio, string> = { BAJO: 'Bajo', NORMAL: 'Normal', ALTO: 'Alto' }

export type CondicionClimatica =
  | 'DESPEJADO' | 'NUBLADO' | 'LLUVIA_LEVE' | 'LLUVIA_FUERTE' | 'TORMENTA_ELECTRICA' | 'VIENTO_FUERTE' | 'VENDAVAL'
export const CONDICION_CLIMATICA: Record<CondicionClimatica, string> = {
  DESPEJADO: 'Despejado',
  NUBLADO: 'Nublado',
  LLUVIA_LEVE: 'Lluvia leve',
  LLUVIA_FUERTE: 'Lluvia fuerte',
  TORMENTA_ELECTRICA: 'Tormenta eléctrica',
  VIENTO_FUERTE: 'Viento fuerte',
  VENDAVAL: 'Vendaval',
}

export type CondicionMecanica =
  | 'RUIDO_ANORMAL_MOTOR' | 'SOBRECALENTAMIENTO' | 'VIBRACION' | 'PERDIDA_POTENCIA' | 'FALLA_ARRANQUE'
  | 'FUGA_ACEITE' | 'FUGA_COMBUSTIBLE' | 'FALLA_DIRECCION' | 'GOLPE_HELICE' | 'FALLA_ELECTRICA'
  | 'FALLA_LUCES_NAVEGACION' | 'FALLA_BOMBA_ACHIQUE' | 'FALLA_AIRE_ACONDICIONADO' | 'INGRESO_AGUA' | 'OTRA'
export const CONDICION_MECANICA: Record<CondicionMecanica, string> = {
  RUIDO_ANORMAL_MOTOR: 'Ruido anormal del motor',
  SOBRECALENTAMIENTO: 'Sobrecalentamiento',
  VIBRACION: 'Vibración',
  PERDIDA_POTENCIA: 'Pérdida de potencia',
  FALLA_ARRANQUE: 'Falla de arranque',
  FUGA_ACEITE: 'Fuga de aceite',
  FUGA_COMBUSTIBLE: 'Fuga de combustible',
  FALLA_DIRECCION: 'Falla de dirección',
  GOLPE_HELICE: 'Golpe en la hélice',
  FALLA_ELECTRICA: 'Falla eléctrica',
  FALLA_LUCES_NAVEGACION: 'Falla en luces de navegación',
  FALLA_BOMBA_ACHIQUE: 'Falla en bomba de achique',
  FALLA_AIRE_ACONDICIONADO: 'Falla en aire acondicionado',
  INGRESO_AGUA: 'Ingreso de agua',
  OTRA: 'Otra',
}
/** Las respuestas traen la descripción; las peticiones, el nombre. */
export const condicionDesdeDescripcion = (descripcion: string): CondicionMecanica | undefined =>
  (Object.keys(CONDICION_MECANICA) as CondicionMecanica[]).find((k) => CONDICION_MECANICA[k] === descripcion)

export type Severidad = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA'
export const SEVERIDAD: Record<Severidad, string> = { BAJA: 'Baja', MEDIA: 'Media', ALTA: 'Alta', CRITICA: 'Crítica' }

export type CausaSuspension =
  | 'INSTRUCCION_AUTORIDAD' | 'CONDICION_CLIMATICA' | 'NIVEL_RIO' | 'RIESGO_INUNDACION_CRECIENTE'
  | 'FALLA_TECNICA_MECANICA' | 'OTRA'
export const CAUSA_SUSPENSION: Record<CausaSuspension, string> = {
  INSTRUCCION_AUTORIDAD: 'Instrucción o restricción de la autoridad competente',
  CONDICION_CLIMATICA: 'Condiciones climáticas que afectan la navegación segura',
  NIVEL_RIO: 'Variación significativa del nivel del río',
  RIESGO_INUNDACION_CRECIENTE: 'Riesgo por inundación, creciente, avalancha u otro evento informado',
  FALLA_TECNICA_MECANICA: 'Condición técnica, mecánica u operacional',
  OTRA: 'Otra circunstancia que requiere medidas preventivas',
}

// ---------- Usuarios (pasajeros) ----------
export type Sexo = 'MASCULINO' | 'FEMENINO'
export const SEXO: Record<Sexo, string> = { FEMENINO: 'Mujer', MASCULINO: 'Hombre' }

export type GrupoEtario = 'PRIMERA_INFANCIA' | 'INFANCIA' | 'ADOLESCENCIA' | 'JUVENTUD' | 'ADULTEZ' | 'PERSONA_MAYOR'
export const GRUPO_ETARIO: Record<GrupoEtario, string> = {
  PRIMERA_INFANCIA: '0 a 5 años',
  INFANCIA: '6 a 11 años',
  ADOLESCENCIA: '12 a 17 años',
  JUVENTUD: '18 a 28 años',
  ADULTEZ: '29 a 59 años',
  PERSONA_MAYOR: '60 años o más',
}
export const GRUPO_ETARIO_CORTO: Record<GrupoEtario, string> = {
  PRIMERA_INFANCIA: '0–5',
  INFANCIA: '6–11',
  ADOLESCENCIA: '12–17',
  JUVENTUD: '18–28',
  ADULTEZ: '29–59',
  PERSONA_MAYOR: '60+',
}

export type FranjaHoraria = 'MANANA' | 'TARDE' | 'NOCHE'
export const FRANJA: Record<FranjaHoraria, string> = { MANANA: 'Mañana', TARDE: 'Tarde', NOCHE: 'Noche' }

export type AgrupacionSerie = 'DIA' | 'SEMANA' | 'MES'
export const AGRUPACION: Record<AgrupacionSerie, string> = { DIA: 'Día', SEMANA: 'Semana', MES: 'Mes' }

// ---------- Combustible ----------
export type TipoCombustible = 'GASOLINA_CORRIENTE' | 'GASOLINA_EXTRA' | 'ACPM'
export const TIPO_COMBUSTIBLE: Record<TipoCombustible, string> = {
  GASOLINA_CORRIENTE: 'Gasolina corriente',
  GASOLINA_EXTRA: 'Gasolina extra',
  ACPM: 'ACPM (diésel)',
}

// ---------- Mantenimiento ----------
export type TipoMantenimiento = 'CORRECTIVO' | 'PREVENTIVO' | 'PREDICTIVO'
export const TIPO_MANTENIMIENTO: Record<TipoMantenimiento, string> = {
  PREVENTIVO: 'Preventivo',
  CORRECTIVO: 'Correctivo',
  PREDICTIVO: 'Predictivo',
}
export const TIPO_MANTENIMIENTO_AYUDA: Record<TipoMantenimiento, string> = {
  PREVENTIVO: 'Programado según plan y recomendaciones técnicas',
  CORRECTIVO: 'Atiende una falla o novedad identificada',
  PREDICTIVO: 'Anticipa una falla a partir de mediciones y seguimiento',
}

export type TipoRecomendacion = 'CORRECTIVA' | 'PREVENTIVA' | 'PREDICTIVA'
export const TIPO_RECOMENDACION: Record<TipoRecomendacion, string> = {
  PREVENTIVA: 'Preventiva',
  CORRECTIVA: 'Correctiva',
  PREDICTIVA: 'Predictiva',
}

export type SistemaIntervenido = 'MOTOR' | 'CASCO' | 'ELECTRICO' | 'AIRE_ACONDICIONADO' | 'SEGURIDAD' | 'OTRO'
export const SISTEMA_INTERVENIDO: Record<SistemaIntervenido, string> = {
  MOTOR: 'Motor',
  CASCO: 'Casco',
  ELECTRICO: 'Eléctrico',
  AIRE_ACONDICIONADO: 'Aire acondicionado',
  SEGURIDAD: 'Seguridad',
  OTRO: 'Otro',
}

export type RepuestoInsumo =
  | 'ACEITE_MOTOR' | 'FILTRO_ACEITE' | 'ACEITE_COLA' | 'GRASA_MARINA' | 'EMPAQUES_DRENAJE' | 'FILTRO_COMBUSTIBLE'
  | 'FILTRO_SEPARADOR_AGUA' | 'MANGUERAS_COMBUSTIBLE' | 'BUJIAS' | 'IMPULSOR_BOMBA_AGUA' | 'TERMOSTATO'
  | 'EMPAQUES_SELLOS' | 'HELICE' | 'PASADOR_TUERCA_HELICE' | 'ANODOS_SACRIFICIO' | 'BATERIA' | 'FUSIBLES'
  | 'CABLEADO_TERMINALES' | 'BOMBILLOS_NAVEGACION' | 'BOMBA_ACHIQUE' | 'DEFENSAS' | 'CABOS_AMARRE'
  | 'PINTURA_SELLANTE' | 'ASIENTOS_HERRAJES' | 'FILTRO_AIRE_ACONDICIONADO' | 'GAS_REFRIGERANTE'
  | 'CHALECOS_SALVAVIDAS' | 'AROS_SALVAVIDAS' | 'EXTINTOR' | 'BOTIQUIN' | 'PRODUCTOS_LIMPIEZA' | 'OTRO'
export const REPUESTO: Record<RepuestoInsumo, string> = {
  ACEITE_MOTOR: 'Aceite de motor',
  FILTRO_ACEITE: 'Filtro de aceite',
  ACEITE_COLA: 'Aceite de cola',
  GRASA_MARINA: 'Grasa marina',
  EMPAQUES_DRENAJE: 'Empaques de drenaje',
  FILTRO_COMBUSTIBLE: 'Filtro de combustible',
  FILTRO_SEPARADOR_AGUA: 'Filtro separador de agua',
  MANGUERAS_COMBUSTIBLE: 'Mangueras de combustible',
  BUJIAS: 'Bujías',
  IMPULSOR_BOMBA_AGUA: 'Impulsor de bomba de agua',
  TERMOSTATO: 'Termostato',
  EMPAQUES_SELLOS: 'Empaques y sellos',
  HELICE: 'Hélice',
  PASADOR_TUERCA_HELICE: 'Pasador y tuerca de hélice',
  ANODOS_SACRIFICIO: 'Ánodos de sacrificio',
  BATERIA: 'Batería',
  FUSIBLES: 'Fusibles',
  CABLEADO_TERMINALES: 'Cableado y terminales',
  BOMBILLOS_NAVEGACION: 'Bombillos de navegación',
  BOMBA_ACHIQUE: 'Bomba de achique',
  DEFENSAS: 'Defensas',
  CABOS_AMARRE: 'Cabos de amarre',
  PINTURA_SELLANTE: 'Pintura y sellante',
  ASIENTOS_HERRAJES: 'Asientos y herrajes',
  FILTRO_AIRE_ACONDICIONADO: 'Filtro de aire acondicionado',
  GAS_REFRIGERANTE: 'Gas refrigerante',
  CHALECOS_SALVAVIDAS: 'Chalecos salvavidas',
  AROS_SALVAVIDAS: 'Aros salvavidas',
  EXTINTOR: 'Extintor',
  BOTIQUIN: 'Botiquín',
  PRODUCTOS_LIMPIEZA: 'Productos de limpieza',
  OTRO: 'Otro',
}

// ---------- Infraestructura ----------
export type EstadoActivo = 'OPERATIVO' | 'OPERATIVO_CON_RESTRICCIONES' | 'FUERA_DE_SERVICIO'
export const ESTADO_ACTIVO: Record<EstadoActivo, string> = {
  OPERATIVO: 'Operativo',
  OPERATIVO_CON_RESTRICCIONES: 'Con restricciones',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
}

export type EstadoSeguimiento = 'ABIERTO' | 'COMUNICADO' | 'CERRADO'
export const ESTADO_SEGUIMIENTO: Record<EstadoSeguimiento, string> = {
  ABIERTO: 'Abierto',
  COMUNICADO: 'Comunicado',
  CERRADO: 'Cerrado',
}

export type TipoRegistroInfraestructura = 'REVISION' | 'MANTENIMIENTO' | 'NOVEDAD' | 'DETERIORO' | 'ALERTA_TEMPRANA'
export const TIPO_REGISTRO_INFRA: Record<TipoRegistroInfraestructura, string> = {
  REVISION: 'Revisión',
  MANTENIMIENTO: 'Mantenimiento',
  NOVEDAD: 'Novedad',
  DETERIORO: 'Deterioro',
  ALERTA_TEMPRANA: 'Alerta temprana',
}

type Ambito = 'EMBARCADERO' | 'AYUDA' | 'AMBOS'
export type ComponenteInfraestructura =
  | 'MUELLE' | 'RAMPA_ACCESO' | 'ESTACION_ENTRADA' | 'PLATAFORMA_EMBARQUE' | 'BARANDAS' | 'DEFENSAS_MUELLE' | 'TAQUILLA'
  | 'CUERPO_ESTRUCTURA' | 'FONDEO_AMARRE' | 'POSICION' | 'MARCA_TOPE' | 'LAMPARA' | 'LENTE' | 'PANEL_SOLAR_BATERIA'
  | 'PINTURA_REFLECTIVO' | 'ILUMINACION' | 'SENALIZACION' | 'GENERAL' | 'OTRO'
export const COMPONENTE: Record<ComponenteInfraestructura, { etiqueta: string; ambito: Ambito; requiereLuz: boolean }> = {
  MUELLE: { etiqueta: 'Muelle', ambito: 'EMBARCADERO', requiereLuz: false },
  RAMPA_ACCESO: { etiqueta: 'Rampa de acceso', ambito: 'EMBARCADERO', requiereLuz: false },
  ESTACION_ENTRADA: { etiqueta: 'Estación de entrada', ambito: 'EMBARCADERO', requiereLuz: false },
  PLATAFORMA_EMBARQUE: { etiqueta: 'Plataforma de embarque', ambito: 'EMBARCADERO', requiereLuz: false },
  BARANDAS: { etiqueta: 'Barandas', ambito: 'EMBARCADERO', requiereLuz: false },
  DEFENSAS_MUELLE: { etiqueta: 'Defensas del muelle', ambito: 'EMBARCADERO', requiereLuz: false },
  TAQUILLA: { etiqueta: 'Taquilla', ambito: 'EMBARCADERO', requiereLuz: false },
  CUERPO_ESTRUCTURA: { etiqueta: 'Cuerpo o estructura', ambito: 'AYUDA', requiereLuz: false },
  FONDEO_AMARRE: { etiqueta: 'Fondeo y amarre', ambito: 'AYUDA', requiereLuz: false },
  POSICION: { etiqueta: 'Posición (desplazada)', ambito: 'AYUDA', requiereLuz: false },
  MARCA_TOPE: { etiqueta: 'Marca de tope', ambito: 'AYUDA', requiereLuz: false },
  LAMPARA: { etiqueta: 'Lámpara', ambito: 'AYUDA', requiereLuz: true },
  LENTE: { etiqueta: 'Lente', ambito: 'AYUDA', requiereLuz: true },
  PANEL_SOLAR_BATERIA: { etiqueta: 'Panel solar y batería', ambito: 'AYUDA', requiereLuz: true },
  PINTURA_REFLECTIVO: { etiqueta: 'Pintura y reflectivo', ambito: 'AYUDA', requiereLuz: false },
  ILUMINACION: { etiqueta: 'Iluminación', ambito: 'AMBOS', requiereLuz: false },
  SENALIZACION: { etiqueta: 'Señalización', ambito: 'AMBOS', requiereLuz: false },
  GENERAL: { etiqueta: 'General', ambito: 'AMBOS', requiereLuz: false },
  OTRO: { etiqueta: 'Otro', ambito: 'AMBOS', requiereLuz: false },
}
export const componentesPara = (ambito: 'EMBARCADERO' | 'AYUDA', luminosa = true) =>
  (Object.keys(COMPONENTE) as ComponenteInfraestructura[]).filter((c) => {
    const d = COMPONENTE[c]
    return (d.ambito === ambito || d.ambito === 'AMBOS') && (luminosa || !d.requiereLuz)
  })

export type AspectoAfectado = 'OPERACION' | 'NAVEGACION' | 'ACCESIBILIDAD' | 'SEGURIDAD'
export const ASPECTO_AFECTADO: Record<AspectoAfectado, string> = {
  OPERACION: 'Operación',
  NAVEGACION: 'Navegación',
  ACCESIBILIDAD: 'Accesibilidad',
  SEGURIDAD: 'Seguridad',
}

export type EntidadCompetente =
  | 'NO_APLICA' | 'MUNICIPIO' | 'OTRO_CONTRATISTA' | 'OPERADOR' | 'PROPIETARIO_INFRAESTRUCTURA' | 'AUTORIDAD_COMPETENTE'
export const ENTIDAD_COMPETENTE: Record<EntidadCompetente, string> = {
  NO_APLICA: 'No aplica',
  MUNICIPIO: 'Municipio',
  OTRO_CONTRATISTA: 'Otro contratista',
  OPERADOR: 'Operador',
  PROPIETARIO_INFRAESTRUCTURA: 'Propietario de la infraestructura',
  AUTORIDAD_COMPETENTE: 'Autoridad competente',
}

export type MedioComunicacion = 'CORREO_ELECTRONICO' | 'OFICIO' | 'WHATSAPP' | 'LLAMADA' | 'REUNION'
export const MEDIO_COMUNICACION: Record<MedioComunicacion, string> = {
  CORREO_ELECTRONICO: 'Correo electrónico',
  OFICIO: 'Oficio',
  WHATSAPP: 'WhatsApp',
  LLAMADA: 'Llamada',
  REUNION: 'Reunión',
}

export type EstructuraAyuda =
  | 'BOYA' | 'BALIZA' | 'FARO' | 'LUZ_SECTOR' | 'ENFILACION' | 'SENALIZACION_PUENTE' | 'MARCA_AUXILIAR' | 'OTRA'
export const ESTRUCTURA_AYUDA: Record<EstructuraAyuda, string> = {
  BOYA: 'Boya',
  BALIZA: 'Baliza',
  FARO: 'Faro',
  LUZ_SECTOR: 'Luz de sector',
  ENFILACION: 'Enfilación',
  SENALIZACION_PUENTE: 'Señalización de puente',
  MARCA_AUXILIAR: 'Marca auxiliar',
  OTRA: 'Otra',
}

export type ColorLuz = 'BLANCA' | 'ROJA' | 'VERDE' | 'AMARILLA' | 'AZUL_AMARILLA'
export const COLOR_LUZ: Record<ColorLuz, string> = {
  BLANCA: 'Blanca',
  ROJA: 'Roja',
  VERDE: 'Verde',
  AMARILLA: 'Amarilla',
  AZUL_AMARILLA: 'Azul y amarilla',
}

export type FuncionMarca =
  | 'LATERAL_BABOR' | 'LATERAL_ESTRIBOR' | 'BIFURCACION_PREFERIDO_BABOR' | 'BIFURCACION_PREFERIDO_ESTRIBOR'
  | 'CARDINAL_NORTE' | 'CARDINAL_ESTE' | 'CARDINAL_SUR' | 'CARDINAL_OESTE' | 'PELIGRO_AISLADO' | 'AGUAS_SEGURAS'
  | 'ESPECIAL' | 'NUEVO_PELIGRO' | 'INFORMATIVA'
/** Etiqueta y color de luz que exige el Manual DIMAR (FuncionMarca.colorExigido). */
export const FUNCION_MARCA: Record<FuncionMarca, { etiqueta: string; color: ColorLuz | null }> = {
  LATERAL_BABOR: { etiqueta: 'Lateral de babor', color: 'VERDE' },
  LATERAL_ESTRIBOR: { etiqueta: 'Lateral de estribor', color: 'ROJA' },
  BIFURCACION_PREFERIDO_BABOR: { etiqueta: 'Bifurcación · canal preferido a babor', color: 'ROJA' },
  BIFURCACION_PREFERIDO_ESTRIBOR: { etiqueta: 'Bifurcación · canal preferido a estribor', color: 'VERDE' },
  CARDINAL_NORTE: { etiqueta: 'Cardinal norte', color: 'BLANCA' },
  CARDINAL_ESTE: { etiqueta: 'Cardinal este', color: 'BLANCA' },
  CARDINAL_SUR: { etiqueta: 'Cardinal sur', color: 'BLANCA' },
  CARDINAL_OESTE: { etiqueta: 'Cardinal oeste', color: 'BLANCA' },
  PELIGRO_AISLADO: { etiqueta: 'Peligro aislado', color: 'BLANCA' },
  AGUAS_SEGURAS: { etiqueta: 'Aguas seguras', color: 'BLANCA' },
  ESPECIAL: { etiqueta: 'Especial', color: 'AMARILLA' },
  NUEVO_PELIGRO: { etiqueta: 'Nuevo peligro', color: 'AZUL_AMARILLA' },
  INFORMATIVA: { etiqueta: 'Informativa', color: null },
}

// ---------- Comunicaciones ----------
export type TipoReferenciaFalla = 'OPERACION' | 'SUSPENSION' | 'MANTENIMIENTO'
export type EstadoEnvio = 'PENDIENTE' | 'ENVIADO' | 'FALLIDO'
export const ESTADO_ENVIO: Record<EstadoEnvio, string> = { PENDIENTE: 'Pendiente', ENVIADO: 'Enviado', FALLIDO: 'Fallido' }

// ---------- PQRS ----------
export type TipoPqrs = 'PETICION' | 'QUEJA' | 'RECLAMO' | 'SUGERENCIA' | 'FELICITACION'
export const TIPO_PQRS: Record<TipoPqrs, string> = {
  PETICION: 'Petición',
  QUEJA: 'Queja',
  RECLAMO: 'Reclamo',
  SUGERENCIA: 'Sugerencia',
  FELICITACION: 'Felicitación',
}
export type EstadoPqrs = 'RECIBIDA' | 'EN_GESTION' | 'RESPONDIDA' | 'TRASLADADA' | 'CERRADA'
export const ESTADO_PQRS: Record<EstadoPqrs, string> = {
  RECIBIDA: 'Recibida',
  EN_GESTION: 'En gestión',
  RESPONDIDA: 'Respondida',
  TRASLADADA: 'Trasladada',
  CERRADA: 'Cerrada',
}

// ---------- Informes, exportaciones y auditoría ----------
export type EstadoInforme = 'BORRADOR' | 'CERRADO' | 'ENTREGADO'
export const ESTADO_INFORME: Record<EstadoInforme, string> = {
  BORRADOR: 'En preparación',
  CERRADO: 'Cerrado',
  ENTREGADO: 'Entregado',
}

export type ConjuntoExportacion =
  | 'VIAJES' | 'PASAJEROS' | 'COMBUSTIBLE' | 'CONSUMO' | 'MANTENIMIENTOS' | 'NOVEDADES' | 'PERSONAL' | 'PQRS' | 'COMPLETO'
export const CONJUNTO_EXPORTACION: Record<ConjuntoExportacion, string> = {
  VIAJES: 'Bitácoras de viaje',
  PASAJEROS: 'Registro de usuarios',
  COMBUSTIBLE: 'Abastecimientos de combustible',
  CONSUMO: 'Consumo de combustible',
  MANTENIMIENTOS: 'Mantenimientos',
  NOVEDADES: 'Novedades',
  PERSONAL: 'Personal vinculado',
  PQRS: 'Peticiones, quejas y reclamos',
  COMPLETO: 'Libro completo (todas las hojas)',
}

export type TipoRegistroAuditado =
  | 'OPERACION' | 'PASAJEROS' | 'MANTENIMIENTO' | 'ABASTECIMIENTO' | 'PERSONAL' | 'EMBARCACION' | 'MOTOR'
  | 'INFRAESTRUCTURA' | 'AYUDA_NAVEGACION' | 'SUSPENSION' | 'COMUNICACION' | 'ADJUNTO'

export { opciones }

/** Las respuestas de operaciones traen la descripción del embarcadero ("Centro Calle 22"), no su nombre. */
const DESCRIPCION_EMBARCADERO: Record<Embarcaderos, string> = {
  RANCHO_GRANDE: 'Rancho Grande',
  CENTRO_CALLE_22: 'Centro Calle 22',
  CENTRO_VERDE: 'Centro Verde',
}
export const embarcaderoDesdeDescripcion = (d: string): Embarcaderos | undefined =>
  (Object.keys(DESCRIPCION_EMBARCADERO) as Embarcaderos[]).find((k) => DESCRIPCION_EMBARCADERO[k] === d || k === d)
/** Nombre para mostrar a partir de la descripción del backend. */
export const nombreEmbarcadero = (d: string) => {
  const e = embarcaderoDesdeDescripcion(d)
  return e ? EMBARCADERO[e] : d
}
