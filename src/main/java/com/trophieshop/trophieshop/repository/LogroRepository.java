package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.Logro;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogroRepository extends JpaRepository<Logro, Long> {
    List<Logro> findBySteamAppId(Long steamAppId);
    List<Logro> findBySteamAppIdOrderByIdAsc(Long steamAppId);
    List<Logro> findByTipo(TipoLogro tipo);
    void deleteBySteamAppId(Long steamAppId);
}
