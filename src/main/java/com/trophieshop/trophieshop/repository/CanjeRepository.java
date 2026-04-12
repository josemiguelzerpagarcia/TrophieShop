package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.Canje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CanjeRepository extends JpaRepository<Canje, Long> {
    List<Canje> findByUsuarioId(Long usuarioId);
    List<Canje> findByProductoId(Long productoId);
    void deleteByUsuarioId(Long usuarioId);
}
