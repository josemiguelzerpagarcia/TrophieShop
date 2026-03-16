package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar el estado de la base de datos
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    /**
     * Verifica el estado de la conexión a la base de datos
     * 
     * @return ResponseEntity con el estado de la conexión
     */
    @GetMapping("/status")
    public ResponseEntity<?> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();

        boolean isConnected = databaseService.testConnection();
        response.put("conectado", isConnected);
        response.put("mensaje", isConnected ? 
            "Conexión a la base de datos establecida correctamente" : 
            "Error: No se pudo conectar a la base de datos");
        response.put("detalles", databaseService.getConnectionInfo());

        if (isConnected) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Endpoint de prueba simple
     * 
     * @return Mensaje de bienvenida
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "API de TrophieShop funcionando correctamente");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
