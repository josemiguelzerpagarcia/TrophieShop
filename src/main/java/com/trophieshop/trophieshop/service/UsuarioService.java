package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario create(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es requerido");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es requerido");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es requerida");
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con ese email");
        }
        if (usuario.getMonedasAcumuladas() == null) {
            usuario.setMonedasAcumuladas(0);
        }
        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            usuario.setRol("USER");
        }
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(java.time.LocalDateTime.now());
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario update(Long id, Usuario data) {
        Usuario usuario = findById(id);
        usuario.setNombre(data.getNombre());
        usuario.setEmail(data.getEmail());
        if (data.getMonedasAcumuladas() != null) {
            usuario.setMonedasAcumuladas(data.getMonedasAcumuladas());
        }
        if (data.getRol() != null && !data.getRol().isBlank()) {
            usuario.setRol(data.getRol());
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
