package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.Videojuego;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import com.trophieshop.trophieshop.repository.LogroRepository;
import com.trophieshop.trophieshop.repository.VideojuegoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LogroService {

    private final LogroRepository logroRepository;
    private final VideojuegoRepository videojuegoRepository;

    public LogroService(LogroRepository logroRepository, VideojuegoRepository videojuegoRepository) {
        this.logroRepository = logroRepository;
        this.videojuegoRepository = videojuegoRepository;
    }

    public List<Logro> findAll() {
        return logroRepository.findAll();
    }

    public Logro findById(Long id) {
        return logroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));
    }

    public Logro create(String nombre, String descripcion, TipoLogro tipo, Integer valorMonedas, Long videojuegoId) {
        Videojuego videojuego = videojuegoRepository.findById(videojuegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Videojuego no encontrado"));

        Logro logro = new Logro();
        logro.setNombre(nombre);
        logro.setDescripcion(descripcion);
        logro.setTipo(tipo);
        logro.setValorMonedas(valorMonedas);
        logro.setVideojuego(videojuego);
        return logroRepository.save(logro);
    }

    public Logro update(Long id, String nombre, String descripcion, TipoLogro tipo, Integer valorMonedas, Long videojuegoId) {
        Logro logro = findById(id);
        Videojuego videojuego = videojuegoRepository.findById(videojuegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Videojuego no encontrado"));

        logro.setNombre(nombre);
        logro.setDescripcion(descripcion);
        logro.setTipo(tipo);
        logro.setValorMonedas(valorMonedas);
        logro.setVideojuego(videojuego);
        return logroRepository.save(logro);
    }

    public void delete(Long id) {
        Logro logro = findById(id);
        logroRepository.delete(logro);
    }
}
