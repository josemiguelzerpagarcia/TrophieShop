package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.Videojuego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideojuegoRepository extends JpaRepository<Videojuego, Long> {
    List<Videojuego> findByUsuarioId(Long usuarioId);
    List<Videojuego> findByPlataformaId(Long plataformaId);
}
