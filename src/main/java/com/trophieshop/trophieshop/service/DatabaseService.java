package com.trophieshop.trophieshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para manejar las operaciones de base de datos
 * Proporciona métodos auxiliares para consultas comunes
 */
@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifica si la conexión a la base de datos está activa
     * 
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public boolean testConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene información de la conexión
     * 
     * @return String con la información de la conexión
     */
    public String getConnectionInfo() {
        try {
            String databaseName = jdbcTemplate.queryForObject(
                    "SELECT DATABASE()", String.class);
            String version = jdbcTemplate.queryForObject(
                    "SELECT VERSION()", String.class);
            return "Base de datos: " + databaseName + " | Versión MySQL: " + version;
        } catch (Exception e) {
            return "No se pudo obtener la información de la conexión";
        }
    }

    /**
     * Ejecuta una consulta SQL simple
     * 
     * @param sql Consulta SQL a ejecutar
     * @return Resultado de la consulta
     */
    public int executeUpdate(String sql) {
        try {
            return jdbcTemplate.update(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar la consulta: " + e.getMessage());
        }
    }
}
