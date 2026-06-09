-- ============================================================
-- PROCEDURES / FUNCIONES AUXILIARES
-- MathBot - PostgreSQL
-- Ejecutar DESPUÉS de crear tablas y ANTES de triggers.sql
-- ============================================================

-- Función genérica para actualizar columna updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Función para cerrar consultas inactivas después de 30 minutos
-- Útil para ejecutar periódicamente (cron job o pgAgent)
CREATE OR REPLACE FUNCTION cerrar_consultas_inactivas()
RETURNS INTEGER AS $$
DECLARE
    actualizadas INTEGER;
BEGIN
    UPDATE consultas
    SET estado = 'cerrada', fecha_fin = NOW()
    WHERE estado = 'activa'
      AND updated_at < NOW() - INTERVAL '30 minutes';
    
    GET DIAGNOSTICS actualizadas = ROW_COUNT;
    RETURN actualizadas;
END;
$$ LANGUAGE plpgsql;

-- Función para calcular indicadores de progreso de un estudiante
CREATE OR REPLACE FUNCTION calcular_progreso_estudiante(p_estudiante_id UUID)
RETURNS TABLE (
    tipo tipo_indicador,
    valor DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'progreso'::tipo_indicador,
        ROUND(
            COUNT(CASE WHEN ejercicio_resuelto_correctamente = TRUE THEN 1 END) * 100.0 
            / NULLIF(COUNT(*), 0), 
            2
        )
    FROM consultas
    WHERE estudiante_id = p_estudiante_id AND estado = 'cerrada'
    
    UNION ALL
    
    SELECT 
        'autonomia'::tipo_indicador,
        ROUND(
            COUNT(CASE WHEN tipo = 'texto' THEN 1 END) * 100.0 
            / NULLIF(COUNT(*), 0), 
            2
        )
    FROM consultas
    WHERE estudiante_id = p_estudiante_id AND estado = 'cerrada';
END;
$$ LANGUAGE plpgsql;

-- Función para registrar auditoría de acceso al panel docente
CREATE OR REPLACE FUNCTION registrar_acceso_panel()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auditoria_accesos (usuario_id, accion, recurso, exito)
    VALUES (NEW.usuario_id, 'acceso_panel_docente', 'panel_administrativo', TRUE);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;