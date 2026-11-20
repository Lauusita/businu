-- =====================================================================================
-- BUSINÚ · Esquema inicial (R17)
--
-- Generado a partir del modelo JPA (Hibernate 7.4) sobre PostgreSQL 16 y validado con
-- spring.jpa.hibernate.ddl-auto=validate. 45 tablas: entidades de negocio, sus colecciones,
-- tablas de auditoría *_aud y revisiones (Hibernate Envers, R5).
--
-- A partir de esta versión, todo cambio del modelo va en una migración nueva (V2__..., V3__...),
-- nunca editando este archivo una vez aplicado en un ambiente.
-- =====================================================================================

CREATE TABLE abastecimientos_combustible (
    galones numeric(8,2) NOT NULL,
    tanque_lleno boolean NOT NULL,
    valor_total numeric(14,2) NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_hora timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    embarcacion_id uuid NOT NULL,
    id uuid NOT NULL,
    modificado_por uuid,
    operacion_id uuid,
    registrado_por_id uuid NOT NULL,
    soporte_id uuid NOT NULL,
    tipo_combustible character varying(30) NOT NULL,
    observaciones text NOT NULL,
    CONSTRAINT abastecimientos_combustible_tipo_combustible_check CHECK (((tipo_combustible)::text = ANY ((ARRAY['GASOLINA_CORRIENTE'::character varying, 'GASOLINA_EXTRA'::character varying, 'ACPM'::character varying])::text[])))
);

CREATE TABLE abastecimientos_combustible_aud (
    galones numeric(8,2),
    revtype smallint,
    tanque_lleno boolean,
    valor_total numeric(14,2),
    fecha_hora timestamp(6) with time zone,
    rev bigint NOT NULL,
    embarcacion_id uuid,
    id uuid NOT NULL,
    operacion_id uuid,
    registrado_por_id uuid,
    soporte_id uuid,
    tipo_combustible character varying(30),
    observaciones text,
    CONSTRAINT abastecimientos_combustible_aud_tipo_combustible_check CHECK (((tipo_combustible)::text = ANY ((ARRAY['GASOLINA_CORRIENTE'::character varying, 'GASOLINA_EXTRA'::character varying, 'ACPM'::character varying])::text[])))
);

CREATE TABLE adjuntos (
    vinculado boolean NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    tamano_bytes bigint NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    subido_por_id uuid NOT NULL,
    sha256 character varying(64) NOT NULL,
    content_type character varying(100) NOT NULL,
    clave_almacenamiento character varying(300) NOT NULL,
    nombre_original character varying(255) NOT NULL
);

CREATE TABLE adjuntos_aud (
    revtype smallint,
    vinculado boolean,
    rev bigint NOT NULL,
    tamano_bytes bigint,
    id uuid NOT NULL,
    subido_por_id uuid,
    sha256 character varying(64),
    content_type character varying(100),
    clave_almacenamiento character varying(300),
    nombre_original character varying(255)
);

CREATE TABLE ayudas_navegacion (
    activa boolean NOT NULL,
    latitud numeric(9,6),
    longitud numeric(9,6),
    luminosa boolean NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    codigo character varying(20) NOT NULL,
    color_luz character varying(20),
    estructura character varying(30) NOT NULL,
    tramo_desde character varying(30) NOT NULL,
    tramo_hasta character varying(30) NOT NULL,
    funcion character varying(40) NOT NULL,
    ritmo_luz character varying(40),
    referencia_ubicacion character varying(200) NOT NULL,
    descripcion character varying(300) NOT NULL,
    CONSTRAINT ayudas_navegacion_color_luz_check CHECK (((color_luz)::text = ANY ((ARRAY['BLANCA'::character varying, 'ROJA'::character varying, 'VERDE'::character varying, 'AMARILLA'::character varying, 'AZUL_AMARILLA'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_estructura_check CHECK (((estructura)::text = ANY ((ARRAY['BOYA'::character varying, 'BALIZA'::character varying, 'FARO'::character varying, 'LUZ_SECTOR'::character varying, 'ENFILACION'::character varying, 'SENALIZACION_PUENTE'::character varying, 'MARCA_AUXILIAR'::character varying, 'OTRA'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_funcion_check CHECK (((funcion)::text = ANY ((ARRAY['LATERAL_BABOR'::character varying, 'LATERAL_ESTRIBOR'::character varying, 'BIFURCACION_PREFERIDO_BABOR'::character varying, 'BIFURCACION_PREFERIDO_ESTRIBOR'::character varying, 'CARDINAL_NORTE'::character varying, 'CARDINAL_ESTE'::character varying, 'CARDINAL_SUR'::character varying, 'CARDINAL_OESTE'::character varying, 'PELIGRO_AISLADO'::character varying, 'AGUAS_SEGURAS'::character varying, 'ESPECIAL'::character varying, 'NUEVO_PELIGRO'::character varying, 'INFORMATIVA'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_tramo_desde_check CHECK (((tramo_desde)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_tramo_hasta_check CHECK (((tramo_hasta)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[])))
);

CREATE TABLE ayudas_navegacion_aud (
    activa boolean,
    latitud numeric(9,6),
    longitud numeric(9,6),
    luminosa boolean,
    revtype smallint,
    rev bigint NOT NULL,
    id uuid NOT NULL,
    codigo character varying(20),
    color_luz character varying(20),
    estructura character varying(30),
    tramo_desde character varying(30),
    tramo_hasta character varying(30),
    funcion character varying(40),
    ritmo_luz character varying(40),
    referencia_ubicacion character varying(200),
    descripcion character varying(300),
    CONSTRAINT ayudas_navegacion_aud_color_luz_check CHECK (((color_luz)::text = ANY ((ARRAY['BLANCA'::character varying, 'ROJA'::character varying, 'VERDE'::character varying, 'AMARILLA'::character varying, 'AZUL_AMARILLA'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_aud_estructura_check CHECK (((estructura)::text = ANY ((ARRAY['BOYA'::character varying, 'BALIZA'::character varying, 'FARO'::character varying, 'LUZ_SECTOR'::character varying, 'ENFILACION'::character varying, 'SENALIZACION_PUENTE'::character varying, 'MARCA_AUXILIAR'::character varying, 'OTRA'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_aud_funcion_check CHECK (((funcion)::text = ANY ((ARRAY['LATERAL_BABOR'::character varying, 'LATERAL_ESTRIBOR'::character varying, 'BIFURCACION_PREFERIDO_BABOR'::character varying, 'BIFURCACION_PREFERIDO_ESTRIBOR'::character varying, 'CARDINAL_NORTE'::character varying, 'CARDINAL_ESTE'::character varying, 'CARDINAL_SUR'::character varying, 'CARDINAL_OESTE'::character varying, 'PELIGRO_AISLADO'::character varying, 'AGUAS_SEGURAS'::character varying, 'ESPECIAL'::character varying, 'NUEVO_PELIGRO'::character varying, 'INFORMATIVA'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_aud_tramo_desde_check CHECK (((tramo_desde)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT ayudas_navegacion_aud_tramo_hasta_check CHECK (((tramo_hasta)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[])))
);

CREATE TABLE comunicaciones_falla (
    intentos integer NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_envio timestamp(6) with time zone,
    version bigint NOT NULL,
    estado character varying(12) NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    referencia_id uuid NOT NULL,
    registrado_por_id uuid NOT NULL,
    referencia_tipo character varying(20) NOT NULL,
    destinatario character varying(150) NOT NULL,
    asunto character varying(200) NOT NULL,
    error text,
    mensaje text NOT NULL,
    message_id character varying(255),
    CONSTRAINT comunicaciones_falla_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'ENVIADO'::character varying, 'FALLIDO'::character varying])::text[]))),
    CONSTRAINT comunicaciones_falla_referencia_tipo_check CHECK (((referencia_tipo)::text = ANY ((ARRAY['OPERACION'::character varying, 'SUSPENSION'::character varying, 'MANTENIMIENTO'::character varying])::text[])))
);

CREATE TABLE comunicaciones_falla_aud (
    intentos integer,
    revtype smallint,
    fecha_envio timestamp(6) with time zone,
    rev bigint NOT NULL,
    estado character varying(12),
    id uuid NOT NULL,
    referencia_id uuid,
    registrado_por_id uuid,
    referencia_tipo character varying(20),
    destinatario character varying(150),
    asunto character varying(200),
    error text,
    mensaje text,
    message_id character varying(255),
    CONSTRAINT comunicaciones_falla_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'ENVIADO'::character varying, 'FALLIDO'::character varying])::text[]))),
    CONSTRAINT comunicaciones_falla_aud_referencia_tipo_check CHECK (((referencia_tipo)::text = ANY ((ARRAY['OPERACION'::character varying, 'SUSPENSION'::character varying, 'MANTENIMIENTO'::character varying])::text[])))
);

CREATE TABLE embarcacion (
    anio_construccion integer NOT NULL,
    capacidad_pasajeros integer NOT NULL,
    horas_operacion_dia numeric(4,1) NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    estado character varying(20) NOT NULL,
    patente character varying(40) NOT NULL,
    nombre character varying(60) NOT NULL,
    constructor character varying(100),
    numero_identificador_construccion character varying(255),
    CONSTRAINT embarcacion_estado_check CHECK (((estado)::text = ANY ((ARRAY['OPERATIVA'::character varying, 'EN_MANTENIMIENTO'::character varying, 'FUERA_DE_SERVICIO'::character varying])::text[]))),
    CONSTRAINT embarcacion_nombre_check CHECK (((nombre)::text = ANY ((ARRAY['BETANCI'::character varying, 'YAPEL'::character varying])::text[])))
);

CREATE TABLE embarcacion_aud (
    anio_construccion integer,
    capacidad_pasajeros integer,
    horas_operacion_dia numeric(4,1),
    revtype smallint,
    rev bigint NOT NULL,
    id uuid NOT NULL,
    estado character varying(20),
    patente character varying(40),
    nombre character varying(60),
    constructor character varying(100),
    numero_identificador_construccion character varying(255),
    CONSTRAINT embarcacion_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['OPERATIVA'::character varying, 'EN_MANTENIMIENTO'::character varying, 'FUERA_DE_SERVICIO'::character varying])::text[]))),
    CONSTRAINT embarcacion_aud_nombre_check CHECK (((nombre)::text = ANY ((ARRAY['BETANCI'::character varying, 'YAPEL'::character varying])::text[])))
);

CREATE TABLE mantenimiento_novedades (
    severidad character varying(10) NOT NULL,
    mantenimiento_id uuid NOT NULL,
    condicion character varying(40) NOT NULL,
    detalle text NOT NULL,
    CONSTRAINT mantenimiento_novedades_condicion_check CHECK (((condicion)::text = ANY ((ARRAY['RUIDO_ANORMAL_MOTOR'::character varying, 'SOBRECALENTAMIENTO'::character varying, 'VIBRACION'::character varying, 'PERDIDA_POTENCIA'::character varying, 'FALLA_ARRANQUE'::character varying, 'FUGA_ACEITE'::character varying, 'FUGA_COMBUSTIBLE'::character varying, 'FALLA_DIRECCION'::character varying, 'GOLPE_HELICE'::character varying, 'FALLA_ELECTRICA'::character varying, 'FALLA_LUCES_NAVEGACION'::character varying, 'FALLA_BOMBA_ACHIQUE'::character varying, 'FALLA_AIRE_ACONDICIONADO'::character varying, 'INGRESO_AGUA'::character varying, 'OTRA'::character varying])::text[]))),
    CONSTRAINT mantenimiento_novedades_severidad_check CHECK (((severidad)::text = ANY ((ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying])::text[])))
);

CREATE TABLE mantenimiento_recomendaciones (
    mantenimiento_id uuid NOT NULL,
    tipo character varying(20) NOT NULL,
    descripcion text NOT NULL,
    CONSTRAINT mantenimiento_recomendaciones_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CORRECTIVA'::character varying, 'PREVENTIVA'::character varying, 'PREDICTIVA'::character varying])::text[])))
);

CREATE TABLE mantenimiento_repuestos (
    cantidad numeric(10,2) NOT NULL,
    costo numeric(14,2) NOT NULL,
    mantenimiento_id uuid NOT NULL,
    unidad character varying(20) NOT NULL,
    item character varying(40) NOT NULL,
    descripcion character varying(200) NOT NULL,
    CONSTRAINT mantenimiento_repuestos_item_check CHECK (((item)::text = ANY ((ARRAY['ACEITE_MOTOR'::character varying, 'FILTRO_ACEITE'::character varying, 'ACEITE_COLA'::character varying, 'GRASA_MARINA'::character varying, 'EMPAQUES_DRENAJE'::character varying, 'FILTRO_COMBUSTIBLE'::character varying, 'FILTRO_SEPARADOR_AGUA'::character varying, 'MANGUERAS_COMBUSTIBLE'::character varying, 'BUJIAS'::character varying, 'IMPULSOR_BOMBA_AGUA'::character varying, 'TERMOSTATO'::character varying, 'EMPAQUES_SELLOS'::character varying, 'HELICE'::character varying, 'PASADOR_TUERCA_HELICE'::character varying, 'ANODOS_SACRIFICIO'::character varying, 'BATERIA'::character varying, 'FUSIBLES'::character varying, 'CABLEADO_TERMINALES'::character varying, 'BOMBILLOS_NAVEGACION'::character varying, 'BOMBA_ACHIQUE'::character varying, 'DEFENSAS'::character varying, 'CABOS_AMARRE'::character varying, 'PINTURA_SELLANTE'::character varying, 'ASIENTOS_HERRAJES'::character varying, 'FILTRO_AIRE_ACONDICIONADO'::character varying, 'GAS_REFRIGERANTE'::character varying, 'CHALECOS_SALVAVIDAS'::character varying, 'AROS_SALVAVIDAS'::character varying, 'EXTINTOR'::character varying, 'BOTIQUIN'::character varying, 'PRODUCTOS_LIMPIEZA'::character varying, 'OTRO'::character varying])::text[])))
);

CREATE TABLE mantenimiento_soportes (
    adjunto_id uuid NOT NULL,
    mantenimiento_id uuid NOT NULL
);

CREATE TABLE mantenimiento_soportes_aud (
    revtype smallint,
    rev bigint NOT NULL,
    adjunto_id uuid NOT NULL,
    mantenimiento_id uuid NOT NULL
);

CREATE TABLE mantenimientos (
    costo_mano_obra numeric(14,2) NOT NULL,
    sin_novedades boolean NOT NULL,
    sin_repuestos boolean NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_intervencion timestamp(6) with time zone NOT NULL,
    proxima_intervencion timestamp(6) with time zone,
    version bigint NOT NULL,
    creado_por uuid,
    embarcacion_id uuid NOT NULL,
    id uuid NOT NULL,
    modificado_por uuid,
    motor_id uuid,
    registrado_por_id uuid NOT NULL,
    responsable_id uuid NOT NULL,
    tipo character varying(20) NOT NULL,
    sistema character varying(30) NOT NULL,
    ejecutado_por character varying(150) NOT NULL,
    condicion_proxima_intervencion character varying(300) NOT NULL,
    descripcion_actividades text NOT NULL,
    CONSTRAINT mantenimientos_sistema_check CHECK (((sistema)::text = ANY ((ARRAY['MOTOR'::character varying, 'CASCO'::character varying, 'ELECTRICO'::character varying, 'AIRE_ACONDICIONADO'::character varying, 'SEGURIDAD'::character varying, 'OTRO'::character varying])::text[]))),
    CONSTRAINT mantenimientos_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CORRECTIVO'::character varying, 'PREVENTIVO'::character varying, 'PREDICTIVO'::character varying])::text[])))
);

CREATE TABLE mantenimientos_aud (
    costo_mano_obra numeric(14,2),
    revtype smallint,
    sin_novedades boolean,
    sin_repuestos boolean,
    fecha_intervencion timestamp(6) with time zone,
    proxima_intervencion timestamp(6) with time zone,
    rev bigint NOT NULL,
    embarcacion_id uuid,
    id uuid NOT NULL,
    motor_id uuid,
    registrado_por_id uuid,
    responsable_id uuid,
    tipo character varying(20),
    sistema character varying(30),
    ejecutado_por character varying(150),
    condicion_proxima_intervencion character varying(300),
    descripcion_actividades text,
    CONSTRAINT mantenimientos_aud_sistema_check CHECK (((sistema)::text = ANY ((ARRAY['MOTOR'::character varying, 'CASCO'::character varying, 'ELECTRICO'::character varying, 'AIRE_ACONDICIONADO'::character varying, 'SEGURIDAD'::character varying, 'OTRO'::character varying])::text[]))),
    CONSTRAINT mantenimientos_aud_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CORRECTIVO'::character varying, 'PREVENTIVO'::character varying, 'PREDICTIVO'::character varying])::text[])))
);

CREATE TABLE motor (
    horometro_actual numeric(10,1) NOT NULL,
    intervalo_servicio_horas integer NOT NULL,
    potencia_hp integer NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    embarcacion_id uuid NOT NULL,
    id uuid NOT NULL,
    modificado_por uuid,
    marca character varying(40) NOT NULL,
    modelo character varying(60),
    serial character varying(60) NOT NULL
);

CREATE TABLE motor_aud (
    horometro_actual numeric(10,1),
    intervalo_servicio_horas integer,
    potencia_hp integer,
    revtype smallint,
    rev bigint NOT NULL,
    embarcacion_id uuid,
    id uuid NOT NULL,
    marca character varying(40),
    modelo character varying(60),
    serial character varying(60)
);

CREATE TABLE operacion_condiciones_mecanicas (
    severidad character varying(10) NOT NULL,
    operacion_id uuid NOT NULL,
    condicion character varying(40) NOT NULL,
    detalle text NOT NULL,
    CONSTRAINT operacion_condiciones_mecanicas_condicion_check CHECK (((condicion)::text = ANY ((ARRAY['RUIDO_ANORMAL_MOTOR'::character varying, 'SOBRECALENTAMIENTO'::character varying, 'VIBRACION'::character varying, 'PERDIDA_POTENCIA'::character varying, 'FALLA_ARRANQUE'::character varying, 'FUGA_ACEITE'::character varying, 'FUGA_COMBUSTIBLE'::character varying, 'FALLA_DIRECCION'::character varying, 'GOLPE_HELICE'::character varying, 'FALLA_ELECTRICA'::character varying, 'FALLA_LUCES_NAVEGACION'::character varying, 'FALLA_BOMBA_ACHIQUE'::character varying, 'FALLA_AIRE_ACONDICIONADO'::character varying, 'INGRESO_AGUA'::character varying, 'OTRA'::character varying])::text[]))),
    CONSTRAINT operacion_condiciones_mecanicas_severidad_check CHECK (((severidad)::text = ANY ((ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying])::text[])))
);

CREATE TABLE operacion_horometros (
    lectura_final numeric(10,1),
    lectura_inicial numeric(10,1) NOT NULL,
    motor_id uuid NOT NULL,
    operacion_id uuid NOT NULL
);

CREATE TABLE operacion_paradas (
    orden integer NOT NULL,
    hora_llegada timestamp(6) with time zone,
    hora_salida timestamp(6) with time zone,
    operacion_id uuid NOT NULL,
    embarcadero character varying(30) NOT NULL,
    CONSTRAINT operacion_paradas_embarcadero_check CHECK (((embarcadero)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT operacion_paradas_orden_check CHECK ((orden >= 0))
);

CREATE TABLE operacion_tripulacion (
    operacion_id uuid NOT NULL,
    personal_id uuid NOT NULL
);

CREATE TABLE operacion_tripulacion_aud (
    revtype smallint,
    rev bigint NOT NULL,
    operacion_id uuid NOT NULL,
    personal_id uuid NOT NULL
);

CREATE TABLE operaciones (
    cargue_combustible boolean NOT NULL,
    horometro_final numeric(10,1),
    verif_aceite_motor boolean NOT NULL,
    verif_bomba_achique boolean NOT NULL,
    verif_chalecos_salvavidas boolean NOT NULL,
    verif_combustible boolean NOT NULL,
    verif_luces_navegacion boolean NOT NULL,
    verif_radio_comunicacion boolean NOT NULL,
    verif_sistema_direccion boolean NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_hora_fin timestamp(6) with time zone,
    fecha_hora_inicio timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    estado_nave character varying(10) NOT NULL,
    nivel_rio character varying(10) NOT NULL,
    creado_por uuid,
    embarcacion_id uuid NOT NULL,
    id uuid NOT NULL,
    modificado_por uuid,
    registrado_por_id uuid NOT NULL,
    estado character varying(20) NOT NULL,
    condicion_climatica character varying(30) NOT NULL,
    embarcadero_destino character varying(30) NOT NULL,
    embarcadero_origen character varying(30) NOT NULL,
    causa_suspension character varying(40),
    autoridad_suspension character varying(150),
    detalle_suspension text,
    justificacion_verificacion text NOT NULL,
    motivo_suspension text,
    novedades text NOT NULL,
    observaciones text NOT NULL,
    CONSTRAINT operaciones_causa_suspension_check CHECK (((causa_suspension)::text = ANY ((ARRAY['INSTRUCCION_AUTORIDAD'::character varying, 'CONDICION_CLIMATICA'::character varying, 'NIVEL_RIO'::character varying, 'RIESGO_INUNDACION_CRECIENTE'::character varying, 'FALLA_TECNICA_MECANICA'::character varying, 'OTRA'::character varying])::text[]))),
    CONSTRAINT operaciones_condicion_climatica_check CHECK (((condicion_climatica)::text = ANY ((ARRAY['DESPEJADO'::character varying, 'NUBLADO'::character varying, 'LLUVIA_LEVE'::character varying, 'LLUVIA_FUERTE'::character varying, 'TORMENTA_ELECTRICA'::character varying, 'VIENTO_FUERTE'::character varying, 'VENDAVAL'::character varying])::text[]))),
    CONSTRAINT operaciones_embarcadero_destino_check CHECK (((embarcadero_destino)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT operaciones_embarcadero_origen_check CHECK (((embarcadero_origen)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT operaciones_estado_check CHECK (((estado)::text = ANY ((ARRAY['EN_CURSO'::character varying, 'FINALIZADA'::character varying, 'SUSPENDIDA'::character varying])::text[]))),
    CONSTRAINT operaciones_estado_nave_check CHECK (((estado_nave)::text = ANY ((ARRAY['BUENO'::character varying, 'REGULAR'::character varying, 'MALO'::character varying])::text[]))),
    CONSTRAINT operaciones_nivel_rio_check CHECK (((nivel_rio)::text = ANY ((ARRAY['BAJO'::character varying, 'NORMAL'::character varying, 'ALTO'::character varying])::text[])))
);

CREATE TABLE operaciones_aud (
    cargue_combustible boolean,
    horometro_final numeric(10,1),
    revtype smallint,
    verif_aceite_motor boolean,
    verif_bomba_achique boolean,
    verif_chalecos_salvavidas boolean,
    verif_combustible boolean,
    verif_luces_navegacion boolean,
    verif_radio_comunicacion boolean,
    verif_sistema_direccion boolean,
    fecha_hora_fin timestamp(6) with time zone,
    fecha_hora_inicio timestamp(6) with time zone,
    rev bigint NOT NULL,
    estado_nave character varying(10),
    nivel_rio character varying(10),
    embarcacion_id uuid,
    id uuid NOT NULL,
    registrado_por_id uuid,
    estado character varying(20),
    condicion_climatica character varying(30),
    embarcadero_destino character varying(30),
    embarcadero_origen character varying(30),
    causa_suspension character varying(40),
    autoridad_suspension character varying(150),
    detalle_suspension text,
    justificacion_verificacion text,
    motivo_suspension text,
    novedades text,
    observaciones text,
    CONSTRAINT operaciones_aud_causa_suspension_check CHECK (((causa_suspension)::text = ANY ((ARRAY['INSTRUCCION_AUTORIDAD'::character varying, 'CONDICION_CLIMATICA'::character varying, 'NIVEL_RIO'::character varying, 'RIESGO_INUNDACION_CRECIENTE'::character varying, 'FALLA_TECNICA_MECANICA'::character varying, 'OTRA'::character varying])::text[]))),
    CONSTRAINT operaciones_aud_condicion_climatica_check CHECK (((condicion_climatica)::text = ANY ((ARRAY['DESPEJADO'::character varying, 'NUBLADO'::character varying, 'LLUVIA_LEVE'::character varying, 'LLUVIA_FUERTE'::character varying, 'TORMENTA_ELECTRICA'::character varying, 'VIENTO_FUERTE'::character varying, 'VENDAVAL'::character varying])::text[]))),
    CONSTRAINT operaciones_aud_embarcadero_destino_check CHECK (((embarcadero_destino)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT operaciones_aud_embarcadero_origen_check CHECK (((embarcadero_origen)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT operaciones_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['EN_CURSO'::character varying, 'FINALIZADA'::character varying, 'SUSPENDIDA'::character varying])::text[]))),
    CONSTRAINT operaciones_aud_estado_nave_check CHECK (((estado_nave)::text = ANY ((ARRAY['BUENO'::character varying, 'REGULAR'::character varying, 'MALO'::character varying])::text[]))),
    CONSTRAINT operaciones_aud_nivel_rio_check CHECK (((nivel_rio)::text = ANY ((ARRAY['BAJO'::character varying, 'NORMAL'::character varying, 'ALTO'::character varying])::text[])))
);

CREATE TABLE personal (
    contrato_activo boolean,
    disponibilidad boolean NOT NULL,
    fecha_retiro date,
    fecha_vinculacion date NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    ultimo_acceso timestamp(6) with time zone,
    version bigint NOT NULL,
    tipo_identificacion character varying(10) NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    numero_identificacion character varying(20) NOT NULL,
    rol character varying(20) NOT NULL,
    tipo_vinculacion character varying(30) NOT NULL,
    apellidos character varying(80) NOT NULL,
    nombres character varying(80) NOT NULL,
    password_hash character varying(100),
    email character varying(120),
    CONSTRAINT personal_rol_check CHECK (((rol)::text = ANY ((ARRAY['SUPERVISOR'::character varying, 'GUIA_TURISTICA'::character varying, 'OPERADOR_EMBARCACION'::character varying, 'TAQUILLERO'::character varying])::text[]))),
    CONSTRAINT personal_tipo_identificacion_check CHECK (((tipo_identificacion)::text = ANY ((ARRAY['CC'::character varying, 'CE'::character varying, 'PPT'::character varying, 'PASAPORTE'::character varying])::text[]))),
    CONSTRAINT personal_tipo_vinculacion_check CHECK (((tipo_vinculacion)::text = ANY ((ARRAY['LABORAL'::character varying, 'PRESTACION_SERVICIOS'::character varying])::text[])))
);

CREATE TABLE personal_aud (
    contrato_activo boolean,
    disponibilidad boolean,
    fecha_retiro date,
    fecha_vinculacion date,
    revtype smallint,
    rev bigint NOT NULL,
    tipo_identificacion character varying(10),
    id uuid NOT NULL,
    numero_identificacion character varying(20),
    rol character varying(20),
    tipo_vinculacion character varying(30),
    apellidos character varying(80),
    nombres character varying(80),
    email character varying(120),
    CONSTRAINT personal_aud_rol_check CHECK (((rol)::text = ANY ((ARRAY['SUPERVISOR'::character varying, 'GUIA_TURISTICA'::character varying, 'OPERADOR_EMBARCACION'::character varying, 'TAQUILLERO'::character varying])::text[]))),
    CONSTRAINT personal_aud_tipo_identificacion_check CHECK (((tipo_identificacion)::text = ANY ((ARRAY['CC'::character varying, 'CE'::character varying, 'PPT'::character varying, 'PASAPORTE'::character varying])::text[]))),
    CONSTRAINT personal_aud_tipo_vinculacion_check CHECK (((tipo_vinculacion)::text = ANY ((ARRAY['LABORAL'::character varying, 'PRESTACION_SERVICIOS'::character varying])::text[])))
);

CREATE TABLE registro_infraestructura_aspectos (
    registro_id uuid NOT NULL,
    aspecto character varying(20) NOT NULL,
    CONSTRAINT registro_infraestructura_aspectos_aspecto_check CHECK (((aspecto)::text = ANY ((ARRAY['OPERACION'::character varying, 'NAVEGACION'::character varying, 'ACCESIBILIDAD'::character varying, 'SEGURIDAD'::character varying])::text[])))
);

CREATE TABLE registro_infraestructura_aspectos_aud (
    revtype smallint,
    rev bigint NOT NULL,
    registro_id uuid NOT NULL,
    aspecto character varying(20) NOT NULL,
    CONSTRAINT registro_infraestructura_aspectos_aud_aspecto_check CHECK (((aspecto)::text = ANY ((ARRAY['OPERACION'::character varying, 'NAVEGACION'::character varying, 'ACCESIBILIDAD'::character varying, 'SEGURIDAD'::character varying])::text[])))
);

CREATE TABLE registro_infraestructura_evidencias (
    adjunto_id uuid NOT NULL,
    registro_id uuid NOT NULL
);

CREATE TABLE registro_infraestructura_evidencias_aud (
    revtype smallint,
    rev bigint NOT NULL,
    adjunto_id uuid NOT NULL,
    registro_id uuid NOT NULL
);

CREATE TABLE registros_infraestructura (
    requiere_intervencion_externa boolean NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    cerrado_en timestamp(6) with time zone,
    comunicado_en timestamp(6) with time zone,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_hora timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    severidad character varying(10) NOT NULL,
    ayuda_id uuid,
    cerrado_por_id uuid,
    comunicado_por_id uuid,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    registrado_por_id uuid NOT NULL,
    estado character varying(20) NOT NULL,
    tipo character varying(20) NOT NULL,
    componente character varying(30) NOT NULL,
    comunicado_medio character varying(30),
    embarcadero character varying(30),
    entidad_competente character varying(30) NOT NULL,
    estado_observado character varying(30) NOT NULL,
    comunicado_referencia character varying(200),
    acciones_realizadas text NOT NULL,
    descripcion text NOT NULL,
    observacion_cierre text,
    CONSTRAINT registros_infraestructura_componente_check CHECK (((componente)::text = ANY ((ARRAY['MUELLE'::character varying, 'RAMPA_ACCESO'::character varying, 'ESTACION_ENTRADA'::character varying, 'PLATAFORMA_EMBARQUE'::character varying, 'BARANDAS'::character varying, 'DEFENSAS_MUELLE'::character varying, 'TAQUILLA'::character varying, 'CUERPO_ESTRUCTURA'::character varying, 'FONDEO_AMARRE'::character varying, 'POSICION'::character varying, 'MARCA_TOPE'::character varying, 'LAMPARA'::character varying, 'LENTE'::character varying, 'PANEL_SOLAR_BATERIA'::character varying, 'PINTURA_REFLECTIVO'::character varying, 'ILUMINACION'::character varying, 'SENALIZACION'::character varying, 'GENERAL'::character varying, 'OTRO'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_comunicado_medio_check CHECK (((comunicado_medio)::text = ANY ((ARRAY['CORREO_ELECTRONICO'::character varying, 'OFICIO'::character varying, 'WHATSAPP'::character varying, 'LLAMADA'::character varying, 'REUNION'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_embarcadero_check CHECK (((embarcadero)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_entidad_competente_check CHECK (((entidad_competente)::text = ANY ((ARRAY['NO_APLICA'::character varying, 'MUNICIPIO'::character varying, 'OTRO_CONTRATISTA'::character varying, 'OPERADOR'::character varying, 'PROPIETARIO_INFRAESTRUCTURA'::character varying, 'AUTORIDAD_COMPETENTE'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_estado_check CHECK (((estado)::text = ANY ((ARRAY['ABIERTO'::character varying, 'COMUNICADO'::character varying, 'CERRADO'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_estado_observado_check CHECK (((estado_observado)::text = ANY ((ARRAY['OPERATIVO'::character varying, 'OPERATIVO_CON_RESTRICCIONES'::character varying, 'FUERA_DE_SERVICIO'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_severidad_check CHECK (((severidad)::text = ANY ((ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['REVISION'::character varying, 'MANTENIMIENTO'::character varying, 'NOVEDAD'::character varying, 'DETERIORO'::character varying, 'ALERTA_TEMPRANA'::character varying])::text[])))
);

CREATE TABLE registros_infraestructura_aud (
    requiere_intervencion_externa boolean,
    revtype smallint,
    cerrado_en timestamp(6) with time zone,
    comunicado_en timestamp(6) with time zone,
    fecha_hora timestamp(6) with time zone,
    rev bigint NOT NULL,
    severidad character varying(10),
    ayuda_id uuid,
    cerrado_por_id uuid,
    comunicado_por_id uuid,
    id uuid NOT NULL,
    registrado_por_id uuid,
    estado character varying(20),
    tipo character varying(20),
    componente character varying(30),
    comunicado_medio character varying(30),
    embarcadero character varying(30),
    entidad_competente character varying(30),
    estado_observado character varying(30),
    comunicado_referencia character varying(200),
    acciones_realizadas text,
    descripcion text,
    observacion_cierre text,
    CONSTRAINT registros_infraestructura_aud_componente_check CHECK (((componente)::text = ANY ((ARRAY['MUELLE'::character varying, 'RAMPA_ACCESO'::character varying, 'ESTACION_ENTRADA'::character varying, 'PLATAFORMA_EMBARQUE'::character varying, 'BARANDAS'::character varying, 'DEFENSAS_MUELLE'::character varying, 'TAQUILLA'::character varying, 'CUERPO_ESTRUCTURA'::character varying, 'FONDEO_AMARRE'::character varying, 'POSICION'::character varying, 'MARCA_TOPE'::character varying, 'LAMPARA'::character varying, 'LENTE'::character varying, 'PANEL_SOLAR_BATERIA'::character varying, 'PINTURA_REFLECTIVO'::character varying, 'ILUMINACION'::character varying, 'SENALIZACION'::character varying, 'GENERAL'::character varying, 'OTRO'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_comunicado_medio_check CHECK (((comunicado_medio)::text = ANY ((ARRAY['CORREO_ELECTRONICO'::character varying, 'OFICIO'::character varying, 'WHATSAPP'::character varying, 'LLAMADA'::character varying, 'REUNION'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_embarcadero_check CHECK (((embarcadero)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_entidad_competente_check CHECK (((entidad_competente)::text = ANY ((ARRAY['NO_APLICA'::character varying, 'MUNICIPIO'::character varying, 'OTRO_CONTRATISTA'::character varying, 'OPERADOR'::character varying, 'PROPIETARIO_INFRAESTRUCTURA'::character varying, 'AUTORIDAD_COMPETENTE'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['ABIERTO'::character varying, 'COMUNICADO'::character varying, 'CERRADO'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_estado_observado_check CHECK (((estado_observado)::text = ANY ((ARRAY['OPERATIVO'::character varying, 'OPERATIVO_CON_RESTRICCIONES'::character varying, 'FUERA_DE_SERVICIO'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_severidad_check CHECK (((severidad)::text = ANY ((ARRAY['BAJA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying])::text[]))),
    CONSTRAINT registros_infraestructura_aud_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['REVISION'::character varying, 'MANTENIMIENTO'::character varying, 'NOVEDAD'::character varying, 'DETERIORO'::character varying, 'ALERTA_TEMPRANA'::character varying])::text[])))
);

CREATE TABLE revisiones (
    fecha bigint NOT NULL,
    id bigint NOT NULL,
    personal_id uuid
);

CREATE SEQUENCE revisiones_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE suspensiones_embarcacion (
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_fin timestamp(6) with time zone,
    fecha_inicio timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    embarcacion_id uuid NOT NULL,
    id uuid NOT NULL,
    levantada_por_id uuid,
    modificado_por uuid,
    registrado_por_id uuid NOT NULL,
    causa character varying(40) NOT NULL,
    autoridad character varying(150) NOT NULL,
    detalle text NOT NULL,
    observacion_levantamiento text,
    CONSTRAINT suspensiones_embarcacion_causa_check CHECK (((causa)::text = ANY ((ARRAY['INSTRUCCION_AUTORIDAD'::character varying, 'CONDICION_CLIMATICA'::character varying, 'NIVEL_RIO'::character varying, 'RIESGO_INUNDACION_CRECIENTE'::character varying, 'FALLA_TECNICA_MECANICA'::character varying, 'OTRA'::character varying])::text[])))
);

CREATE TABLE suspensiones_embarcacion_aud (
    revtype smallint,
    fecha_fin timestamp(6) with time zone,
    fecha_inicio timestamp(6) with time zone,
    rev bigint NOT NULL,
    embarcacion_id uuid,
    id uuid NOT NULL,
    levantada_por_id uuid,
    registrado_por_id uuid,
    causa character varying(40),
    autoridad character varying(150),
    detalle text,
    observacion_levantamiento text,
    CONSTRAINT suspensiones_embarcacion_aud_causa_check CHECK (((causa)::text = ANY ((ARRAY['INSTRUCCION_AUTORIDAD'::character varying, 'CONDICION_CLIMATICA'::character varying, 'NIVEL_RIO'::character varying, 'RIESGO_INUNDACION_CRECIENTE'::character varying, 'FALLA_TECNICA_MECANICA'::character varying, 'OTRA'::character varying])::text[])))
);

CREATE TABLE usuarios (
    usuarios_transportados integer NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_hora timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    operacion_id uuid NOT NULL,
    registrado_por_id uuid NOT NULL,
    embarcadero_abordaje character varying(30) NOT NULL,
    CONSTRAINT usuarios_embarcadero_abordaje_check CHECK (((embarcadero_abordaje)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[])))
);

CREATE TABLE usuarios_aud (
    revtype smallint,
    usuarios_transportados integer,
    fecha_hora timestamp(6) with time zone,
    rev bigint NOT NULL,
    id uuid NOT NULL,
    operacion_id uuid,
    registrado_por_id uuid,
    embarcadero_abordaje character varying(30),
    CONSTRAINT usuarios_aud_embarcadero_abordaje_check CHECK (((embarcadero_abordaje)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[])))
);

CREATE TABLE usuarios_destino (
    cantidad integer NOT NULL,
    usuario_id uuid NOT NULL,
    embarcadero_destino character varying(30) NOT NULL,
    CONSTRAINT usuarios_destino_embarcadero_destino_check CHECK (((embarcadero_destino)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[])))
);

CREATE TABLE usuarios_destino_aud (
    cantidad integer NOT NULL,
    revtype smallint,
    rev bigint NOT NULL,
    usuario_id uuid NOT NULL,
    embarcadero_destino character varying(30) NOT NULL,
    CONSTRAINT usuarios_destino_aud_embarcadero_destino_check CHECK (((embarcadero_destino)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[])))
);

CREATE TABLE usuarios_grupo_etario (
    cantidad integer NOT NULL,
    usuario_id uuid NOT NULL,
    grupo_etario character varying(20) NOT NULL,
    CONSTRAINT usuarios_grupo_etario_grupo_etario_check CHECK (((grupo_etario)::text = ANY ((ARRAY['PRIMERA_INFANCIA'::character varying, 'INFANCIA'::character varying, 'ADOLESCENCIA'::character varying, 'JUVENTUD'::character varying, 'ADULTEZ'::character varying, 'PERSONA_MAYOR'::character varying])::text[])))
);

CREATE TABLE usuarios_grupo_etario_aud (
    cantidad integer NOT NULL,
    revtype smallint,
    rev bigint NOT NULL,
    usuario_id uuid NOT NULL,
    grupo_etario character varying(20) NOT NULL,
    CONSTRAINT usuarios_grupo_etario_aud_grupo_etario_check CHECK (((grupo_etario)::text = ANY ((ARRAY['PRIMERA_INFANCIA'::character varying, 'INFANCIA'::character varying, 'ADOLESCENCIA'::character varying, 'JUVENTUD'::character varying, 'ADULTEZ'::character varying, 'PERSONA_MAYOR'::character varying])::text[])))
);

CREATE TABLE usuarios_sexo (
    cantidad integer NOT NULL,
    sexo character varying(12) NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT usuarios_sexo_sexo_check CHECK (((sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMENINO'::character varying])::text[])))
);

CREATE TABLE usuarios_sexo_aud (
    cantidad integer NOT NULL,
    revtype smallint,
    rev bigint NOT NULL,
    sexo character varying(12) NOT NULL,
    usuario_id uuid NOT NULL,
    CONSTRAINT usuarios_sexo_aud_sexo_check CHECK (((sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMENINO'::character varying])::text[])))
);

ALTER TABLE ONLY abastecimientos_combustible_aud
    ADD CONSTRAINT abastecimientos_combustible_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY abastecimientos_combustible
    ADD CONSTRAINT abastecimientos_combustible_pkey PRIMARY KEY (id);

ALTER TABLE ONLY abastecimientos_combustible
    ADD CONSTRAINT abastecimientos_combustible_soporte_id_key UNIQUE (soporte_id);

ALTER TABLE ONLY adjuntos_aud
    ADD CONSTRAINT adjuntos_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY adjuntos
    ADD CONSTRAINT adjuntos_clave_almacenamiento_key UNIQUE (clave_almacenamiento);

ALTER TABLE ONLY adjuntos
    ADD CONSTRAINT adjuntos_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ayudas_navegacion_aud
    ADD CONSTRAINT ayudas_navegacion_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY ayudas_navegacion
    ADD CONSTRAINT ayudas_navegacion_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY ayudas_navegacion
    ADD CONSTRAINT ayudas_navegacion_pkey PRIMARY KEY (id);

ALTER TABLE ONLY comunicaciones_falla_aud
    ADD CONSTRAINT comunicaciones_falla_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY comunicaciones_falla
    ADD CONSTRAINT comunicaciones_falla_pkey PRIMARY KEY (id);

ALTER TABLE ONLY embarcacion_aud
    ADD CONSTRAINT embarcacion_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY embarcacion
    ADD CONSTRAINT embarcacion_nombre_key UNIQUE (nombre);

ALTER TABLE ONLY embarcacion
    ADD CONSTRAINT embarcacion_patente_key UNIQUE (patente);

ALTER TABLE ONLY embarcacion
    ADD CONSTRAINT embarcacion_pkey PRIMARY KEY (id);

ALTER TABLE ONLY mantenimiento_soportes_aud
    ADD CONSTRAINT mantenimiento_soportes_aud_pkey PRIMARY KEY (rev, adjunto_id, mantenimiento_id);

ALTER TABLE ONLY mantenimientos_aud
    ADD CONSTRAINT mantenimientos_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY mantenimientos
    ADD CONSTRAINT mantenimientos_pkey PRIMARY KEY (id);

ALTER TABLE ONLY motor_aud
    ADD CONSTRAINT motor_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY motor
    ADD CONSTRAINT motor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY motor
    ADD CONSTRAINT motor_serial_key UNIQUE (serial);

ALTER TABLE ONLY operacion_paradas
    ADD CONSTRAINT operacion_paradas_pkey PRIMARY KEY (orden, operacion_id);

ALTER TABLE ONLY operacion_tripulacion_aud
    ADD CONSTRAINT operacion_tripulacion_aud_pkey PRIMARY KEY (rev, operacion_id, personal_id);

ALTER TABLE ONLY operacion_tripulacion
    ADD CONSTRAINT operacion_tripulacion_pkey PRIMARY KEY (operacion_id, personal_id);

ALTER TABLE ONLY operaciones_aud
    ADD CONSTRAINT operaciones_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY operaciones
    ADD CONSTRAINT operaciones_pkey PRIMARY KEY (id);

ALTER TABLE ONLY personal_aud
    ADD CONSTRAINT personal_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY personal
    ADD CONSTRAINT personal_email_key UNIQUE (email);

ALTER TABLE ONLY personal
    ADD CONSTRAINT personal_numero_identificacion_key UNIQUE (numero_identificacion);

ALTER TABLE ONLY personal
    ADD CONSTRAINT personal_pkey PRIMARY KEY (id);

ALTER TABLE ONLY personal
    ADD CONSTRAINT personal_tipo_identificacion_numero_identificacion_key UNIQUE (tipo_identificacion, numero_identificacion);

ALTER TABLE ONLY registro_infraestructura_aspectos_aud
    ADD CONSTRAINT registro_infraestructura_aspectos_aud_pkey PRIMARY KEY (rev, registro_id, aspecto);

ALTER TABLE ONLY registro_infraestructura_aspectos
    ADD CONSTRAINT registro_infraestructura_aspectos_pkey PRIMARY KEY (registro_id, aspecto);

ALTER TABLE ONLY registro_infraestructura_evidencias_aud
    ADD CONSTRAINT registro_infraestructura_evidencias_aud_pkey PRIMARY KEY (rev, adjunto_id, registro_id);

ALTER TABLE ONLY registros_infraestructura_aud
    ADD CONSTRAINT registros_infraestructura_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY registros_infraestructura
    ADD CONSTRAINT registros_infraestructura_pkey PRIMARY KEY (id);

ALTER TABLE ONLY revisiones
    ADD CONSTRAINT revisiones_pkey PRIMARY KEY (id);

ALTER TABLE ONLY suspensiones_embarcacion_aud
    ADD CONSTRAINT suspensiones_embarcacion_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY suspensiones_embarcacion
    ADD CONSTRAINT suspensiones_embarcacion_pkey PRIMARY KEY (id);

ALTER TABLE ONLY usuarios_aud
    ADD CONSTRAINT usuarios_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY usuarios_destino_aud
    ADD CONSTRAINT usuarios_destino_aud_pkey PRIMARY KEY (cantidad, rev, usuario_id, embarcadero_destino);

ALTER TABLE ONLY usuarios_destino
    ADD CONSTRAINT usuarios_destino_pkey PRIMARY KEY (usuario_id, embarcadero_destino);

ALTER TABLE ONLY usuarios_grupo_etario_aud
    ADD CONSTRAINT usuarios_grupo_etario_aud_pkey PRIMARY KEY (cantidad, rev, usuario_id, grupo_etario);

ALTER TABLE ONLY usuarios_grupo_etario
    ADD CONSTRAINT usuarios_grupo_etario_pkey PRIMARY KEY (usuario_id, grupo_etario);

ALTER TABLE ONLY usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);

ALTER TABLE ONLY usuarios_sexo_aud
    ADD CONSTRAINT usuarios_sexo_aud_pkey PRIMARY KEY (cantidad, rev, sexo, usuario_id);

ALTER TABLE ONLY usuarios_sexo
    ADD CONSTRAINT usuarios_sexo_pkey PRIMARY KEY (sexo, usuario_id);

CREATE INDEX ix_abastecimiento_embarcacion_fecha ON abastecimientos_combustible USING btree (embarcacion_id, fecha_hora);

CREATE INDEX ix_comunicacion_referencia ON comunicaciones_falla USING btree (referencia_tipo, referencia_id);

CREATE INDEX ix_mantenimiento_embarcacion_fecha ON mantenimientos USING btree (embarcacion_id, fecha_intervencion);

CREATE INDEX ix_operaciones_inicio ON operaciones USING btree (fecha_hora_inicio);

CREATE INDEX ix_reg_infra_ayuda ON registros_infraestructura USING btree (ayuda_id, fecha_hora);

CREATE INDEX ix_reg_infra_embarcadero ON registros_infraestructura USING btree (embarcadero, fecha_hora);

CREATE INDEX ix_reg_infra_estado ON registros_infraestructura USING btree (estado);

CREATE INDEX ix_suspension_embarcacion_inicio ON suspensiones_embarcacion USING btree (embarcacion_id, fecha_inicio);

ALTER TABLE ONLY operacion_tripulacion_aud
    ADD CONSTRAINT fk1o09r3k8gwickbh30g6a3pgcg FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY operacion_horometros
    ADD CONSTRAINT fk1w84uu8j8q5uxsrfmo7sbvruh FOREIGN KEY (motor_id) REFERENCES motor(id);

ALTER TABLE ONLY registro_infraestructura_evidencias
    ADD CONSTRAINT fk1yruijcpshljvbj4mqa97j0ob FOREIGN KEY (adjunto_id) REFERENCES adjuntos(id);

ALTER TABLE ONLY abastecimientos_combustible_aud
    ADD CONSTRAINT fk28833yfs5ltgwy2s9bmfdk14h FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY motor
    ADD CONSTRAINT fk2pesmw3nmcrofsm643lqr29b FOREIGN KEY (embarcacion_id) REFERENCES embarcacion(id);

ALTER TABLE ONLY operaciones
    ADD CONSTRAINT fk32kw7ncdbiex4a9dos0a0dv4d FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY registros_infraestructura_aud
    ADD CONSTRAINT fk3gr9r9pemaocsujbr653r69lo FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY usuarios_sexo_aud
    ADD CONSTRAINT fk4735alk4cs1qikp1bajcwirmu FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY mantenimientos
    ADD CONSTRAINT fk5fj5bnhj2j6hkt3j54fxmo1s5 FOREIGN KEY (motor_id) REFERENCES motor(id);

ALTER TABLE ONLY usuarios_destino_aud
    ADD CONSTRAINT fk5ic3cs1tbnblbh6yandlj0q5u FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY usuarios_aud
    ADD CONSTRAINT fk5itfy9l58due76jpym6y7907i FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY motor_aud
    ADD CONSTRAINT fk6p6r2cs5gal656hbixbvt8mo1 FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY mantenimientos_aud
    ADD CONSTRAINT fk6pie90h6ph2oh1jr70vcllp5b FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY personal_aud
    ADD CONSTRAINT fk6x7g1nek5kn3k8obkxuqfq3di FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY usuarios_sexo
    ADD CONSTRAINT fk73toa2t0015tn8egxjqwo6nvq FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

ALTER TABLE ONLY abastecimientos_combustible
    ADD CONSTRAINT fk7ckcic8ca3n3oqw4pp5xd4iu2 FOREIGN KEY (embarcacion_id) REFERENCES embarcacion(id);

ALTER TABLE ONLY mantenimiento_novedades
    ADD CONSTRAINT fk7pfc7ckv15bn50wnp6alv1sir FOREIGN KEY (mantenimiento_id) REFERENCES mantenimientos(id);

ALTER TABLE ONLY abastecimientos_combustible
    ADD CONSTRAINT fk8ebkodoln9m511arjc3wd51ie FOREIGN KEY (operacion_id) REFERENCES operaciones(id);

ALTER TABLE ONLY mantenimiento_recomendaciones
    ADD CONSTRAINT fk8gsmo9qdbugjd8yb0yt9f1qkt FOREIGN KEY (mantenimiento_id) REFERENCES mantenimientos(id);

ALTER TABLE ONLY usuarios
    ADD CONSTRAINT fk8wqjv59chd8yvta5sb4mdgksg FOREIGN KEY (operacion_id) REFERENCES operaciones(id);

ALTER TABLE ONLY suspensiones_embarcacion
    ADD CONSTRAINT fk90r51mqe2fvnyoalsuiogt0mj FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY abastecimientos_combustible
    ADD CONSTRAINT fk_abastecimiento_soporte FOREIGN KEY (soporte_id) REFERENCES adjuntos(id);

ALTER TABLE ONLY registros_infraestructura
    ADD CONSTRAINT fkan4ou163usov3jx4y9kxva5bv FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY operacion_tripulacion
    ADD CONSTRAINT fkasqdevs7vrsnttouxb1vmjebl FOREIGN KEY (operacion_id) REFERENCES operaciones(id);

ALTER TABLE ONLY mantenimiento_soportes
    ADD CONSTRAINT fkayvr3ow5v8ha70s3e5tokmnta FOREIGN KEY (adjunto_id) REFERENCES adjuntos(id);

ALTER TABLE ONLY operaciones
    ADD CONSTRAINT fkbciqga1ttpjqvi3aijhtpm6et FOREIGN KEY (embarcacion_id) REFERENCES embarcacion(id);

ALTER TABLE ONLY usuarios_grupo_etario
    ADD CONSTRAINT fkccb57h60fcyq1s80xup6ihdgd FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

ALTER TABLE ONLY registro_infraestructura_aspectos_aud
    ADD CONSTRAINT fkd7x8xbek1pmr3wd0qs37u1eej FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY operaciones_aud
    ADD CONSTRAINT fkeho7f8dvuejatn9g3t68eeuqs FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY mantenimiento_repuestos
    ADD CONSTRAINT fkej9ik5hdii7vbx6ukc2bvvibn FOREIGN KEY (mantenimiento_id) REFERENCES mantenimientos(id);

ALTER TABLE ONLY usuarios
    ADD CONSTRAINT fkfv9s70gkn81xyret9r0e792vr FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY operacion_tripulacion
    ADD CONSTRAINT fkg2chveqieikx7mr277x30njk5 FOREIGN KEY (personal_id) REFERENCES personal(id);

ALTER TABLE ONLY comunicaciones_falla_aud
    ADD CONSTRAINT fkg4xdr8w1u2r7gu9hrdckmso4g FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY mantenimientos
    ADD CONSTRAINT fkg7h7y0cxahnhng12t96p4ctsu FOREIGN KEY (embarcacion_id) REFERENCES embarcacion(id);

ALTER TABLE ONLY registro_infraestructura_aspectos
    ADD CONSTRAINT fkgnhs3ti6rndoklgtb9wh2d0wk FOREIGN KEY (registro_id) REFERENCES registros_infraestructura(id);

ALTER TABLE ONLY registros_infraestructura
    ADD CONSTRAINT fkhjgo5l82ribmi56qkg9nnlyen FOREIGN KEY (ayuda_id) REFERENCES ayudas_navegacion(id);

ALTER TABLE ONLY operacion_paradas
    ADD CONSTRAINT fkhnajhvcxe5vkxgu0bcxl9vkld FOREIGN KEY (operacion_id) REFERENCES operaciones(id);

ALTER TABLE ONLY mantenimientos
    ADD CONSTRAINT fkhvaq812000qk4p2lco28nwbld FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY suspensiones_embarcacion
    ADD CONSTRAINT fki0vxql3wh03ch9vcf69xvyx7h FOREIGN KEY (levantada_por_id) REFERENCES personal(id);

ALTER TABLE ONLY embarcacion_aud
    ADD CONSTRAINT fkiov4apqb523611n3tn6qkqdr5 FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY operacion_horometros
    ADD CONSTRAINT fkiri040r7qqwvmenims9gdj2pt FOREIGN KEY (operacion_id) REFERENCES operaciones(id);

ALTER TABLE ONLY abastecimientos_combustible
    ADD CONSTRAINT fkj49756uen3wihvx206kv97ww8 FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY usuarios_destino
    ADD CONSTRAINT fkjvsc6duq8u9kyilhvg8ggp8tf FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

ALTER TABLE ONLY registros_infraestructura
    ADD CONSTRAINT fkkc39l6mg0dskix9xvt28ca3a9 FOREIGN KEY (comunicado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY adjuntos
    ADD CONSTRAINT fkkh5f99o1u5lhwar0qo3gde7x FOREIGN KEY (subido_por_id) REFERENCES personal(id);

ALTER TABLE ONLY registros_infraestructura
    ADD CONSTRAINT fkmprgdv09xs2rutfekr5ebnm7q FOREIGN KEY (cerrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY suspensiones_embarcacion
    ADD CONSTRAINT fkmy7awwa9241wlpkhu115hxhy4 FOREIGN KEY (embarcacion_id) REFERENCES embarcacion(id);

ALTER TABLE ONLY mantenimiento_soportes_aud
    ADD CONSTRAINT fknq3aeadigprpjy437kruux9p8 FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY suspensiones_embarcacion_aud
    ADD CONSTRAINT fkodh7h58o5rh4q2m04nlnikon3 FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY mantenimiento_soportes
    ADD CONSTRAINT fkoo014qv14oe981gu0x84c8u4k FOREIGN KEY (mantenimiento_id) REFERENCES mantenimientos(id);

ALTER TABLE ONLY mantenimientos
    ADD CONSTRAINT fkp34lnhgrvc60gd95iamqdo4n6 FOREIGN KEY (responsable_id) REFERENCES personal(id);

ALTER TABLE ONLY ayudas_navegacion_aud
    ADD CONSTRAINT fkp3wg551c6vksssxotb34lle8h FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY registro_infraestructura_evidencias_aud
    ADD CONSTRAINT fkpr1brw4qrvlc16ssmy0bm7mvp FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY adjuntos_aud
    ADD CONSTRAINT fkpvw6qqe4b91x2fuw0dgf6aw7w FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY registro_infraestructura_evidencias
    ADD CONSTRAINT fkr4rl965orhpvh5h8yc5i2uwop FOREIGN KEY (registro_id) REFERENCES registros_infraestructura(id);

ALTER TABLE ONLY comunicaciones_falla
    ADD CONSTRAINT fkrat5jw4xcy281qsu8l1hph94m FOREIGN KEY (registrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY usuarios_grupo_etario_aud
    ADD CONSTRAINT fkroaurnfrf1s52q4dckjspvf1a FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY operacion_condiciones_mecanicas
    ADD CONSTRAINT fksfrkcftuydo6ymconsf8njle4 FOREIGN KEY (operacion_id) REFERENCES operaciones(id);
