package com.trophieshop.trophieshop.config;

import com.trophieshop.trophieshop.entity.Canje;
import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.LogroDesbloqueado;
import com.trophieshop.trophieshop.entity.Plataforma;
import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.entity.Videojuego;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import com.trophieshop.trophieshop.repository.CanjeRepository;
import com.trophieshop.trophieshop.repository.LogroDesbloqueadoRepository;
import com.trophieshop.trophieshop.repository.LogroRepository;
import com.trophieshop.trophieshop.repository.PlataformaRepository;
import com.trophieshop.trophieshop.repository.ProductoMerchandisingRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import com.trophieshop.trophieshop.repository.VideojuegoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

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
    public CommandLineRunner initializeDatabase(
            JdbcTemplate jdbcTemplate,
            UsuarioRepository usuarioRepository,
            PlataformaRepository plataformaRepository,
            ProductoMerchandisingRepository productoRepository,
            VideojuegoRepository videojuegoRepository,
            LogroRepository logroRepository,
            LogroDesbloqueadoRepository logroDesbloqueadoRepository,
            CanjeRepository canjeRepository) {
        return args -> {
            try {
                // Verificar la conexión a la base de datos
                jdbcTemplate.queryForObject("SELECT 1", String.class);
                System.out.println("✓ Conexión a la base de datos establecida correctamente");

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

                if (usuarioRepository.count() > 0) {
                    System.out.println("✓ Datos ya existentes: se omite el seed inicial");
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

                Plataforma ps5 = new Plataforma();
                ps5.setNombre("PlayStation 5");

                Plataforma pc = new Plataforma();
                pc.setNombre("PC");

                Plataforma xbox = new Plataforma();
                xbox.setNombre("Xbox Series X");

                plataformaRepository.saveAll(List.of(ps5, pc, xbox));

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

                Videojuego game1 = new Videojuego();
                game1.setTitulo("The Last Chronicle");
                game1.setUsuario(user);
                game1.setPlataforma(ps5);
                game1.setSteamAppId(730L);

                Videojuego game2 = new Videojuego();
                game2.setTitulo("Cyber Legends");
                game2.setUsuario(user2);
                game2.setPlataforma(pc);
                game2.setSteamAppId(570L);

                Videojuego game3 = new Videojuego();
                game3.setTitulo("Racing Pro 2026");
                game3.setUsuario(admin);
                game3.setPlataforma(xbox);
                game3.setSteamAppId(440L);

                videojuegoRepository.saveAll(List.of(game1, game2, game3));

                Logro logro1 = new Logro();
                logro1.setNombre("Primer Trofeo");
                logro1.setDescripcion("Desbloquea tu primer logro");
                logro1.setTipo(TipoLogro.APLICACION);
                logro1.setValorMonedas(100);
                logro1.setVideojuego(game1);

                Logro logro2 = new Logro();
                logro2.setNombre("Coleccionista");
                logro2.setDescripcion("Completa 10 retos");
                logro2.setTipo(TipoLogro.PLATAFORMA);
                logro2.setValorMonedas(250);
                logro2.setVideojuego(game2);

                Logro logro3 = new Logro();
                logro3.setNombre("Velocidad Extrema");
                logro3.setDescripcion("Gana 3 carreras seguidas");
                logro3.setTipo(TipoLogro.APLICACION);
                logro3.setValorMonedas(180);
                logro3.setVideojuego(game3);

                logroRepository.saveAll(List.of(logro1, logro2, logro3));

                LogroDesbloqueado desbloqueado1 = new LogroDesbloqueado();
                desbloqueado1.setUsuario(user);
                desbloqueado1.setLogro(logro1);
                desbloqueado1.setMonedasOtorgadas(logro1.getValorMonedas());

                LogroDesbloqueado desbloqueado2 = new LogroDesbloqueado();
                desbloqueado2.setUsuario(user2);
                desbloqueado2.setLogro(logro2);
                desbloqueado2.setMonedasOtorgadas(logro2.getValorMonedas());

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

                System.out.println("✓ Seed inicial creado correctamente");
                System.out.println("  - Admin: admin@trophieshop.com / admin1234");
                System.out.println("  - User: user@trophieshop.com / user1234");
            } catch (Exception e) {
                System.err.println("✗ Error al conectar con la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
