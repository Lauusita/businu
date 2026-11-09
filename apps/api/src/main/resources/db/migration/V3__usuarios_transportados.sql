-- =============================================================================
-- 4.2 Usuarios transportados: se reemplaza la cuadrícula grupo etario × sexo por
-- un total y dos distribuciones independientes (por sexo y por grupo etario),
-- y se agrega la embarcación de la operación.
-- =============================================================================
DROP TABLE registro_pasajeros_detalle;
DROP TABLE registro_pasajeros;

CREATE TABLE usuarios (
    id                     UUID PRIMARY KEY,           -- generado en el dispositivo (offline)
    fecha_hora             TIMESTAMPTZ  NOT NULL,      -- hora del dispositivo; la del servidor es creado_en
    viaje_id               UUID,                       -- FK a viaje cuando exista la bitácora (4.1)
    embarcacion_id         UUID         NOT NULL REFERENCES embarcacion (id),
    embarcadero_origen_id  UUID         NOT NULL REFERENCES embarcadero (id),
    embarcadero_destino_id UUID         NOT NULL REFERENCES embarcadero (id),
    usuarios_transportados INT          NOT NULL CHECK (usuarios_transportados > 0),
    registrado_por_id      UUID         NOT NULL REFERENCES usuario_sistema (id),
    version                BIGINT       NOT NULL DEFAULT 0,
    creado_en              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    creado_por             VARCHAR(100) NOT NULL DEFAULT 'sistema',
    actualizado_en         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_por        VARCHAR(100) NOT NULL DEFAULT 'sistema',
    CHECK (embarcadero_origen_id <> embarcadero_destino_id)
);
CREATE INDEX ix_usuarios_fecha ON usuarios (fecha_hora);
CREATE INDEX ix_usuarios_viaje ON usuarios (viaje_id);
CREATE INDEX ix_usuarios_embarcacion ON usuarios (embarcacion_id);

CREATE TABLE usuarios_sexo (
    usuario_id UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    sexo       VARCHAR(12) NOT NULL CHECK (sexo IN ('HOMBRE', 'MUJER', 'NO_REPORTA')),
    cantidad   INT         NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (usuario_id, sexo)
);

CREATE TABLE usuarios_grupo_etario (
    usuario_id   UUID        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    grupo_etario VARCHAR(20) NOT NULL CHECK (grupo_etario IN (
                     'PRIMERA_INFANCIA', 'INFANCIA', 'ADOLESCENCIA', 'JUVENTUD', 'ADULTEZ', 'PERSONA_MAYOR')),
    cantidad     INT         NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (usuario_id, grupo_etario)
);
