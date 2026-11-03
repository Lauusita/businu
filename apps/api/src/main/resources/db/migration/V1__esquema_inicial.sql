-- =============================================================================
-- BUSINÚ – Esquema inicial
-- 4.3 Hoja de vida de las embarcaciones · 4.2 Registro de pasajeros · 4.6 Personal
-- Convención de auditoría: todas las tablas principales llevan id UUID, version
-- y creado/actualizado (en, por). Las tablas de detalle solo llevan id.
-- =============================================================================

-- ---------------------------------------------------------------- Catálogos
CREATE TABLE embarcadero (
    id              UUID PRIMARY KEY,
    codigo          VARCHAR(30)  NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    tipo            VARCHAR(20)  NOT NULL CHECK (tipo IN ('EMBARCADERO', 'ESTACION_ATRAQUE')),
    latitud         NUMERIC(9, 6),
    longitud        NUMERIC(9, 6),
    orden_ruta      INT          NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por      VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'sistema'
);

CREATE TABLE catalogo_item (
    id              UUID PRIMARY KEY,
    tipo            VARCHAR(20)  NOT NULL CHECK (tipo IN ('REPUESTO_INSUMO', 'NOVEDAD')),
    categoria       VARCHAR(60)  NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    orden           INT          NOT NULL DEFAULT 0,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por      VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'sistema',
    UNIQUE (tipo, nombre)
);

-- ---------------------------------------------------------------- Personal (4.6)
CREATE TABLE personal (
    id                    UUID PRIMARY KEY,
    tipo_identificacion   VARCHAR(10)  NOT NULL CHECK (tipo_identificacion IN ('CC', 'CE', 'PPT', 'PASAPORTE')),
    numero_identificacion VARCHAR(20)  NOT NULL,
    nombres               VARCHAR(80)  NOT NULL,
    apellidos             VARCHAR(80)  NOT NULL,
    telefono              VARCHAR(20),
    email                 VARCHAR(120),
    cargo                 VARCHAR(30)  NOT NULL
        CHECK (cargo IN ('SUPERVISOR_OPERATIVO', 'CONDUCTOR', 'GUIA_TURISMO', 'OPERADOR_TAQUILLA')),
    tipo_vinculacion      VARCHAR(30)  NOT NULL CHECK (tipo_vinculacion IN ('LABORAL', 'PRESTACION_SERVICIOS')),
    fecha_vinculacion     DATE         NOT NULL,
    fecha_retiro          DATE,
    reemplaza_a_id        UUID REFERENCES personal (id),
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    version               BIGINT       NOT NULL DEFAULT 0,
    creado_en             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por            VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por       VARCHAR(100) NOT NULL DEFAULT 'sistema',
    UNIQUE (tipo_identificacion, numero_identificacion),
    CHECK (fecha_retiro IS NULL OR fecha_retiro >= fecha_vinculacion)
);

CREATE TABLE usuario_sistema (
    id              UUID PRIMARY KEY,
    personal_id     UUID         NOT NULL UNIQUE REFERENCES personal (id),
    username        VARCHAR(60)  NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('SUPERVISOR', 'TAQUILLERO', 'PILOTO')),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    ultimo_acceso   TIMESTAMPTZ,
    version         BIGINT       NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por      VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'sistema'
);

CREATE TABLE documento_personal (
    id                UUID PRIMARY KEY,
    personal_id       UUID         NOT NULL REFERENCES personal (id),
    tipo              VARCHAR(40)  NOT NULL CHECK (tipo IN (
                          'DOCUMENTO_IDENTIDAD', 'HOJA_VIDA_FUNCION_PUBLICA', 'CERTIFICADO_FORMACION',
                          'CERTIFICADO_EXPERIENCIA', 'TARJETA_PROFESIONAL', 'LICENCIA_TRIPULANTE',
                          'CERTIFICADO_COMPETENCIA', 'AFILIACION_EPS', 'AFILIACION_PENSION', 'AFILIACION_ARL',
                          'AFILIACION_CAJA_COMPENSACION', 'PLANILLA_PILA')),
    numero            VARCHAR(60),
    entidad_emisora   VARCHAR(120),
    fecha_expedicion  DATE,
    fecha_vencimiento DATE,
    periodo           VARCHAR(7) CHECK (periodo ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    adjunto_id        UUID,
    verificado_por_id UUID REFERENCES usuario_sistema (id),
    verificado_en     TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0,
    creado_en         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por        VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por   VARCHAR(100) NOT NULL DEFAULT 'sistema',
    CHECK (tipo <> 'PLANILLA_PILA' OR periodo IS NOT NULL)
);
CREATE INDEX ix_documento_personal_personal ON documento_personal (personal_id);
CREATE INDEX ix_documento_personal_vencimiento ON documento_personal (fecha_vencimiento);

CREATE TABLE requisito_documental (
    id              UUID PRIMARY KEY,
    cargo           VARCHAR(30)  NOT NULL
        CHECK (cargo IN ('SUPERVISOR_OPERATIVO', 'CONDUCTOR', 'GUIA_TURISMO', 'OPERADOR_TAQUILLA')),
    tipo_documento  VARCHAR(40)  NOT NULL,
    obligatorio     BOOLEAN      NOT NULL DEFAULT TRUE,
    periodicidad    VARCHAR(20)  NOT NULL CHECK (periodicidad IN ('UNICA', 'MENSUAL', 'POR_VENCIMIENTO')),
    version         BIGINT       NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por      VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'sistema',
    UNIQUE (cargo, tipo_documento)
);

-- ---------------------------------------------------------------- Flota
CREATE TABLE embarcacion (
    id                  UUID PRIMARY KEY,
    patente             VARCHAR(40)  NOT NULL UNIQUE,
    nombre              VARCHAR(60)  NOT NULL UNIQUE,
    capacidad_pasajeros INT          NOT NULL CHECK (capacidad_pasajeros > 0),
    constructor         VARCHAR(100),
    anio_construccion   INT,
    estado              VARCHAR(20)  NOT NULL CHECK (estado IN ('OPERATIVA', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO')),
    horas_operacion_dia NUMERIC(4, 1) NOT NULL DEFAULT 6 CHECK (horas_operacion_dia > 0),
    version             BIGINT       NOT NULL DEFAULT 0,
    creado_en           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por          VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por     VARCHAR(100) NOT NULL DEFAULT 'sistema'
);

CREATE TABLE motor (
    id                       UUID PRIMARY KEY,
    embarcacion_id           UUID          NOT NULL REFERENCES embarcacion (id),
    posicion                 VARCHAR(10)   NOT NULL CHECK (posicion IN ('BABOR', 'ESTRIBOR')),
    marca                    VARCHAR(40)   NOT NULL,
    modelo                   VARCHAR(60),
    serial                   VARCHAR(60)   NOT NULL UNIQUE,
    potencia_hp              INT           NOT NULL CHECK (potencia_hp > 0),
    horometro_actual         NUMERIC(10, 1) NOT NULL DEFAULT 0 CHECK (horometro_actual >= 0),
    intervalo_servicio_horas INT           NOT NULL DEFAULT 100 CHECK (intervalo_servicio_horas > 0),
    version                  BIGINT        NOT NULL DEFAULT 0,
    creado_en                TIMESTAMPTZ   NOT NULL DEFAULT now(),
    creado_por               VARCHAR(100)  NOT NULL DEFAULT 'sistema',
    actualizado_en           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    actualizado_por          VARCHAR(100)  NOT NULL DEFAULT 'sistema',
    UNIQUE (embarcacion_id, posicion)
);

-- ---------------------------------------------------------------- Hoja de vida (4.3)
CREATE TABLE mantenimiento (
    id                      UUID PRIMARY KEY,
    embarcacion_id          UUID          NOT NULL REFERENCES embarcacion (id),
    tipo                    VARCHAR(20)   NOT NULL CHECK (tipo IN ('PREVENTIVO', 'CORRECTIVO')),
    sistema                 VARCHAR(30)   NOT NULL CHECK (sistema IN (
                                'MOTOR', 'CASCO', 'ELECTRICO', 'AIRE_ACONDICIONADO', 'SEGURIDAD', 'OTRO')),
    fecha_intervencion      DATE          NOT NULL,
    descripcion_actividades TEXT          NOT NULL CHECK (length(trim(descripcion_actividades)) > 0),
    ejecutado_por           VARCHAR(120),
    responsable_id          UUID          NOT NULL REFERENCES personal (id),
    costo_mano_obra         NUMERIC(14, 2) CHECK (costo_mano_obra >= 0),
    sin_repuestos           BOOLEAN       NOT NULL,
    sin_novedades           BOOLEAN       NOT NULL,
    version                 BIGINT        NOT NULL DEFAULT 0,
    creado_en               TIMESTAMPTZ   NOT NULL DEFAULT now(),
    creado_por              VARCHAR(100)  NOT NULL DEFAULT 'sistema',
    actualizado_en          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    actualizado_por         VARCHAR(100)  NOT NULL DEFAULT 'sistema'
);
CREATE INDEX ix_mantenimiento_embarcacion_fecha ON mantenimiento (embarcacion_id, fecha_intervencion DESC);

CREATE TABLE mantenimiento_motor (
    id                 UUID PRIMARY KEY,
    mantenimiento_id   UUID           NOT NULL REFERENCES mantenimiento (id) ON DELETE CASCADE,
    motor_id           UUID           NOT NULL REFERENCES motor (id),
    horometro          NUMERIC(10, 1) NOT NULL CHECK (horometro >= 0),
    servicio_periodico BOOLEAN        NOT NULL,
    proximo_horometro  NUMERIC(10, 1),
    UNIQUE (mantenimiento_id, motor_id),
    CHECK (servicio_periodico = (proximo_horometro IS NOT NULL)),
    CHECK (proximo_horometro IS NULL OR proximo_horometro > horometro)
);
CREATE INDEX ix_mantenimiento_motor_servicio ON mantenimiento_motor (motor_id, horometro DESC) WHERE servicio_periodico;

CREATE TABLE mantenimiento_repuesto (
    id               UUID PRIMARY KEY,
    mantenimiento_id UUID           NOT NULL REFERENCES mantenimiento (id) ON DELETE CASCADE,
    item_id          UUID REFERENCES catalogo_item (id),
    otro_descripcion VARCHAR(200),
    cantidad         NUMERIC(10, 2) NOT NULL CHECK (cantidad > 0),
    unidad           VARCHAR(20)    NOT NULL,
    costo            NUMERIC(14, 2) CHECK (costo >= 0),
    CHECK ((item_id IS NULL) <> (otro_descripcion IS NULL))
);
CREATE INDEX ix_mantenimiento_repuesto_mant ON mantenimiento_repuesto (mantenimiento_id);

CREATE TABLE mantenimiento_novedad (
    id               UUID PRIMARY KEY,
    mantenimiento_id UUID        NOT NULL REFERENCES mantenimiento (id) ON DELETE CASCADE,
    item_id          UUID REFERENCES catalogo_item (id),
    otro_descripcion VARCHAR(200),
    severidad        VARCHAR(10) NOT NULL CHECK (severidad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    detalle          TEXT,
    CHECK ((item_id IS NULL) <> (otro_descripcion IS NULL))
);
CREATE INDEX ix_mantenimiento_novedad_mant ON mantenimiento_novedad (mantenimiento_id);

CREATE TABLE mantenimiento_recomendacion (
    id               UUID PRIMARY KEY,
    mantenimiento_id UUID        NOT NULL REFERENCES mantenimiento (id) ON DELETE CASCADE,
    tipo             VARCHAR(20) NOT NULL CHECK (tipo IN ('CORRECTIVA', 'PREVENTIVA', 'PREDICTIVA')),
    descripcion      TEXT        NOT NULL CHECK (length(trim(descripcion)) > 0),
    UNIQUE (mantenimiento_id, tipo)
);

-- ---------------------------------------------------------------- Pasajeros (4.2)
CREATE TABLE registro_pasajeros (
    id                     UUID PRIMARY KEY,           -- generado en el dispositivo (offline)
    fecha_hora             TIMESTAMPTZ  NOT NULL,      -- hora del dispositivo; la del servidor es creado_en
    viaje_id               UUID,                       -- FK a viaje cuando exista la bitácora (4.1)
    embarcadero_origen_id  UUID         NOT NULL REFERENCES embarcadero (id),
    embarcadero_destino_id UUID         NOT NULL REFERENCES embarcadero (id),
    registrado_por_id      UUID         NOT NULL REFERENCES usuario_sistema (id),
    total                  INT          NOT NULL CHECK (total > 0),
    version                BIGINT       NOT NULL DEFAULT 0,
    creado_en              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por             VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por        VARCHAR(100) NOT NULL DEFAULT 'sistema',
    CHECK (embarcadero_origen_id <> embarcadero_destino_id)
);
CREATE INDEX ix_registro_pasajeros_fecha ON registro_pasajeros (fecha_hora);
CREATE INDEX ix_registro_pasajeros_viaje ON registro_pasajeros (viaje_id);

CREATE TABLE registro_pasajeros_detalle (
    id           UUID PRIMARY KEY,
    registro_id  UUID        NOT NULL REFERENCES registro_pasajeros (id) ON DELETE CASCADE,
    grupo_etario VARCHAR(20) NOT NULL CHECK (grupo_etario IN (
                     'PRIMERA_INFANCIA', 'INFANCIA', 'ADOLESCENCIA', 'JUVENTUD', 'ADULTEZ', 'PERSONA_MAYOR')),
    sexo         VARCHAR(12) NOT NULL CHECK (sexo IN ('HOMBRE', 'MUJER', 'NO_REPORTA')),
    cantidad     INT         NOT NULL CHECK (cantidad > 0),
    UNIQUE (registro_id, grupo_etario, sexo)
);
