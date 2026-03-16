package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import com.trophieshop.trophieshop.service.LogroService;
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
@RequestMapping("/api/logros")
public class LogroController {

    private final LogroService logroService;

    public LogroController(LogroService logroService) {
        this.logroService = logroService;
    }

    @GetMapping
    public List<Logro> getAll() {
        return logroService.findAll();
    }

    @GetMapping("/{id}")
    public Logro getById(@PathVariable Long id) {
        return logroService.findById(id);
    }

    @PostMapping
    public Logro create(@RequestBody LogroRequest request) {
        return logroService.create(
                request.nombre(),
                request.descripcion(),
                request.tipo(),
                request.valorMonedas(),
                request.videojuegoId());
    }

    @PutMapping("/{id}")
    public Logro update(@PathVariable Long id, @RequestBody LogroRequest request) {
        return logroService.update(
                id,
                request.nombre(),
                request.descripcion(),
                request.tipo(),
                request.valorMonedas(),
                request.videojuegoId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logroService.delete(id);
    }

    public record LogroRequest(String nombre, String descripcion, TipoLogro tipo, Integer valorMonedas, Long videojuegoId) {
    }
}
