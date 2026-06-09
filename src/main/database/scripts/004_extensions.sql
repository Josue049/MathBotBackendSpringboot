-- --------------------------------------------------------
-- 5. TABLAS DE CONSULTAS Y CHAT (MVP 1)
-- --------------------------------------------------------

CREATE TABLE consultas (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    titulo          VARCHAR(200),
    tema_id         INTEGER REFERENCES temas_matematicos(id) ON DELETE SET NULL,
    estado          estado_consulta NOT NULL DEFAULT 'activa',
    tipo            tipo_consulta NOT NULL DEFAULT 'texto',
    fecha_inicio    TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_fin       TIMESTAMP WITH TIME ZONE,
    ejercicio_resuelto_correctamente BOOLEAN,
    calificacion_utilidad INTEGER CHECK (calificacion_utilidad BETWEEN 1 AND 5),
    metadata        JSONB DEFAULT '{}',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE mensajes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consulta_id     UUID NOT NULL REFERENCES consultas(id) ON DELETE CASCADE,
    emisor_tipo     tipo_emisor NOT NULL,
    contenido       TEXT NOT NULL,
    tipo_contenido  tipo_contenido_mensaje NOT NULL DEFAULT 'texto',
    orden           INTEGER NOT NULL,
    metadata_ia     JSONB DEFAULT '{}',
    fecha_envio     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    editado         BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_edicion   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE explicaciones_paso (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mensaje_id      UUID NOT NULL REFERENCES mensajes(id) ON DELETE CASCADE,
    numero_paso     INTEGER NOT NULL,
    titulo_paso     VARCHAR(200),
    contenido       TEXT NOT NULL,
    formula_latex   TEXT,
    imagen_url      VARCHAR(500),
    UNIQUE(mensaje_id, numero_paso)
);

CREATE TABLE confirmaciones_comprension (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mensaje_id      UUID NOT NULL REFERENCES mensajes(id) ON DELETE CASCADE,
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    entendio        BOOLEAN NOT NULL,
    estrategia_id   INTEGER REFERENCES estrategias_pedagogicas(id) ON DELETE SET NULL,
    comentario      TEXT,
    fecha_respuesta TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(mensaje_id, estudiante_id)
);