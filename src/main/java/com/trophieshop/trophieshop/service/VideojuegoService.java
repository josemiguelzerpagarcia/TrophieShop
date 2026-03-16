package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Plataforma;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.entity.Videojuego;
import com.trophieshop.trophieshop.repository.PlataformaRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import com.trophieshop.trophieshop.repository.VideojuegoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VideojuegoService {

    private final VideojuegoRepository videojuegoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlataformaRepository plataformaRepository;

    public VideojuegoService(VideojuegoRepository videojuegoRepository,
                             UsuarioRepository usuarioRepository,
                             PlataformaRepository plataformaRepository) {
        this.videojuegoRepository = videojuegoRepository;
        this.usuarioRepository = usuarioRepository;
        this.plataformaRepository = plataformaRepository;
    }

    public List<Videojuego> findAll() {
        return videojuegoRepository.findAll();
    }

    public Videojuego findById(Long id) {
        return videojuegoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Videojuego no encontrado"));
    }

    public Videojuego create(String titulo, Long usuarioId, Long plataformaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Plataforma plataforma = plataformaRepository.findById(plataformaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plataforma no encontrada"));

        Videojuego videojuego = new Videojuego();
        videojuego.setTitulo(titulo);
        videojuego.setUsuario(usuario);
        videojuego.setPlataforma(plataforma);
        return videojuegoRepository.save(videojuego);
    }

    public Videojuego update(Long id, String titulo, Long usuarioId, Long plataformaId) {
        Videojuego videojuego = findById(id);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Plataforma plataforma = plataformaRepository.findById(plataformaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plataforma no encontrada"));

        videojuego.setTitulo(titulo);
        videojuego.setUsuario(usuario);
        videojuego.setPlataforma(plataforma);
        return videojuegoRepository.save(videojuego);
    }

    public void delete(Long id) {
        Videojuego videojuego = findById(id);
        videojuegoRepository.delete(videojuego);
    }
}
