-- Script DML de Datos Semilla para H2 Database
-- Sistema: MedPharm Express (Laboratorio 13)

-- Insertar Usuarios Iniciales
-- Nota: La contraseña en texto plano para testing es 'password123'
-- Hash BCrypt de 'password123': $2a$10$e8XWfC.y47b5s7j3zK9t.O0E/4m4l6/P5K9V8/Z.6g2R1
INSERT INTO usuario (username, password, nombre_completo, rol) VALUES 
('medico1', '$2a$10$e8XWfC.y47b5s7j3zK9t.O0E/4m4l6/P5K9V8/Z.6g2R1', 'Dr. Esteban Soto Vargas', 'MEDICO'),
('farma1', '$2a$10$e8XWfC.y47b5s7j3zK9t.O0E/4m4l6/P5K9V8/Z.6g2R1', 'Dra. Lucia Ramirez Solis', 'FARMACEUTICO');

-- Insertar Medicamentos en Catálogo
INSERT INTO medicamento (codigo, nombre, stock, precio_unitario) VALUES 
('MED-001', 'Acetaminofen 500mg', 150, 1200.00),
('MED-002', 'Amoxicilina 500mg', 80, 4500.00),
('MED-003', 'Ibuprofeno 400mg', 100, 2100.00),
('MED-004', 'Loratadina 10mg', 60, 1800.00),
('MED-005', 'Omeprazol 20mg', 45, 3200.00);

-- Insertar Recetas Médicas Encabezado
INSERT INTO receta_medica (codigo_receta, paciente_nombre, medico_id, estado, fecha_emision) VALUES 
('REC-2026-001', 'Mariana Fallas Cordero', 1, 'PENDIENTE', CURRENT_TIMESTAMP),
('REC-2026-002', 'Carlos Guzman Arias', 1, 'DESPACHADA', CURRENT_TIMESTAMP);

-- Insertar Detalle de Recetas Médicas
INSERT INTO detalle_receta (receta_id, medicamento_id, cantidad, dosis_indicada) VALUES 
(1, 1, 20, '1 tableta cada 8 horas por 5 dias'),
(1, 3, 15, '1 tableta cada 12 horas por 3 dias'),
(2, 2, 14, '1 capsula cada 12 horas por 7 dias');
