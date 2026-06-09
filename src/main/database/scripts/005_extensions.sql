-- --------------------------------------------------------
-- 6. TABLAS DE ANÁLISIS DE IMÁGENES (MVP 2)
-- --------------------------------------------------------

CREATE TABLE imagenes_ejercicio (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consulta_id     UUID NOT NULL REFERENCES consultas(id) ON DELETE CASCADE,
    mensaje_id      UUID REFERENCES mensajes(id) ON DELETE SET NULL,
    url_imagen      VARCHAR(500) NOT NULL,
    url_imagen_miniatura VARCHAR(500),
    formato         VARCHAR(10) NOT NULL,
    tamano_bytes    INTEGER,
    estado_procesamiento estado_procesamiento_imagen NOT NULL DEFAULT 'pendiente',
    texto_reconocido_ocr TEXT,
    ejercicio_detectado TEXT,
    respuesta_estudiante TEXT,
    resultado_correcto TEXT,
    metadata_ia     JSONB DEFAULT '{}',
    fecha_carga     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_procesamiento TIMESTAMP WITH TIME ZONE
);

CREATE TABLE errores_detectados (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    imagen_id       UUID NOT NULL REFERENCES imagenes_ejercicio(id) ON DELETE CASCADE,
    tipo_error      tipo_error_matematico NOT NULL,
    severidad       severidad_error NOT NULL DEFAULT 'moderado',
    descripcion     TEXT NOT NULL,
    paso_afectado   INTEGER,
    sugerencia_correccion TEXT,
    explicacion_generada TEXT,
    fecha_deteccion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);