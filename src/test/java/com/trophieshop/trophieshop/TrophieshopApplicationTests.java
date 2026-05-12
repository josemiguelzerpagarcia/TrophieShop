package com.trophieshop.trophieshop;

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
import com.trophieshop.trophieshop.service.ProductoMerchandisingService;
import com.trophieshop.trophieshop.service.LogroService;
import com.trophieshop.trophieshop.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
	private LogroRepository logroRepository;

	@Autowired
	private LogroDesbloqueadoRepository logroDesbloqueadoRepository;

	@Autowired
	private CanjeRepository canjeRepository;

	@Autowired
	private ProductoMerchandisingService productoMerchandisingService;

	@Autowired
	private LogroService logroService;

	@Test
	void contextLoads() {
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
	void deleteLogroRemovesRelatedUnlocks() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Borrar Logro User");
		usuario.setEmail("borrar-logro-user@trophieshop.com");
		usuario.setPassword("secret");
		usuario.setRol("USER");
		usuario = usuarioRepository.save(usuario);

		Logro logro = new Logro();
		logro.setNombre("Logro Cascade 3");
		logro.setDescripcion("Descripcion");
		logro.setTipo(TipoLogro.PLATAFORMA);
		logro.setValorMonedas(130);
		logro.setSteamAppId(123L);
		logro = logroRepository.save(logro);

		LogroDesbloqueado desbloqueado = new LogroDesbloqueado();
		desbloqueado.setUsuario(usuario);
		desbloqueado.setLogro(logro);
		logroDesbloqueadoRepository.save(desbloqueado);

		logroService.delete(logro.getId());

		assertThat(logroRepository.findById(logro.getId())).isEmpty();
		assertThat(logroDesbloqueadoRepository.findByLogroId(logro.getId())).isEmpty();
	}

}
