-- --------------------------------------------------------
-- 9. VISTAS Y MATERIALIZED VIEWS (PARA PANEL DOCENTE)
-- --------------------------------------------------------

CREATE VIEW v_resumen_estudiante AS
SELECT 
    pe.id AS estudiante_id,
    pe.nombres || ' ' || pe.apellido_paterno AS nombre_completo,
    ge.nombre AS grado_escolar,
    pe.avatar_id,
    COUNT(DISTINCT c.id) AS total_consultas,
    COUNT(DISTINCT CASE WHEN c.tipo = 'imagen' THEN c.id END) AS consultas_con_imagen,
    COUNT(DISTINCT CASE WHEN c.ejercicio_resuelto_correctamente = TRUE THEN c.id END) AS ejercicios_correctos,
    MAX(c.fecha_inicio) AS ultima_consulta,
    MAX(ip.valor) FILTER (WHERE ip.tipo_indicador = 'progreso') AS progreso_actual,
    MAX(ip.valor) FILTER (WHERE ip.tipo_indicador = 'autonomia') AS autonomia_actual
FROM perfiles_estudiante pe
JOIN grados_escolares ge ON pe.grado_escolar_id = ge.id
LEFT JOIN consultas c ON pe.id = c.estudiante_id AND c.estado = 'cerrada'
LEFT JOIN indicadores_progreso ip ON pe.id = ip.estudiante_id
GROUP BY pe.id, pe.nombres, pe.apellido_paterno, ge.nombre, pe.avatar_id;

CREATE VIEW v_estadisticas_tema_grado AS
SELECT 
    tm.id AS tema_id,
    tm.nombre AS tema_nombre,
    tm.categoria,
    ge.id AS grado_id,
    ge.nombre AS grado_nombre,
    COUNT(c.id) AS total_consultas,
    COUNT(CASE WHEN c.ejercicio_resuelto_correctamente = TRUE THEN 1 END) AS resueltos_correctamente,
    ROUND(
        COUNT(CASE WHEN c.ejercicio_resuelto_correctamente = TRUE THEN 1 END) * 100.0 / NULLIF(COUNT(c.id), 0), 
        2
    ) AS tasa_exito,
    COUNT(DISTINCT c.estudiante_id) AS estudiantes_unicos,
    MAX(c.fecha_inicio) AS ultima_consulta
FROM temas_matematicos tm
JOIN grados_escolares ge ON tm.grado_escolar_id = ge.id
LEFT JOIN consultas c ON tm.id = c.tema_id
GROUP BY tm.id, tm.nombre, tm.categoria, ge.id, ge.nombre;

CREATE VIEW v_alumnos_sin_actividad AS
SELECT 
    pe.id AS estudiante_id,
    pe.nombres || ' ' || pe.apellido_paterno AS nombre_completo,
    ge.nombre AS grado_escolar,
    ade.docente_id,
    MAX(c.fecha_inicio) AS ultima_actividad,
    EXTRACT(DAY FROM NOW() - MAX(c.fecha_inicio)) AS dias_inactivo
FROM perfiles_estudiante pe
JOIN grados_escolares ge ON pe.grado_escolar_id = ge.id
JOIN aulas_docente_estudiante ade ON pe.id = ade.estudiante_id AND ade.activo = TRUE
LEFT JOIN consultas c ON pe.id = c.estudiante_id
GROUP BY pe.id, pe.nombres, pe.apellido_paterno, ge.nombre, ade.docente_id
HAVING MAX(c.fecha_inicio) < NOW() - INTERVAL '7 days' OR MAX(c.fecha_inicio) IS NULL;