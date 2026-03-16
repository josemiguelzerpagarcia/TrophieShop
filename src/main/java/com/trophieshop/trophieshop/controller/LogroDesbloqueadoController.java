package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.LogroDesbloqueado;
import com.trophieshop.trophieshop.service.LogroDesbloqueadoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logros-desbloqueados")
public class LogroDesbloqueadoController {

    private final LogroDesbloqueadoService desbloqueadoService;

    public LogroDesbloqueadoController(LogroDesbloqueadoService desbloqueadoService) {
        this.desbloqueadoService = desbloqueadoService;
    }

    @GetMapping
    public List<LogroDesbloqueado> getAll() {
        return desbloqueadoService.findAll();
    }

    @GetMapping("/{id}")
    public LogroDesbloqueado getById(@PathVariable Long id) {
        return desbloqueadoService.findById(id);
    }

    @PostMapping
    public LogroDesbloqueado create(@RequestBody LogroDesbloqueadoRequest request) {
        return desbloqueadoService.create(request.usuarioId(), request.logroId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        desbloqueadoService.delete(id);
    }

    public record LogroDesbloqueadoRequest(Long usuarioId, Long logroId) {
    }
}
