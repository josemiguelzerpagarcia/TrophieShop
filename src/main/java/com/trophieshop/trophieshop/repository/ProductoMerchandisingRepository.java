package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.ProductoMerchandising;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoMerchandisingRepository extends JpaRepository<ProductoMerchandising, Long> {
    Optional<ProductoMerchandising> findByNombre(String nombre);
}
