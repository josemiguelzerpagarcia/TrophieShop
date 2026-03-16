package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Canje;
import com.trophieshop.trophieshop.service.CanjeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/canjes")
public class CanjeController {

    private final CanjeService canjeService;

    public CanjeController(CanjeService canjeService) {
        this.canjeService = canjeService;
    }

    @GetMapping
    public List<Canje> getAll() {
        return canjeService.findAll();
    }

    @GetMapping("/{id}")
    public Canje getById(@PathVariable Long id) {
        return canjeService.findById(id);
    }

    @PostMapping
    public Canje create(@RequestBody CanjeRequest request) {
        return canjeService.create(request.usuarioId(), request.productoId(), request.cantidad());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        canjeService.delete(id);
    }

    public record CanjeRequest(Long usuarioId, Long productoId, Integer cantidad) {
    }
}
