package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import com.trophieshop.trophieshop.repository.ProductoMerchandisingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoMerchandisingService {

    private final ProductoMerchandisingRepository productoRepository;

    public ProductoMerchandisingService(ProductoMerchandisingRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoMerchandising> findAll() {
        return productoRepository.findAll();
    }

    public ProductoMerchandising findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public ProductoMerchandising create(ProductoMerchandising producto) {
        validate(producto);
        producto.setNombre(producto.getNombre().trim());
        if (producto.getDescripcion() != null) {
            producto.setDescripcion(producto.getDescripcion().trim());
        }
        return productoRepository.save(producto);
    }

    public ProductoMerchandising update(Long id, ProductoMerchandising data) {
        ProductoMerchandising producto = findById(id);
        validate(data);
        producto.setNombre(data.getNombre().trim());
        producto.setDescripcion(data.getDescripcion() == null ? null : data.getDescripcion().trim());
        producto.setStock(data.getStock());
        producto.setCostoMonedas(data.getCostoMonedas());
        return productoRepository.save(producto);
    }

    public void delete(Long id) {
        ProductoMerchandising producto = findById(id);
        productoRepository.delete(producto);
    }

    private void validate(ProductoMerchandising producto) {
        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto no puede ser nulo");
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto no puede estar vacío");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock del producto debe ser mayor o igual a 0");
        }

        if (producto.getCostoMonedas() == null || producto.getCostoMonedas() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo en monedas debe ser mayor o igual a 0");
        }
    }
}
