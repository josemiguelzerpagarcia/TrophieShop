package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Videojuego;
import com.trophieshop.trophieshop.service.VideojuegoService;
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
@RequestMapping("/api/videojuegos")
public class VideojuegoController {

    private final VideojuegoService videojuegoService;

    public VideojuegoController(VideojuegoService videojuegoService) {
        this.videojuegoService = videojuegoService;
    }

    @GetMapping
    public List<Videojuego> getAll() {
        return videojuegoService.findAll();
    }

    @GetMapping("/{id}")
    public Videojuego getById(@PathVariable Long id) {
        return videojuegoService.findById(id);
    }

    @PostMapping
    public Videojuego create(@RequestBody VideojuegoRequest request) {
        return videojuegoService.create(request.titulo(), request.usuarioId(), request.plataformaId());
    }

    @PutMapping("/{id}")
    public Videojuego update(@PathVariable Long id, @RequestBody VideojuegoRequest request) {
        return videojuegoService.update(id, request.titulo(), request.usuarioId(), request.plataformaId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        videojuegoService.delete(id);
    }

    public record VideojuegoRequest(String titulo, Long usuarioId, Long plataformaId) {
    }
}
