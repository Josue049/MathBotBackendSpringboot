-- ============================================================
-- ESQUEMA DE BASE DE DATOS: MATHBOT
-- Sistema educativo de asistencia matemática para primaria
-- PostgreSQL 15+
-- ============================================================

-- --------------------------------------------------------
-- 1. EXTENSIONES
-- --------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- --------------------------------------------------------
-- 2. TIPOS ENUMERADOS (DOMAIN TYPES)
-- --------------------------------------------------------
CREATE TYPE rol_usuario AS ENUM ('estudiante', 'docente', 'padre', 'administrador');
CREATE TYPE estado_consulta AS ENUM ('activa', 'cerrada', 'abandonada');
CREATE TYPE tipo_consulta AS ENUM ('texto', 'imagen', 'mixta');
CREATE TYPE tipo_emisor AS ENUM ('estudiante', 'bot', 'sistema');
CREATE TYPE tipo_contenido_mensaje AS ENUM ('texto', 'imagen', 'explicacion_paso', 'confirmacion', 'sistema');
CREATE TYPE estado_procesamiento_imagen AS ENUM ('pendiente', 'procesando', 'procesado', 'error', 'rechazado');
CREATE TYPE tipo_error_matematico AS ENUM ('calculo', 'procedimiento', 'concepto', 'notacion', 'omision', 'otro');
CREATE TYPE severidad_error AS ENUM ('leve', 'moderado', 'grave');
CREATE TYPE estado_actividad AS ENUM ('pendiente', 'asignada', 'completada', 'vencida');
CREATE TYPE tipo_indicador AS ENUM ('autonomia', 'participacion', 'comprension', 'progreso', 'regularidad');
CREATE TYPE estado_reporte AS ENUM ('generando', 'disponible', 'error', 'descargado');