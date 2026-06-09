-- --------------------------------------------------------
-- 11. DATOS INICIALES (SEED DATA)
-- --------------------------------------------------------

INSERT INTO grados_escolares (nombre, nivel, orden, descripcion) VALUES
('1° Primaria', 'primaria', 1, 'Primer año de educación primaria'),
('2° Primaria', 'primaria', 2, 'Segundo año de educación primaria'),
('3° Primaria', 'primaria', 3, 'Tercer año de educación primaria'),
('4° Primaria', 'primaria', 4, 'Cuarto año de educación primaria'),
('5° Primaria', 'primaria', 5, 'Quinto año de educación primaria'),
('6° Primaria', 'primaria', 6, 'Sexto año de educación primaria');

INSERT INTO avatares (codigo, nombre, color_hex) VALUES
('a', 'Pato Aventurero', '#F4A261'),
('b', 'Gato Curioso', '#E76F51'),
('c', 'Oso Sabio', '#2A9D8F'),
('d', 'Zorro Astuto', '#264653'),
('e', 'Rana Amigable', '#E9C46A'),
('f', 'Búho Erudito', '#8B5E3C');

INSERT INTO temas_matematicos (nombre, descripcion, grado_escolar_id, categoria, tags) VALUES
('Suma de números enteros', 'Operación básica de adición con llevadas', 1, 'aritmetica', ARRAY['suma', 'enteros', 'llevadas']),
('Resta con llevadas', 'Operación de sustracción con préstamo', 1, 'aritmetica', ARRAY['resta', 'sustraccion', 'prestamo']),
('Multiplicación de decimales', 'Producto con números decimales', 3, 'aritmetica', ARRAY['multiplicacion', 'decimales']),
('División de fracciones', 'Cociente entre fracciones propias e impropias', 4, 'fracciones', ARRAY['division', 'fracciones', 'inverso']),
('Problemas de suma y resta', 'Resolución de problemas verbales básicos', 1, 'problemas', ARRAY['problemas', 'suma', 'resta']),
('Álgebra básica', 'Introducción a ecuaciones simples', 5, 'algebra', ARRAY['algebra', 'ecuaciones', 'incognita']),
('Geometría plana', 'Figuras y perímetros básicos', 2, 'geometria', ARRAY['geometria', 'figuras', 'perimetro']);

INSERT INTO estrategias_pedagogicas (nombre, descripcion, tipo_aprendizaje) VALUES
('Explicación verbal detallada', 'Repetir el concepto con otras palabras más simples', 'auditivo'),
('Representación visual', 'Usar diagramas, bloques o figuras para ilustrar', 'visual'),
('Ejemplo cotidiano', 'Relacionar con situaciones de la vida diaria del niño', 'kinestesico'),
('Desglose por partes', 'Dividir el problema en mini-problemas más simples', 'visual'),
('Práctica guiada', 'Resolver un ejercicio similar paso a paso junto al estudiante', 'kinestesico');