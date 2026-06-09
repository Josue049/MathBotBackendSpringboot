-- --------------------------------------------------------
-- 3. TABLAS DE CATÁLOGO / REFERENCIA
-- --------------------------------------------------------

CREATE TABLE grados_escolares (
    id              SERIAL PRIMARY KEY,
    nombre          VARCHAR(50) NOT NULL UNIQUE,
    nivel           VARCHAR(20) NOT NULL DEFAULT 'primaria',
    orden           INTEGER NOT NULL UNIQUE,
    descripcion     TEXT,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE avatares (
    id              SERIAL PRIMARY KEY,
    codigo          CHAR(1) NOT NULL UNIQUE,
    nombre          VARCHAR(50) NOT NULL,
    url_imagen      VARCHAR(500),
    color_hex       CHAR(7),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE temas_matematicos (
    id              SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     TEXT,
    grado_escolar_id INTEGER NOT NULL REFERENCES grados_escolares(id) ON DELETE RESTRICT,
    categoria       VARCHAR(50),
    tags            VARCHAR(50)[],
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE estrategias_pedagogicas (
    id              SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     TEXT,
    tipo_aprendizaje VARCHAR(50),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);