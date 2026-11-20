CREATE TABLE token_recuperacion (
    id          uuid                        NOT NULL,
    personal_id uuid                        NOT NULL,
    token_hash  character varying(64)       NOT NULL,
    creado_en   timestamp(6) with time zone NOT NULL,
    expira_en   timestamp(6) with time zone NOT NULL,
    usado_en    timestamp(6) with time zone,
    CONSTRAINT token_recuperacion_pkey PRIMARY KEY (id),
    CONSTRAINT token_recuperacion_token_hash_key UNIQUE (token_hash),
    CONSTRAINT token_recuperacion_personal_fk FOREIGN KEY (personal_id) REFERENCES personal (id)
);

CREATE INDEX token_recuperacion_personal_idx ON token_recuperacion (personal_id);
