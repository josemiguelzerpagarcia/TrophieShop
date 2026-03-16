package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.Plataforma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {
    Optional<Plataforma> findByNombre(String nombre);
}
