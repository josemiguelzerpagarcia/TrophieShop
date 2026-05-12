package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import com.trophieshop.trophieshop.repository.LogroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LogroService {

    private final LogroRepository logroRepository;

    public LogroService(LogroRepository logroRepository) {
        this.logroRepository = logroRepository;
    }

    public List<Logro> findAll() {
        return logroRepository.findAll();
    }

    public Logro findById(Long id) {
        return logroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));
    }

    public Logro create(String nombre, String descripcion, TipoLogro tipo, Integer valorMonedas, Long steamAppId) {
        Logro logro = new Logro();
        logro.setNombre(nombre);
        logro.setDescripcion(descripcion);
        logro.setTipo(tipo);
        logro.setValorMonedas(valorMonedas);
        logro.setSteamAppId(steamAppId);
        return logroRepository.save(logro);
    }

    public Logro update(Long id, String nombre, String descripcion, TipoLogro tipo, Integer valorMonedas, Long steamAppId) {
        Logro logro = findById(id);
        logro.setNombre(nombre);
        logro.setDescripcion(descripcion);
        logro.setTipo(tipo);
        logro.setValorMonedas(valorMonedas);
        logro.setSteamAppId(steamAppId);
        return logroRepository.save(logro);
    }

    public void delete(Long id) {
        Logro logro = findById(id);
        logroRepository.delete(logro);
    }
}
