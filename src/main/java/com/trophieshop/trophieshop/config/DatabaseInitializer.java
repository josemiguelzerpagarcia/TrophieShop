package com.trophieshop.trophieshop.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Clase para inicializar la base de datos automáticamente
 * Verifica la conexión a la base de datos cuando inicia la aplicación
 */
@Configuration
public class DatabaseInitializer {

    /**
     * Verifica la conexión a la base de datos y ejecuta consultas iniciales
     * 
     * @param jdbcTemplate Template de JDBC para ejecutar consultas
     * @return CommandLineRunner que se ejecuta al inicio de la aplicación
     */
    @Bean
    public CommandLineRunner initializeDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Verificar la conexión a la base de datos
                jdbcTemplate.queryForObject("SELECT 1", String.class);
                System.out.println("✓ Conexión a la base de datos establecida correctamente");
            } catch (Exception e) {
                System.err.println("✗ Error al conectar con la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
