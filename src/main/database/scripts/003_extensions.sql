-- --------------------------------------------------------
-- 4. TABLAS DE USUARIOS Y PERFILES
-- --------------------------------------------------------

CREATE TABLE usuarios (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_usuario  VARCHAR(50) NOT NULL UNIQUE,
    correo_electronico VARCHAR(255) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    telefono        VARCHAR(20),
    rol             rol_usuario NOT NULL DEFAULT 'estudiante',
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    ultimo_acceso   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE perfiles_estudiante (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id      UUID NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    nombres         VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    edad            INTEGER CHECK (edad BETWEEN 5 AND 18),
    grado_escolar_id INTEGER NOT NULL REFERENCES grados_escolares(id) ON DELETE RESTRICT,
    avatar_id       INTEGER REFERENCES avatares(id) ON DELETE SET NULL,
    fecha_nacimiento DATE,
    preferencias    JSONB DEFAULT '{}',
    autonomia_score DECIMAL(5,2) DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE perfiles_docente (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id      UUID NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    nombres         VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    especialidad    VARCHAR(100),
    centro_educativo VARCHAR(200),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE perfiles_padre (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id      UUID NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    nombres         VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE aulas_docente_estudiante (
    id              SERIAL PRIMARY KEY,
    docente_id      UUID NOT NULL REFERENCES perfiles_docente(id) ON DELETE CASCADE,
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    grado_escolar_id INTEGER NOT NULL REFERENCES grados_escolares(id) ON DELETE RESTRICT,
    anio_escolar    INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM NOW()),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_asignacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(docente_id, estudiante_id, anio_escolar)
);

CREATE TABLE tutores_estudiante (
    id              SERIAL PRIMARY KEY,
    padre_id        UUID NOT NULL REFERENCES perfiles_padre(id) ON DELETE CASCADE,
    estudiante_id   UUID NOT NULL REFERENCES perfiles_estudiante(id) ON DELETE CASCADE,
    tipo_tutoria    VARCHAR(50) DEFAULT 'padre',
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(padre_id, estudiante_id)
);