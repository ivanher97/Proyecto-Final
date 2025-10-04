-- 1) Creamos la base de datos 
DROP DATABASE IF EXISTS BBDD_RS;
CREATE DATABASE BBDD_RS;
USE BBDD_RS;

-- 2) Tabla de Usuarios
CREATE TABLE Usuario (
    NombreUsuario VARCHAR(50) NOT NULL PRIMARY KEY,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Contrasena VARCHAR(255) NOT NULL, -- Se almacenara el hash
    Rol ENUM('EMP', 'ADM')  NOT NULL
);

-- 3) Tabla de Estudios
CREATE TABLE Estudio (
    CodEstudio VARCHAR(20) NOT NULL PRIMARY KEY
);

-- 4) Tabla de Transporte
CREATE TABLE Transporte (
    CodTransporte VARCHAR(8) PRIMARY KEY
);

-- 5) Tabla de Enlace
CREATE TABLE Enlace (
    km DECIMAL(6,3),
    coordenadas VARCHAR (30),
    tipo ENUM('GLO', 'ENL', 'GAL', 'CUR', 'SAL') NOT NULL,
    -- GLO -> glorieta // ENL -> enlace // GAL -> galibo // SAL -> salida
    direccion ENUM('NORTE -> NORTE', 'NORTE -> ESTE', 'NORTE -> OESTE', 'NORTE -> SUR',
        'ESTE -> NORTE', 'ESTE -> ESTE', 'ESTE -> OESTE', 'ESTE -> SUR',
        'OESTE -> NORTE', 'OESTE -> ESTE', 'OESTE -> OESTE', 'OESTE -> SUR',
        'SUR -> NORTE', 'SUR -> ESTE', 'SUR -> OESTE', 'SUR -> SUR') NOT NULL,
    codCarreteraOrigen VARCHAR(8) NOT NULL,     
    codCarreteraDestino VARCHAR(8),

    -- Generare el id del enlace de la siguiente manera:
        -- CodCarreteraOrigen_tipo_CodCarreteraDestino_direccion, en caso de que no exista
        -- CodCarreteraDestino se sustituira por km
    idEnlace VARCHAR(50) GENERATED ALWAYS AS (
        CONCAT(codCarreteraOrigen, '_',
        tipo, '_',
        IF (codCarreteraDestino IS NULL OR codCarreteraDestino = '', km, codCarreteraDestino),
        '_', direccion
    )) STORED NOT NULL,
    PRIMARY KEY (idEnlace)      
);

-- 7) Tabla de Plano
CREATE TABLE Plano (
    codTransporte VARCHAR(8) NOT NULL, 
    idEnlace VARCHAR(50) NOT NULL, 
    coordenadas VARCHAR (30),
    
    -- Generamos el idPlano de la siguiente manera:
        -- Usamos el idEnlace generado anteriormente e incluimos el codTransporte
    codPlano VARCHAR(50) GENERATED ALWAYS AS (
        CONCAT(idEnlace, '_', codTransporte)) STORED NOT NULL,
    PRIMARY KEY (codPlano),           

    CONSTRAINT fk_plano_transporte
        FOREIGN KEY (codTransporte) 
        REFERENCES Transporte (codTransporte),

    CONSTRAINT fk_plano_enlace
        FOREIGN KEY (idEnlace)
        REFERENCES Enlace (idEnlace)
);

-- 8) Tabla de Planos - Estudios
-- Un mismo plano puede encontrarse en 1 o varios estudios y un estidio contiene de 1 a N planos
CREATE TABLE Estudio_Plano(
    idEstudio_Plano INT AUTO_INCREMENT NOT NULL PRIMARY KEY,

    codPlano VARCHAR(50) NOT NULL,
    CodEstudio  VARCHAR(20) NOT NULL,

    CONSTRAINT fk_estudio_planoEstudio
        FOREIGN KEY (CodEstudio)
        REFERENCES Estudio (CodEstudio),

    CONSTRAINT fk_plano_planoEstudio
        FOREIGN KEY (codPlano)
        REFERENCES Plano (codPlano)      
);

