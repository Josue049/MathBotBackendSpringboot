-- ============================================================
-- PROCEDURES / TRIGGERS
-- MathBot - PostgreSQL
-- Ejecutar DESPUÉS de functions.sql
-- ============================================================

-- Triggers para updated_at automático en tablas de perfil
CREATE TRIGGER trg_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_perfiles_estudiante_updated_at
    BEFORE UPDATE ON perfiles_estudiante
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_perfiles_docente_updated_at
    BEFORE UPDATE ON perfiles_docente
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_perfiles_padre_updated_at
    BEFORE UPDATE ON perfiles_padre
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_consultas_updated_at
    BEFORE UPDATE ON consultas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger para registrar acceso al panel docente
-- Se dispara cuando un docente inicia sesión (actualiza ultimo_acceso)
CREATE TRIGGER trg_auditoria_docente_login
    AFTER UPDATE OF ultimo_acceso ON usuarios
    FOR EACH ROW
    WHEN (NEW.rol = 'docente')
    EXECUTE FUNCTION registrar_acceso_panel();