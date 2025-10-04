-- 1) Creamos la base de datos 
DROP DATABASE IF EXISTS BBDD_CONS;
CREATE DATABASE BBDD_CONS;
USE BBDD_CONS;


-- 2) Tabla de Enlace
CREATE TABLE Enlace (
    km DECIMAL(6,3),
    tipo ENUM('GLO', 'ENL', 'GAL', 'CUR', 'SAL') NOT NULL,
    -- GLO -> glorieta // ENL -> enlace // GAL -> galibo // SAL -> salida
    direccion ENUM('NORTE -> NORTE', 'NORTE -> ESTE', 'NORTE -> OESTE', 'NORTE -> SUR',
        'ESTE -> NORTE', 'ESTE -> ESTE', 'ESTE -> OESTE', 'ESTE -> SUR',
        'OESTE -> NORTE', 'OESTE -> ESTE', 'OESTE -> OESTE', 'OESTE -> SUR',
        'SUR -> NORTE', 'SUR -> ESTE', 'SUR -> OESTE', 'SUR -> SUR') NOT NULL,
    longitud DECIMAL(6,3),
    anchura DECIMAL(5,3),
    codCarreteraOrigen VARCHAR(8) NOT NULL,     
    codCarreteraDestino VARCHAR(8),
    fecha DATE NOT NULL,

    -- Generare el id del enlace de la siguiente manera:
        -- CodCarreteraOrigen_tipo_CodCarreteraDestino_direccion, en caso de que no exista
        -- CodCarreteraDestino se sustituira por km
    idEnlace VARCHAR(30) GENERATED ALWAYS AS (
        CONCAT(codCarreteraOrigen, '_',
        tipo, '_',
        IF (codCarreteraDestino IS NULL OR codCarreteraDestino = '', km, codCarreteraDestino),
        '_', direccion
    )) STORED NOT NULL,
    PRIMARY KEY (idEnlace)
);