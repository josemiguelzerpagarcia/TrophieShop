package com.trophieshop.trophieshop;

import com.trophieshop.trophieshop.entity.Canje;
import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.LogroDesbloqueado;
import com.trophieshop.trophieshop.entity.Plataforma;
import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import com.trophieshop.trophieshop.entity.SteamLogroOtorgado;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.entity.Videojuego;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import com.trophieshop.trophieshop.repository.CanjeRepository;
import com.trophieshop.trophieshop.repository.LogroDesbloqueadoRepository;
import com.trophieshop.trophieshop.repository.LogroRepository;
import com.trophieshop.trophieshop.repository.PlataformaRepository;
import com.trophieshop.trophieshop.repository.ProductoMerchandisingRepository;
import com.trophieshop.trophieshop.repository.SteamLogroOtorgadoRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import com.trophieshop.trophieshop.repository.VideojuegoRepository;
import com.trophieshop.trophieshop.service.PlataformaService;
import com.trophieshop.trophieshop.service.ProductoMerchandisingService;
import com.trophieshop.trophieshop.service.LogroService;
import com.trophieshop.trophieshop.service.VideojuegoService;
import com.trophieshop.trophieshop.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TrophieshopApplicationTests {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ProductoMerchandisingRepository productoRepository;

	@Autowired
	private PlataformaRepository plataformaRepository;

	@Autowired
	private VideojuegoRepository videojuegoRepository;

	@Autowired
	private LogroRepository logroRepository;

	@Autowired
	private LogroDesbloqueadoRepository logroDesbloqueadoRepository;

	@Autowired
	private CanjeRepository canjeRepository;

	@Autowired
	private SteamLogroOtorgadoRepository steamLogroOtorgadoRepository;

	@Autowired
	private ProductoMerchandisingService productoMerchandisingService;

	@Autowired
	private PlataformaService plataformaService;

	@Autowired
	private VideojuegoService videojuegoService;

	@Autowired
	private LogroService logroService;

	@Test
	void contextLoads() {
	}

	@Test
	void deleteUserRemovesDependentRecords() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Borrar Usuario");
		usuario.setEmail("borrar@trophieshop.com");
		usuario.setPassword("secret");
		usuario.setRol("USER");
		usuario.setMonedasAcumuladas(100);
		usuario = usuarioRepository.save(usuario);

		Plataforma plataforma = new Plataforma();
		plataforma.setNombre("PC Test");
		plataforma = plataformaRepository.save(plataforma);

		ProductoMerchandising producto = new ProductoMerchandising();
		producto.setNombre("Taza Test");
		producto.setDescripcion("Producto de prueba");
		producto.setStock(10);
		producto.setCostoMonedas(50);
		producto = productoRepository.save(producto);

		Videojuego videojuego = new Videojuego();
		videojuego.setTitulo("Juego Test");
		videojuego.setUsuario(usuario);
		videojuego.setPlataforma(plataforma);
		videojuego.setSteamAppId(123L);
		videojuego = videojuegoRepository.save(videojuego);

		Logro logro = new Logro();
		logro.setNombre("Logro Test");
		logro.setDescripcion("Descripcion");
		logro.setTipo(TipoLogro.APLICACION);
		logro.setValorMonedas(200);
		logro.setVideojuego(videojuego);
		logro = logroRepository.save(logro);

		LogroDesbloqueado desbloqueado = new LogroDesbloqueado();
		desbloqueado.setUsuario(usuario);
		desbloqueado.setLogro(logro);
		desbloqueado.setMonedasOtorgadas(200);
		logroDesbloqueadoRepository.save(desbloqueado);

		Canje canje = new Canje();
		canje.setUsuario(usuario);
		canje.setProducto(producto);
		canje.setCantidad(1);
		canje.setTotalMonedas(50);
		canjeRepository.save(canje);

		SteamLogroOtorgado steamLogroOtorgado = new SteamLogroOtorgado();
		steamLogroOtorgado.setUsuario(usuario);
		steamLogroOtorgado.setAppId(123L);
		steamLogroOtorgado.setGameName("Juego Test");
		steamLogroOtorgado.setAchievementApiName("ACH_TEST");
		steamLogroOtorgado.setAchievementDisplayName("Achievement Test");
		steamLogroOtorgado.setRarityPercent(12.5);
		steamLogroOtorgado.setRarityType("COMMON");
		steamLogroOtorgado.setPuntosOtorgados(200);
		steamLogroOtorgadoRepository.save(steamLogroOtorgado);

		usuarioService.delete(usuario.getId());

		assertThat(usuarioRepository.findById(usuario.getId())).isEmpty();
		assertThat(videojuegoRepository.findByUsuarioId(usuario.getId())).isEmpty();
		assertThat(logroRepository.findByVideojuegoId(videojuego.getId())).isEmpty();
		assertThat(logroDesbloqueadoRepository.findByUsuarioId(usuario.getId())).isEmpty();
		assertThat(canjeRepository.findByUsuarioId(usuario.getId())).isEmpty();
		assertThat(steamLogroOtorgadoRepository.findByUsuarioIdOrderByFechaOtorgadoDesc(usuario.getId())).isEmpty();
	}

	@Test
	void deleteProductRemovesRelatedCanjes() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Borrar Producto User");
		usuario.setEmail("borrar-producto-user@trophieshop.com");
		usuario.setPassword("secret");
		usuario.setRol("USER");
		usuario = usuarioRepository.save(usuario);

		ProductoMerchandising producto = new ProductoMerchandising();
		producto.setNombre("Producto Cascade");
		producto.setDescripcion("Producto de prueba");
		producto.setStock(10);
		producto.setCostoMonedas(75);
		producto = productoRepository.save(producto);

		Canje canje = new Canje();
		canje.setUsuario(usuario);
		canje.setProducto(producto);
		canje.setCantidad(1);
		canje.setTotalMonedas(75);
		canjeRepository.save(canje);

		productoMerchandisingService.delete(producto.getId());

		assertThat(productoRepository.findById(producto.getId())).isEmpty();
		assertThat(canjeRepository.findByProductoId(producto.getId())).isEmpty();
	}

	@Test
	void deletePlatformRemovesRelatedGamesAndAchievements() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Borrar Plataforma User");
		usuario.setEmail("borrar-plataforma-user@trophieshop.com");
		usuario.setPassword("secret");
		usuario.setRol("USER");
		usuario = usuarioRepository.save(usuario);

		Plataforma plataforma = new Plataforma();
		plataforma.setNombre("Platform Cascade");
		plataforma = plataformaRepository.save(plataforma);

		Videojuego videojuego = new Videojuego();
		videojuego.setTitulo("Juego Cascade");
		videojuego.setUsuario(usuario);
		videojuego.setPlataforma(plataforma);
		videojuego.setSteamAppId(456L);
		videojuego = videojuegoRepository.save(videojuego);

		Logro logro = new Logro();
		logro.setNombre("Logro Cascade");
		logro.setDescripcion("Descripcion");
		logro.setTipo(TipoLogro.PLATAFORMA);
		logro.setValorMonedas(150);
		logro.setVideojuego(videojuego);
		logro = logroRepository.save(logro);

		LogroDesbloqueado desbloqueado = new LogroDesbloqueado();
		desbloqueado.setUsuario(usuario);
		desbloqueado.setLogro(logro);
		desbloqueado.setMonedasOtorgadas(150);
		logroDesbloqueadoRepository.save(desbloqueado);

		plataformaService.delete(plataforma.getId());

		assertThat(plataformaRepository.findById(plataforma.getId())).isEmpty();
		assertThat(videojuegoRepository.findByUsuarioId(usuario.getId())).isEmpty();
		assertThat(logroRepository.findByVideojuegoId(videojuego.getId())).isEmpty();
		assertThat(logroDesbloqueadoRepository.findByLogroId(logro.getId())).isEmpty();
	}

	@Test
	void deleteVideojuegoRemovesRelatedLogrosAndUnlocks() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Borrar Juego User");
		usuario.setEmail("borrar-juego-user@trophieshop.com");
		usuario.setPassword("secret");
		usuario.setRol("USER");
		usuario = usuarioRepository.save(usuario);

		Plataforma plataforma = new Plataforma();
		plataforma.setNombre("Platform Game Cascade");
		plataforma = plataformaRepository.save(plataforma);

		Videojuego videojuego = new Videojuego();
		videojuego.setTitulo("Juego Cascade 2");
		videojuego.setUsuario(usuario);
		videojuego.setPlataforma(plataforma);
		videojuego.setSteamAppId(789L);
		videojuego = videojuegoRepository.save(videojuego);

		Logro logro = new Logro();
		logro.setNombre("Logro Cascade 2");
		logro.setDescripcion("Descripcion");
		logro.setTipo(TipoLogro.APLICACION);
		logro.setValorMonedas(120);
		logro.setVideojuego(videojuego);
		logro = logroRepository.save(logro);

		LogroDesbloqueado desbloqueado = new LogroDesbloqueado();
		desbloqueado.setUsuario(usuario);
		desbloqueado.setLogro(logro);
		desbloqueado.setMonedasOtorgadas(120);
		logroDesbloqueadoRepository.save(desbloqueado);

		videojuegoService.delete(videojuego.getId());

		assertThat(videojuegoRepository.findById(videojuego.getId())).isEmpty();
		assertThat(logroRepository.findByVideojuegoId(videojuego.getId())).isEmpty();
		assertThat(logroDesbloqueadoRepository.findByLogroId(logro.getId())).isEmpty();
	}

	@Test
	void deleteLogroRemovesRelatedUnlocks() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Borrar Logro User");
		usuario.setEmail("borrar-logro-user@trophieshop.com");
		usuario.setPassword("secret");
		usuario.setRol("USER");
		usuario = usuarioRepository.save(usuario);

		Plataforma plataforma = new Plataforma();
		plataforma.setNombre("Platform Logro Cascade");
		plataforma = plataformaRepository.save(plataforma);

		Videojuego videojuego = new Videojuego();
		videojuego.setTitulo("Juego Logro Cascade");
		videojuego.setUsuario(usuario);
		videojuego.setPlataforma(plataforma);
		videojuego.setSteamAppId(987L);
		videojuego = videojuegoRepository.save(videojuego);

		Logro logro = new Logro();
		logro.setNombre("Logro Cascade 3");
		logro.setDescripcion("Descripcion");
		logro.setTipo(TipoLogro.PLATAFORMA);
		logro.setValorMonedas(130);
		logro.setVideojuego(videojuego);
		logro = logroRepository.save(logro);

		LogroDesbloqueado desbloqueado = new LogroDesbloqueado();
		desbloqueado.setUsuario(usuario);
		desbloqueado.setLogro(logro);
		desbloqueado.setMonedasOtorgadas(130);
		logroDesbloqueadoRepository.save(desbloqueado);

		logroService.delete(logro.getId());

		assertThat(logroRepository.findById(logro.getId())).isEmpty();
		assertThat(logroDesbloqueadoRepository.findByLogroId(logro.getId())).isEmpty();
	}

}
