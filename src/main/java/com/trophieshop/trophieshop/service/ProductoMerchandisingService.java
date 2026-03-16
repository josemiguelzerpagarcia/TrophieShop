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
        return productoRepository.save(producto);
    }

    public ProductoMerchandising update(Long id, ProductoMerchandising data) {
        ProductoMerchandising producto = findById(id);
        producto.setNombre(data.getNombre());
        producto.setDescripcion(data.getDescripcion());
        producto.setStock(data.getStock());
        producto.setCostoMonedas(data.getCostoMonedas());
        return productoRepository.save(producto);
    }

    public void delete(Long id) {
        ProductoMerchandising producto = findById(id);
        productoRepository.delete(producto);
    }
}
