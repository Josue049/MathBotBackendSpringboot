-- --------------------------------------------------------
-- 8. TABLAS DE CONFIGURACIÓN Y AUDITORÍA
-- --------------------------------------------------------

CREATE TABLE configuraciones_usuario (
    id              SERIAL PRIMARY KEY,
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    clave           VARCHAR(100) NOT NULL,
    valor           TEXT,
    UNIQUE(usuario_id, clave)
);

CREATE TABLE auditoria_accesos (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    accion          VARCHAR(100) NOT NULL,
    recurso         VARCHAR(200),
    ip_address      INET,
    user_agent      TEXT,
    exito           BOOLEAN NOT NULL DEFAULT TRUE,
    detalle_error   TEXT,
    fecha           TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE notificaciones (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo          VARCHAR(200) NOT NULL,
    mensaje         TEXT NOT NULL,
    tipo            VARCHAR(50) DEFAULT 'info',
    leida           BOOLEAN NOT NULL DEFAULT FALSE,
    url_accion      VARCHAR(500),
    fecha_envio     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_lectura   TIMESTAMP WITH TIME ZONE
);