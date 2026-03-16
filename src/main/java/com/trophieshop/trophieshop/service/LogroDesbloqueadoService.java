package com.trophieshop.trophieshop.service;

import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.LogroDesbloqueado;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.repository.LogroDesbloqueadoRepository;
import com.trophieshop.trophieshop.repository.LogroRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LogroDesbloqueadoService {

    private final LogroDesbloqueadoRepository desbloqueadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogroRepository logroRepository;

    public LogroDesbloqueadoService(LogroDesbloqueadoRepository desbloqueadoRepository,
                                    UsuarioRepository usuarioRepository,
                                    LogroRepository logroRepository) {
        this.desbloqueadoRepository = desbloqueadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.logroRepository = logroRepository;
    }

    public List<LogroDesbloqueado> findAll() {
        return desbloqueadoRepository.findAll();
    }

    public LogroDesbloqueado findById(Long id) {
        return desbloqueadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de logro no encontrado"));
    }

    @Transactional
    public LogroDesbloqueado create(Long usuarioId, Long logroId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Logro logro = logroRepository.findById(logroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));

        LogroDesbloqueado desbloqueado = new LogroDesbloqueado();
        desbloqueado.setUsuario(usuario);
        desbloqueado.setLogro(logro);
        desbloqueado.setMonedasOtorgadas(logro.getValorMonedas());

        usuario.setMonedasAcumuladas(usuario.getMonedasAcumuladas() + logro.getValorMonedas());
        usuarioRepository.save(usuario);
        return desbloqueadoRepository.save(desbloqueado);
    }

    public void delete(Long id) {
        LogroDesbloqueado desbloqueado = findById(id);
        desbloqueadoRepository.delete(desbloqueado);
    }
}
