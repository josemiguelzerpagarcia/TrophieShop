package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Canje;
import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.repository.CanjeRepository;
import com.trophieshop.trophieshop.repository.ProductoMerchandisingRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CanjeService {

    private final CanjeRepository canjeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoMerchandisingRepository productoRepository;

    public CanjeService(CanjeRepository canjeRepository,
                        UsuarioRepository usuarioRepository,
                        ProductoMerchandisingRepository productoRepository) {
        this.canjeRepository = canjeRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<Canje> findAll() {
        return canjeRepository.findAll();
    }

    public Canje findById(Long id) {
        return canjeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Canje no encontrado"));
    }

    @Transactional
    public Canje create(Long usuarioId, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor que 0");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        ProductoMerchandising producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        int totalMonedas = producto.getCostoMonedas() * cantidad;
        if (producto.getStock() < cantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay stock suficiente");
        }
        if (usuario.getMonedasAcumuladas() < totalMonedas) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monedas insuficientes");
        }

        producto.setStock(producto.getStock() - cantidad);
        usuario.setMonedasAcumuladas(usuario.getMonedasAcumuladas() - totalMonedas);

        Canje canje = new Canje();
        canje.setUsuario(usuario);
        canje.setProducto(producto);
        canje.setCantidad(cantidad);
        canje.setTotalMonedas(totalMonedas);

        productoRepository.save(producto);
        usuarioRepository.save(usuario);
        return canjeRepository.save(canje);
    }

    public void delete(Long id) {
        Canje canje = findById(id);
        canjeRepository.delete(canje);
    }
}
