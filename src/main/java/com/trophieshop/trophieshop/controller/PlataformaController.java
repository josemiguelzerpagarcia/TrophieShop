package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Plataforma;
import com.trophieshop.trophieshop.service.PlataformaService;
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
@RequestMapping("/api/plataformas")
public class PlataformaController {

    private final PlataformaService plataformaService;

    public PlataformaController(PlataformaService plataformaService) {
        this.plataformaService = plataformaService;
    }

    @GetMapping
    public List<Plataforma> getAll() {
        return plataformaService.findAll();
    }

    @GetMapping("/{id}")
    public Plataforma getById(@PathVariable Long id) {
        return plataformaService.findById(id);
    }

    @PostMapping
    public Plataforma create(@RequestBody Plataforma plataforma) {
        return plataformaService.create(plataforma);
    }

    @PutMapping("/{id}")
    public Plataforma update(@PathVariable Long id, @RequestBody Plataforma plataforma) {
        return plataformaService.update(id, plataforma);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        plataformaService.delete(id);
    }
}
