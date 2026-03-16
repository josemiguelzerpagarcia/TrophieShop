package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con ese email");
        }
        if (usuario.getMonedasAcumuladas() == null) {
            usuario.setMonedasAcumuladas(0);
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
        return usuarioRepository.save(usuario);
    }

    public void delete(Long id) {
        Usuario usuario = findById(id);
        usuarioRepository.delete(usuario);
    }
}
