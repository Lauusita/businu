-- =============================================================================
-- BUSINÚ – Datos iniciales: embarcaderos, catálogos de chequeo y requisitos
-- documentales por cargo. Las embarcaciones y motores NO se precargan: su patente
-- y seriales se registran desde el sistema con los documentos reales.
-- =============================================================================

-- ---------------------------------------------------------------- Embarcaderos (de sur a norte)
INSERT INTO embarcadero (id, codigo, nombre, tipo, orden_ruta, activo) VALUES
    (gen_random_uuid(), 'RANCHO_GRANDE',  'Rancho Grande',            'EMBARCADERO',      1, TRUE),
    (gen_random_uuid(), 'CALLE_22',       'Centro – Calle 22',        'EMBARCADERO',      2, TRUE),
    (gen_random_uuid(), 'CENTRO_VERDE',   'Centro Verde',             'EMBARCADERO',      3, TRUE),
    -- En construcción (anexo §2): se activa cuando entre en operación.
    (gen_random_uuid(), 'UNICORDOBA',     'Universidad de Córdoba',   'ESTACION_ATRAQUE', 4, FALSE);

-- ---------------------------------------------------------------- Repuestos e insumos
-- Motor: orientado al servicio de 100 h de motores fuera de borda Suzuki DF200.
-- Validar contra el manual del propietario del modelo exacto.
INSERT INTO catalogo_item (id, tipo, categoria, nombre, orden) VALUES
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – lubricación',       'Aceite de motor 4T', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – lubricación',       'Filtro de aceite', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – lubricación',       'Aceite de cola (unidad inferior)', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – lubricación',       'Grasa marina', 4),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – lubricación',       'Arandelas / empaques de tapones de drenaje', 5),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – combustible',       'Filtro de combustible', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – combustible',       'Filtro separador de agua', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – combustible',       'Mangueras / abrazaderas de combustible', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – encendido y refrigeración', 'Bujías', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – encendido y refrigeración', 'Impulsor (impeller) de bomba de agua', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – encendido y refrigeración', 'Termostato', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Motor – encendido y refrigeración', 'Empaques / sellos', 4),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Propulsión',                'Hélice', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Propulsión',                'Pasador / tuerca de hélice', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Propulsión',                'Ánodos de sacrificio', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Eléctrico',                 'Batería', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Eléctrico',                 'Fusibles', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Eléctrico',                 'Cableado / terminales', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Eléctrico',                 'Bombillos de luces de navegación', 4),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Casco y cubierta',          'Bomba de achique', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Casco y cubierta',          'Defensas', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Casco y cubierta',          'Cabos de amarre', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Casco y cubierta',          'Pintura / sellante', 4),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Casco y cubierta',          'Asientos / herrajes', 5),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Aire acondicionado',        'Filtro de aire acondicionado', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Aire acondicionado',        'Gas refrigerante', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Aire acondicionado',        'Limpieza de serpentín', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Seguridad',                 'Chalecos salvavidas', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Seguridad',                 'Aros / flotadores salvavidas', 2),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Seguridad',                 'Extintor (recarga)', 3),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Seguridad',                 'Botiquín', 4),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Seguridad',                 'Linterna / señales', 5),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Consumibles',               'Productos de limpieza', 1),
    (gen_random_uuid(), 'REPUESTO_INSUMO', 'Consumibles',               'Trapos / absorbentes', 2);

-- ---------------------------------------------------------------- Novedades
INSERT INTO catalogo_item (id, tipo, categoria, nombre, orden) VALUES
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Ruido anormal del motor', 1),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Sobrecalentamiento', 2),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Vibración', 3),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Fuga de aceite', 4),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Fuga de combustible', 5),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Falla de arranque', 6),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Pérdida de potencia', 7),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Golpe / daño en hélice', 8),
    (gen_random_uuid(), 'NOVEDAD', 'Mecánica',    'Falla de dirección', 9),
    (gen_random_uuid(), 'NOVEDAD', 'Eléctrica',   'Batería descargada', 1),
    (gen_random_uuid(), 'NOVEDAD', 'Eléctrica',   'Falla de luces de navegación', 2),
    (gen_random_uuid(), 'NOVEDAD', 'Eléctrica',   'Falla de instrumentos', 3),
    (gen_random_uuid(), 'NOVEDAD', 'Eléctrica',   'Falla de bomba de achique', 4),
    (gen_random_uuid(), 'NOVEDAD', 'Confort',     'Falla de aire acondicionado', 1),
    (gen_random_uuid(), 'NOVEDAD', 'Confort',     'Daño en asientos', 2),
    (gen_random_uuid(), 'NOVEDAD', 'Casco',       'Ingreso de agua', 1),
    (gen_random_uuid(), 'NOVEDAD', 'Casco',       'Golpe / abolladura', 2),
    (gen_random_uuid(), 'NOVEDAD', 'Casco',       'Desgaste de defensas', 3),
    (gen_random_uuid(), 'NOVEDAD', 'Operacional', 'Palizada / objeto flotante', 1),
    (gen_random_uuid(), 'NOVEDAD', 'Operacional', 'Bajo nivel del río', 2),
    (gen_random_uuid(), 'NOVEDAD', 'Operacional', 'Banco de arena', 3),
    (gen_random_uuid(), 'NOVEDAD', 'Operacional', 'Contacto con muelle', 4),
    (gen_random_uuid(), 'NOVEDAD', 'Operacional', 'Retraso por falla', 5);

-- ---------------------------------------------------------------- Requisitos documentales (§6, §7.3.1)
-- Comunes a todos los cargos
INSERT INTO requisito_documental (id, cargo, tipo_documento, obligatorio, periodicidad)
SELECT gen_random_uuid(), c.cargo, d.tipo, d.obligatorio, d.periodicidad
FROM (VALUES ('SUPERVISOR_OPERATIVO'), ('CONDUCTOR'), ('GUIA_TURISMO'), ('OPERADOR_TAQUILLA')) AS c (cargo)
CROSS JOIN (VALUES
    ('DOCUMENTO_IDENTIDAD',          TRUE,  'UNICA'),
    ('HOJA_VIDA_FUNCION_PUBLICA',    TRUE,  'UNICA'),
    ('CERTIFICADO_FORMACION',        TRUE,  'UNICA'),
    ('CERTIFICADO_EXPERIENCIA',      TRUE,  'UNICA'),
    ('CERTIFICADO_COMPETENCIA',      FALSE, 'POR_VENCIMIENTO'),
    ('AFILIACION_EPS',               TRUE,  'UNICA'),
    ('AFILIACION_PENSION',           TRUE,  'UNICA'),
    ('AFILIACION_ARL',               TRUE,  'UNICA'),
    -- Obligatoria solo con vinculación laboral; el servicio lo resuelve según tipo_vinculacion.
    ('AFILIACION_CAJA_COMPENSACION', FALSE, 'UNICA'),
    ('PLANILLA_PILA',                TRUE,  'MENSUAL')
) AS d (tipo, obligatorio, periodicidad);

-- Específicos por cargo
INSERT INTO requisito_documental (id, cargo, tipo_documento, obligatorio, periodicidad) VALUES
    (gen_random_uuid(), 'SUPERVISOR_OPERATIVO', 'TARJETA_PROFESIONAL', TRUE, 'UNICA'),
    (gen_random_uuid(), 'CONDUCTOR',            'LICENCIA_TRIPULANTE', TRUE, 'POR_VENCIMIENTO');
