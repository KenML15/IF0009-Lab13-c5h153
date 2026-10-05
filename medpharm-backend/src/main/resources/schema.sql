-- Script DDL de Inicialización de Esquema para H2 Database
-- Sistema: MedPharm Express (Laboratorio 13)

DROP TABLE IF EXISTS detalle_receta;
DROP TABLE IF EXISTS receta_medica;
DROP TABLE IF EXISTS medicamento;
DROP TABLE IF EXISTS usuario;

-- Tabla de Usuarios para Autenticación JWT
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol VARCHAR(30) NOT NULL
);

-- Tabla de Catálogo de Medicamentos
CREATE TABLE medicamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(10,2) NOT NULL
);

-- Tabla Encabezado de Receta Médica
CREATE TABLE receta_medica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_receta VARCHAR(30) NOT NULL UNIQUE,
    paciente_nombre VARCHAR(120) NOT NULL,
    medico_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receta_medico 
        FOREIGN KEY (medico_id) REFERENCES usuario(id)
);

-- Tabla Detalle de Receta Médica
CREATE TABLE detalle_receta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receta_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    dosis_indicada VARCHAR(255) NOT NULL,
    CONSTRAINT fk_detalle_receta 
        FOREIGN KEY (receta_id) REFERENCES receta_medica(id),
    CONSTRAINT fk_detalle_medicamento 
        FOREIGN KEY (medicamento_id) REFERENCES medicamento(id)
);
