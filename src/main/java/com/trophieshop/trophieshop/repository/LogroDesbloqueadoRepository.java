package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.LogroDesbloqueado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogroDesbloqueadoRepository extends JpaRepository<LogroDesbloqueado, Long> {
    List<LogroDesbloqueado> findByUsuarioId(Long usuarioId);
    List<LogroDesbloqueado> findByLogroId(Long logroId);
}
