-- =====================================================================================
-- BUSINÚ · V2 · Fase §4.6 a §4.8
--   documentos_personal  : documentación del personal (§4.6)
--   pqrs                 : PQRS del formulario público (§4.7) + secuencia del radicado
--   informes_mensuales   : informe mensual de operación (§4.7)
--   *_aud                : historial de cambios (Envers, R5)
-- =====================================================================================

CREATE SEQUENCE pqrs_consecutivo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE documentos_personal (
    periodo date,
    vigente boolean NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    adjunto_id uuid NOT NULL,
    creado_por uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    personal_id uuid NOT NULL,
    registrado_por_id uuid NOT NULL,
    tipo character varying(40) NOT NULL,
    observaciones text NOT NULL,
    CONSTRAINT documentos_personal_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CEDULA'::character varying, 'RUT'::character varying, 'HOJA_VIDA_FUNCION_PUBLICA'::character varying, 'CERTIFICADO_ACADEMICO'::character varying, 'CERTIFICADO_EXPERIENCIA'::character varying, 'LICENCIA_TRIPULANTE'::character varying, 'CERTIFICADO_COMPETENCIA'::character varying, 'AFILIACION_SEGURIDAD_SOCIAL'::character varying, 'PLANILLA_PILA'::character varying, 'OTRO'::character varying])::text[])))
);

CREATE TABLE documentos_personal_aud (
    periodo date,
    revtype smallint,
    vigente boolean,
    rev bigint NOT NULL,
    adjunto_id uuid,
    id uuid NOT NULL,
    personal_id uuid,
    registrado_por_id uuid,
    tipo character varying(40),
    observaciones text,
    CONSTRAINT documentos_personal_aud_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CEDULA'::character varying, 'RUT'::character varying, 'HOJA_VIDA_FUNCION_PUBLICA'::character varying, 'CERTIFICADO_ACADEMICO'::character varying, 'CERTIFICADO_EXPERIENCIA'::character varying, 'LICENCIA_TRIPULANTE'::character varying, 'CERTIFICADO_COMPETENCIA'::character varying, 'AFILIACION_SEGURIDAD_SOCIAL'::character varying, 'PLANILLA_PILA'::character varying, 'OTRO'::character varying])::text[])))
);

CREATE TABLE informes_mensuales (
    periodo date NOT NULL,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_cierre timestamp(6) with time zone,
    fecha_entrega timestamp(6) with time zone,
    version bigint NOT NULL,
    estado character varying(12) NOT NULL,
    cerrado_por_id uuid,
    creado_por uuid,
    entregado_por_id uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    huella_datos character varying(64),
    conclusiones text NOT NULL,
    datos_congelados text,
    otros_documentos text NOT NULL,
    recomendaciones text NOT NULL,
    resumen_actividades text NOT NULL,
    CONSTRAINT informes_mensuales_estado_check CHECK (((estado)::text = ANY ((ARRAY['BORRADOR'::character varying, 'CERRADO'::character varying, 'ENTREGADO'::character varying])::text[])))
);

CREATE TABLE informes_mensuales_aud (
    periodo date,
    revtype smallint,
    fecha_cierre timestamp(6) with time zone,
    fecha_entrega timestamp(6) with time zone,
    rev bigint NOT NULL,
    estado character varying(12),
    cerrado_por_id uuid,
    entregado_por_id uuid,
    id uuid NOT NULL,
    huella_datos character varying(64),
    conclusiones text,
    datos_congelados text,
    otros_documentos text,
    recomendaciones text,
    resumen_actividades text,
    CONSTRAINT informes_mensuales_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['BORRADOR'::character varying, 'CERRADO'::character varying, 'ENTREGADO'::character varying])::text[])))
);

CREATE TABLE pqrs (
    autoriza_datos boolean NOT NULL,
    fecha_hecho date,
    actualizado_en timestamp(6) with time zone NOT NULL,
    creado_en timestamp(6) with time zone NOT NULL,
    fecha_respuesta timestamp(6) with time zone,
    version bigint NOT NULL,
    estado character varying(15) NOT NULL,
    tipo character varying(15) NOT NULL,
    creado_por uuid,
    gestionado_por_id uuid,
    id uuid NOT NULL,
    modificado_por uuid,
    radicado character varying(20) NOT NULL,
    embarcadero character varying(30),
    telefono character varying(30),
    correo character varying(150),
    nombre character varying(150),
    entidad_traslado character varying(200),
    descripcion text NOT NULL,
    observacion_interna text,
    respuesta text,
    CONSTRAINT pqrs_embarcadero_check CHECK (((embarcadero)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT pqrs_estado_check CHECK (((estado)::text = ANY ((ARRAY['RECIBIDA'::character varying, 'EN_GESTION'::character varying, 'RESPONDIDA'::character varying, 'TRASLADADA'::character varying, 'CERRADA'::character varying])::text[]))),
    CONSTRAINT pqrs_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PETICION'::character varying, 'QUEJA'::character varying, 'RECLAMO'::character varying, 'SUGERENCIA'::character varying, 'FELICITACION'::character varying])::text[])))
);

CREATE TABLE pqrs_aud (
    autoriza_datos boolean,
    fecha_hecho date,
    revtype smallint,
    fecha_respuesta timestamp(6) with time zone,
    rev bigint NOT NULL,
    estado character varying(15),
    tipo character varying(15),
    gestionado_por_id uuid,
    id uuid NOT NULL,
    radicado character varying(20),
    embarcadero character varying(30),
    telefono character varying(30),
    correo character varying(150),
    nombre character varying(150),
    entidad_traslado character varying(200),
    descripcion text,
    observacion_interna text,
    respuesta text,
    CONSTRAINT pqrs_aud_embarcadero_check CHECK (((embarcadero)::text = ANY ((ARRAY['RANCHO_GRANDE'::character varying, 'CENTRO_CALLE_22'::character varying, 'CENTRO_VERDE'::character varying])::text[]))),
    CONSTRAINT pqrs_aud_estado_check CHECK (((estado)::text = ANY ((ARRAY['RECIBIDA'::character varying, 'EN_GESTION'::character varying, 'RESPONDIDA'::character varying, 'TRASLADADA'::character varying, 'CERRADA'::character varying])::text[]))),
    CONSTRAINT pqrs_aud_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PETICION'::character varying, 'QUEJA'::character varying, 'RECLAMO'::character varying, 'SUGERENCIA'::character varying, 'FELICITACION'::character varying])::text[])))
);

ALTER TABLE ONLY documentos_personal
    ADD CONSTRAINT documentos_personal_adjunto_id_key UNIQUE (adjunto_id);

ALTER TABLE ONLY documentos_personal_aud
    ADD CONSTRAINT documentos_personal_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY documentos_personal
    ADD CONSTRAINT documentos_personal_pkey PRIMARY KEY (id);

ALTER TABLE ONLY informes_mensuales_aud
    ADD CONSTRAINT informes_mensuales_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY informes_mensuales
    ADD CONSTRAINT informes_mensuales_periodo_key UNIQUE (periodo);

ALTER TABLE ONLY informes_mensuales
    ADD CONSTRAINT informes_mensuales_pkey PRIMARY KEY (id);

ALTER TABLE ONLY pqrs_aud
    ADD CONSTRAINT pqrs_aud_pkey PRIMARY KEY (rev, id);

ALTER TABLE ONLY pqrs
    ADD CONSTRAINT pqrs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY pqrs
    ADD CONSTRAINT pqrs_radicado_key UNIQUE (radicado);

CREATE INDEX ix_documento_personal_tipo ON documentos_personal USING btree (personal_id, tipo, periodo);

CREATE INDEX ix_pqrs_estado_fecha ON pqrs USING btree (estado, creado_en);

ALTER TABLE ONLY pqrs_aud
    ADD CONSTRAINT fk10ngtp8insua881eqap6jkwy FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY pqrs
    ADD CONSTRAINT fk3am0hqembwy0nqia2230o95w2 FOREIGN KEY (gestionado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY informes_mensuales_aud
    ADD CONSTRAINT fk8wfom1d4jqlps7yt36qfqq7ts FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY informes_mensuales
    ADD CONSTRAINT fka8fhavhvip0fk5cs6vlc7p0xo FOREIGN KEY (cerrado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY documentos_personal_aud
    ADD CONSTRAINT fkcw0uxpcyq1mpr4i3y8lit7eqg FOREIGN KEY (rev) REFERENCES revisiones(id);

ALTER TABLE ONLY documentos_personal
    ADD CONSTRAINT fkfxsw574py4t4icr83kvv30vus FOREIGN KEY (adjunto_id) REFERENCES adjuntos(id);

ALTER TABLE ONLY documentos_personal
    ADD CONSTRAINT fkk5rs09ma3kv4p08n3n6c2cahn FOREIGN KEY (personal_id) REFERENCES personal(id);

ALTER TABLE ONLY informes_mensuales
    ADD CONSTRAINT fklwwjgajkbj8jt3yd0ptrxe3f FOREIGN KEY (entregado_por_id) REFERENCES personal(id);

ALTER TABLE ONLY documentos_personal
    ADD CONSTRAINT fko1alg8wi02f29i9oy5jr6o083 FOREIGN KEY (registrado_por_id) REFERENCES personal(id);
