package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import com.trophieshop.trophieshop.service.ProductoMerchandisingService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoMerchandisingController {

    private final ProductoMerchandisingService productoService;

    public ProductoMerchandisingController(ProductoMerchandisingService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoMerchandising> getAll() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    public ProductoMerchandising getById(@PathVariable Long id) {
        return productoService.findById(id);
    }

    @PostMapping
    public ProductoMerchandising create(@RequestBody ProductoMerchandising producto) {
        return productoService.create(producto);
    }

    @PutMapping("/{id}")
    public ProductoMerchandising update(@PathVariable Long id, @RequestBody ProductoMerchandising producto) {
        return productoService.update(id, producto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productoService.delete(id);
    }
}
