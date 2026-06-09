-- --------------------------------------------------------
-- 10. ÍNDICES PARA OPTIMIZACIÓN
-- --------------------------------------------------------

CREATE INDEX idx_usuarios_nombre_usuario ON usuarios(nombre_usuario);
CREATE INDEX idx_usuarios_correo ON usuarios(correo_electronico);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_perfiles_estudiante_grado ON perfiles_estudiante(grado_escolar_id);
CREATE INDEX idx_perfiles_estudiante_usuario ON perfiles_estudiante(usuario_id);
CREATE INDEX idx_aulas_docente ON aulas_docente_estudiante(docente_id, activo);
CREATE INDEX idx_aulas_estudiante ON aulas_docente_estudiante(estudiante_id, activo);
CREATE INDEX idx_consultas_estudiante ON consultas(estudiante_id, estado);
CREATE INDEX idx_consultas_fecha ON consultas(fecha_inicio DESC);
CREATE INDEX idx_consultas_tema ON consultas(tema_id);
CREATE INDEX idx_mensajes_consulta ON mensajes(consulta_id, orden);
CREATE INDEX idx_mensajes_fecha ON mensajes(fecha_envio DESC);
CREATE INDEX idx_imagenes_consulta ON imagenes_ejercicio(consulta_id);
CREATE INDEX idx_imagenes_estado ON imagenes_ejercicio(estado_procesamiento);
CREATE INDEX idx_errores_imagen ON errores_detectados(imagen_id);
CREATE INDEX idx_errores_tipo ON errores_detectados(tipo_error);
CREATE INDEX idx_indicadores_estudiante ON indicadores_progreso(estudiante_id, tipo_indicador);
CREATE INDEX idx_indicadores_periodo ON indicadores_progreso(periodo_inicio, periodo_fin);
CREATE INDEX idx_reportes_padre ON reportes_pdf(padre_id, estado);
CREATE INDEX idx_reportes_estudiante ON reportes_pdf(estudiante_id, periodo_fin DESC);
CREATE INDEX idx_auditoria_usuario ON auditoria_accesos(usuario_id, fecha DESC);
CREATE INDEX idx_auditoria_accion ON auditoria_accesos(accion, fecha DESC);
CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id, leida, fecha_envio DESC);