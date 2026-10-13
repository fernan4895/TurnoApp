CREATE DATABASE IF NOT EXISTS turnopro_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE turnopro_db;

CREATE TABLE IF NOT EXISTS roles (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(32) NOT NULL UNIQUE);
CREATE TABLE IF NOT EXISTS usuarios (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(100) NOT NULL, username VARCHAR(60) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS usuario_roles (usuario_id BIGINT NOT NULL, rol_id BIGINT NOT NULL, PRIMARY KEY(usuario_id,rol_id), FOREIGN KEY(usuario_id) REFERENCES usuarios(id), FOREIGN KEY(rol_id) REFERENCES roles(id));
CREATE TABLE IF NOT EXISTS sucursales (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(100) NOT NULL, direccion VARCHAR(200), activa BOOLEAN NOT NULL DEFAULT TRUE, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS servicios (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(80) NOT NULL UNIQUE, descripcion VARCHAR(180) NOT NULL DEFAULT '', prefijo VARCHAR(5) NOT NULL, tiempo_promedio_min INT NOT NULL DEFAULT 10, activo BOOLEAN NOT NULL DEFAULT TRUE, prioridad INT NOT NULL DEFAULT 0, orden_visualizacion INT NOT NULL DEFAULT 0, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS ventanillas (id BIGINT PRIMARY KEY AUTO_INCREMENT, sucursal_id BIGINT NOT NULL, nombre VARCHAR(60) NOT NULL, activa BOOLEAN NOT NULL DEFAULT TRUE, FOREIGN KEY(sucursal_id) REFERENCES sucursales(id), UNIQUE KEY uq_ventanilla_sucursal(sucursal_id,nombre));
CREATE TABLE IF NOT EXISTS ventanilla_servicios (ventanilla_id BIGINT NOT NULL, servicio_id BIGINT NOT NULL, PRIMARY KEY(ventanilla_id,servicio_id), FOREIGN KEY(ventanilla_id) REFERENCES ventanillas(id), FOREIGN KEY(servicio_id) REFERENCES servicios(id));
CREATE TABLE IF NOT EXISTS turnos (id BIGINT PRIMARY KEY AUTO_INCREMENT, numero_turno VARCHAR(16) NOT NULL, numero_secuencial INT NOT NULL, fecha_operativa DATE NOT NULL, servicio_id BIGINT NOT NULL, sucursal_id BIGINT NOT NULL, estado ENUM('ESPERA','LLAMADO','EN_ATENCION','ATENDIDO','CANCELADO','AUSENTE') NOT NULL DEFAULT 'ESPERA', prioridad INT NOT NULL DEFAULT 0, cliente_nombre VARCHAR(100) NOT NULL, cliente_telefono VARCHAR(24), codigo_consulta VARCHAR(64) NOT NULL UNIQUE, ventanilla_id BIGINT, creado_en DATETIME NOT NULL, llamado_en DATETIME, inicio_atencion DATETIME, fin_atencion DATETIME, motivo_cancelacion VARCHAR(250), version BIGINT NOT NULL DEFAULT 0, FOREIGN KEY(servicio_id) REFERENCES servicios(id), FOREIGN KEY(sucursal_id) REFERENCES sucursales(id), FOREIGN KEY(ventanilla_id) REFERENCES ventanillas(id), UNIQUE KEY uq_turno_operativo(sucursal_id,servicio_id,fecha_operativa,numero_secuencial), UNIQUE KEY uq_numero_turno(sucursal_id,fecha_operativa,numero_turno), INDEX idx_turnos_fila(sucursal_id,servicio_id,estado,prioridad,creado_en));
CREATE TABLE IF NOT EXISTS historial_turnos (id BIGINT PRIMARY KEY AUTO_INCREMENT, turno_id BIGINT NOT NULL, estado_anterior VARCHAR(16), estado_nuevo VARCHAR(16) NOT NULL, usuario_id BIGINT, detalle VARCHAR(250), creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(turno_id) REFERENCES turnos(id), FOREIGN KEY(usuario_id) REFERENCES usuarios(id), INDEX idx_historial_turno(turno_id,creado_en));
CREATE TABLE IF NOT EXISTS configuracion (id BIGINT PRIMARY KEY AUTO_INCREMENT, sucursal_id BIGINT NOT NULL, clave VARCHAR(80) NOT NULL, valor VARCHAR(500), actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, FOREIGN KEY(sucursal_id) REFERENCES sucursales(id), UNIQUE KEY uq_config_sucursal(sucursal_id,clave));

INSERT IGNORE INTO roles(nombre) VALUES ('ADMINISTRADOR'),('SUPERVISOR'),('AGENTE');
INSERT INTO sucursales(id,nombre,direccion) VALUES (1,'Sucursal principal','') ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);
INSERT INTO servicios(id,nombre,descripcion,prefijo,tiempo_promedio_min,orden_visualizacion) VALUES
 (1,'Atención general','Trámites y atención personalizada','A',12,1),
 (2,'Caja','Pagos, cobros y facturación','C',8,2),
 (3,'Informes','Orientación y consultas','I',6,3),
 (4,'Soporte técnico','Ayuda con productos y servicios','S',18,4)
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion),prefijo=VALUES(prefijo),tiempo_promedio_min=VALUES(tiempo_promedio_min);
INSERT IGNORE INTO ventanillas(id,sucursal_id,nombre) VALUES (1,1,'Ventanilla 01'),(2,1,'Ventanilla 02');
INSERT IGNORE INTO ventanilla_servicios(ventanilla_id,servicio_id) SELECT 1,id FROM servicios;
INSERT IGNORE INTO ventanilla_servicios(ventanilla_id,servicio_id) SELECT 2,id FROM servicios;
