-- --------------------------------------------------------
-- 7. TABLAS DE PROGRESO Y ESTADÍSTICAS (MVP 3)
-- --------------------------------------------------------

CREATE TABLE indicadores_progreso (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    tipo_indicador  tipo_indicador NOT NULL,
    valor           DECIMAL(5,2) NOT NULL CHECK (valor BETWEEN 0 AND 100),
    periodo_inicio  DATE NOT NULL,
    periodo_fin     DATE NOT NULL,
    detalles        JSONB DEFAULT '{}',
    fecha_calculo   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(estudiante_id, tipo_indicador, periodo_inicio)
);

CREATE TABLE actividades_refuerzo (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    docente_id      UUID NOT NULL REFERENCES perfiles_docente(id) ON DELETE CASCADE,
    titulo          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    temas_ids       INTEGER[] NOT NULL,
    grados_ids      INTEGER[] NOT NULL,
    dificultades_detectadas TEXT,
    archivo_url     VARCHAR(500),
    archivo_nombre  VARCHAR(255),
    estado_generacion VARCHAR(20) DEFAULT 'completado',
    fecha_generacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE actividades_asignadas (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actividad_id    UUID NOT NULL REFERENCES actividades_refuerzo(id) ON DELETE CASCADE,
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    estado          estado_actividad NOT NULL DEFAULT 'pendiente',
    fecha_asignacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_completado TIMESTAMP WITH TIME ZONE,
    calificacion    DECIMAL(4,2),
    observaciones   TEXT,
    UNIQUE(actividad_id, estudiante_id)
);

CREATE TABLE reportes_pdf (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    padre_id        UUID NOT NULL REFERENCES perfiles_padre(id) ON DELETE CASCADE,
    periodo_inicio  DATE NOT NULL,
    periodo_fin     DATE NOT NULL,
    url_pdf         VARCHAR(500) NOT NULL,
    tamano_bytes    INTEGER,
    estado          estado_reporte NOT NULL DEFAULT 'generando',
    metricas_resumen JSONB DEFAULT '{}',
    fecha_solicitud TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_generacion TIMESTAMP WITH TIME ZONE,
    fecha_descarga  TIMESTAMP WITH TIME ZONE
);