-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS trophieshop
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE trophieshop;

-- Tabla de usuarios (incluye monedas acumuladas y campo para distinguir admin)
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,  
    monedas INT NOT NULL DEFAULT 0,
    es_admin TINYINT(1) NOT NULL DEFAULT 0,  
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de videojuegos 
CREATE TABLE videojuegos (
    id_videojuego INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    genero VARCHAR(50) DEFAULT NULL,
    desarrollador VARCHAR(100) DEFAULT NULL
);

-- Tabla de relación: usuarios y videojuegos que poseen/vinculan
CREATE TABLE usuario_videojuegos (
    id_usuario INT NOT NULL,
    id_videojuego INT NOT NULL,
    fecha_vinculacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_videojuego),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_videojuego) REFERENCES videojuegos(id_videojuego) ON DELETE CASCADE
);

-- Tabla de logros 
CREATE TABLE logros (
    id_logro INT AUTO_INCREMENT PRIMARY KEY,
    id_videojuego INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    cantidad_monedas INT NOT NULL,
    es_exclusivo TINYINT(1) NOT NULL DEFAULT 0, 
    FOREIGN KEY (id_videojuego) REFERENCES videojuegos(id_videojuego) ON DELETE CASCADE
);

-- Tabla de relación: logros desbloqueados por usuario 
CREATE TABLE usuario_logros (
    id_usuario INT NOT NULL,
    id_logro INT NOT NULL,
    fecha_unlock DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_logro),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_logro) REFERENCES logros(id_logro) ON DELETE CASCADE
);

-- Tabla de productos de merchandising
CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    stock INT NOT NULL DEFAULT 0,
    costo_monedas INT NOT NULL
);

-- Tabla de canjes 
CREATE TABLE canjes (
    id_canje INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_producto INT NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    cantidad_monedas INT NOT NULL,  
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE RESTRICT
);