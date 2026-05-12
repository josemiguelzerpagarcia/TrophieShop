package com.trophieshop.trophieshop.config;

import com.trophieshop.trophieshop.entity.Canje;
import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.LogroDesbloqueado;
import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import com.trophieshop.trophieshop.repository.CanjeRepository;
import com.trophieshop.trophieshop.repository.LogroDesbloqueadoRepository;
import com.trophieshop.trophieshop.repository.LogroRepository;
import com.trophieshop.trophieshop.repository.ProductoMerchandisingRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Clase para inicializar la base de datos automáticamente
 * Verifica la conexión a la base de datos cuando inicia la aplicación
 */
@Configuration
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private boolean columnExists(JdbcTemplate jdbcTemplate, String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                table,
                column
        );
        return count != null && count > 0;
    }

    private boolean uniqueConstraintExists(JdbcTemplate jdbcTemplate, String table, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class,
                table,
                indexName
        );
        return count != null && count > 0;
    }

    /**
     * Verifica la conexión a la base de datos y ejecuta consultas iniciales
     * 
     * @param jdbcTemplate Template de JDBC para ejecutar consultas
     * @return CommandLineRunner que se ejecuta al inicio de la aplicación
     */
    @Bean
    public CommandLineRunner initializeDatabase(
            JdbcTemplate jdbcTemplate,
            UsuarioRepository usuarioRepository,
            ProductoMerchandisingRepository productoRepository,
            LogroRepository logroRepository,
            LogroDesbloqueadoRepository logroDesbloqueadoRepository,
            CanjeRepository canjeRepository) {
        return args -> {
            try {
                // Verificar la conexión a la base de datos
                jdbcTemplate.queryForObject("SELECT 1", String.class);
                logger.info("✓ Conexión a la base de datos establecida correctamente");

                // Migracion real de esquema: logros_desbloqueados sin monedas_otorgadas
                try {
                    if (columnExists(jdbcTemplate, "logros_desbloqueados", "monedas_otorgadas")) {
                        jdbcTemplate.execute("ALTER TABLE logros_desbloqueados DROP COLUMN monedas_otorgadas");
                        logger.info("✓ Columna logros_desbloqueados.monedas_otorgadas eliminada");
                    }
                } catch (Exception e) {
                    logger.warn("⚠ No se pudo eliminar columna monedas_otorgadas (podría no existir): {}", e.getMessage());
                }

                // Limpiar posibles duplicados antes de crear el índice único
                try {
                    int deleted = jdbcTemplate.update("""
                            DELETE ld1 FROM logros_desbloqueados ld1
                            INNER JOIN logros_desbloqueados ld2
                              ON ld1.usuario_id = ld2.usuario_id
                             AND ld1.logro_id = ld2.logro_id
                             AND ld1.id > ld2.id
                            """);
                    if (deleted > 0) {
                        logger.info("✓ Eliminados {} registros duplicados en logros_desbloqueados", deleted);
                    }
                } catch (Exception e) {
                    logger.warn("⚠ No se pudo limpiar duplicados: {}", e.getMessage());
                }

                try {
                    if (!uniqueConstraintExists(jdbcTemplate, "logros_desbloqueados", "uk_usuario_logro_desbloqueado")) {
                        jdbcTemplate.execute("ALTER TABLE logros_desbloqueados ADD CONSTRAINT uk_usuario_logro_desbloqueado UNIQUE (usuario_id, logro_id)");
                        logger.info("✓ Restricción única uk_usuario_logro_desbloqueado creada");
                    } else {
                        logger.info("✓ Restricción única uk_usuario_logro_desbloqueado ya existe");
                    }
                } catch (Exception e) {
                    logger.warn("⚠ No se pudo crear restricción única: {}", e.getMessage());
                }

                // Tabla para evitar otorgar puntos duplicados en sincronizaciones de Steam
                jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS steam_logros_otorgados (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            usuario_id BIGINT NOT NULL,
                            app_id BIGINT NOT NULL,
                            game_name VARCHAR(200) NOT NULL,
                            achievement_api_name VARCHAR(200) NOT NULL,
                            achievement_display_name VARCHAR(200),
                            rarity_percent DOUBLE NOT NULL,
                            rarity_type VARCHAR(20) NOT NULL,
                            puntos_otorgados INT NOT NULL,
                            fecha_otorgado DATETIME(6) NOT NULL,
                            CONSTRAINT uk_usuario_app_achievement UNIQUE (usuario_id, app_id, achievement_api_name),
                            CONSTRAINT fk_steam_logros_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                        )
                        """);

                // Migración: Eliminar tabla videojuegos (datos vienen de Steam API)
                try {
                    Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'videojuegos'",
                        Integer.class
                    );
                    if (count != null && count > 0) {
                        try {
                            // Buscar y dropear cualquier FK que referencia videojuegos
                            List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logros' AND REFERENCED_TABLE_NAME = 'videojuegos'"
                            );
                            for (Map<String, Object> fk : fks) {
                                String constraintName = (String) fk.get("CONSTRAINT_NAME");
                                jdbcTemplate.execute("ALTER TABLE logros DROP FOREIGN KEY " + constraintName);
                            }
                        } catch (Exception e) {
                            logger.debug("No foreign keys to drop from logros table");
                        }
                        
                        try {
                            jdbcTemplate.execute("ALTER TABLE logros DROP COLUMN videojuego_id");
                        } catch (Exception e) {
                            logger.debug("Column videojuego_id might not exist");
                        }
                        
                        jdbcTemplate.execute("DROP TABLE videojuegos");
                        logger.info("✓ Tabla videojuegos eliminada (datos de Steam API)");
                    }
                } catch (Exception e) {
                    logger.debug("⚠ Tabla videojuegos no existe o no se pudo eliminar: {}", e.getMessage());
                }

                // Migración: Eliminar tabla plataformas (no necesaria, todo por Steam)
                try {
                    Integer plataformasCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'plataformas'",
                        Integer.class
                    );
                    if (plataformasCount != null && plataformasCount > 0) {
                        jdbcTemplate.execute("DROP TABLE plataformas");
                        logger.info("✓ Tabla plataformas eliminada (datos de Steam API)");
                    }
                } catch (Exception e) {
                    logger.debug("⚠ Tabla plataformas no existe o no se pudo eliminar: {}", e.getMessage());
                }

                if (usuarioRepository.count() > 0) {
                    logger.info("✓ Datos ya existentes: se omite el seed inicial");
                    return;
                }

                // Usuarios base para probar invitado/user/admin
                Usuario admin = new Usuario();
                admin.setNombre("Admin TrophyShop");
                admin.setEmail("admin@trophieshop.com");
                admin.setPassword("admin1234");
                admin.setMonedasAcumuladas(9999);
                admin.setRol("ADMIN");

                Usuario user = new Usuario();
                user.setNombre("Jugador Demo");
                user.setEmail("user@trophieshop.com");
                user.setPassword("user1234");
                user.setMonedasAcumuladas(2500);
                user.setRol("USER");

                Usuario user2 = new Usuario();
                user2.setNombre("Maria Gamer");
                user2.setEmail("maria@trophieshop.com");
                user2.setPassword("maria1234");
                user2.setMonedasAcumuladas(1400);
                user2.setRol("USER");

                usuarioRepository.saveAll(List.of(admin, user, user2));

                ProductoMerchandising camiseta = new ProductoMerchandising();
                camiseta.setNombre("Camiseta TrophyShop");
                camiseta.setDescripcion("Camiseta oficial de la tienda");
                camiseta.setStock(50);
                camiseta.setCostoMonedas(500);

                ProductoMerchandising taza = new ProductoMerchandising();
                taza.setNombre("Taza Coleccionable");
                taza.setDescripcion("Taza premium para gamers");
                taza.setStock(80);
                taza.setCostoMonedas(300);

                ProductoMerchandising gorra = new ProductoMerchandising();
                gorra.setNombre("Gorra TrophyShop");
                gorra.setDescripcion("Gorra oficial de la marca");
                gorra.setStock(35);
                gorra.setCostoMonedas(450);

                productoRepository.saveAll(List.of(camiseta, taza, gorra));

                Logro logro1 = new Logro();
                logro1.setNombre("Primer Trofeo");
                logro1.setDescripcion("Desbloquea tu primer logro");
                logro1.setTipo(TipoLogro.APLICACION);
                logro1.setValorMonedas(100);
                logro1.setSteamAppId(730L);

                Logro logro2 = new Logro();
                logro2.setNombre("Coleccionista");
                logro2.setDescripcion("Completa 10 retos");
                logro2.setTipo(TipoLogro.PLATAFORMA);
                logro2.setValorMonedas(250);
                logro2.setSteamAppId(570L);

                Logro logro3 = new Logro();
                logro3.setNombre("Velocidad Extrema");
                logro3.setDescripcion("Gana 3 carreras seguidas");
                logro3.setTipo(TipoLogro.APLICACION);
                logro3.setValorMonedas(180);
                logro3.setSteamAppId(440L);

                logroRepository.saveAll(List.of(logro1, logro2, logro3));

                LogroDesbloqueado desbloqueado1 = new LogroDesbloqueado();
                desbloqueado1.setUsuario(user);
                desbloqueado1.setLogro(logro1);

                LogroDesbloqueado desbloqueado2 = new LogroDesbloqueado();
                desbloqueado2.setUsuario(user2);
                desbloqueado2.setLogro(logro2);

                logroDesbloqueadoRepository.saveAll(List.of(desbloqueado1, desbloqueado2));

                Canje canje1 = new Canje();
                canje1.setUsuario(user);
                canje1.setProducto(camiseta);
                canje1.setCantidad(1);
                canje1.setTotalMonedas(camiseta.getCostoMonedas());

                Canje canje2 = new Canje();
                canje2.setUsuario(user2);
                canje2.setProducto(taza);
                canje2.setCantidad(2);
                canje2.setTotalMonedas(taza.getCostoMonedas() * 2);

                canjeRepository.saveAll(List.of(canje1, canje2));

                logger.info("✓ Seed inicial creado correctamente");
                logger.info("  - Admin: admin@trophieshop.com / admin1234");
                logger.info("  - User: user@trophieshop.com / user1234");
            } catch (Exception e) {
                logger.error("✗ Error al conectar con la base de datos: {}", e.getMessage(), e);
            }
        };
    }
}
