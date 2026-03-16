package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Plataforma;
import com.trophieshop.trophieshop.repository.PlataformaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlataformaService {

    private final PlataformaRepository plataformaRepository;

    public PlataformaService(PlataformaRepository plataformaRepository) {
        this.plataformaRepository = plataformaRepository;
    }

    public List<Plataforma> findAll() {
        return plataformaRepository.findAll();
    }

    public Plataforma findById(Long id) {
        return plataformaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plataforma no encontrada"));
    }

    public Plataforma create(Plataforma plataforma) {
        return plataformaRepository.save(plataforma);
    }

    public Plataforma update(Long id, Plataforma data) {
        Plataforma plataforma = findById(id);
        plataforma.setNombre(data.getNombre());
        return plataformaRepository.save(plataforma);
    }

    public void delete(Long id) {
        Plataforma plataforma = findById(id);
        plataformaRepository.delete(plataforma);
    }
}
