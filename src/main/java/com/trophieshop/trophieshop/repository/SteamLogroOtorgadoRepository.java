package com.trophieshop.trophieshop.repository;

import com.trophieshop.trophieshop.entity.SteamLogroOtorgado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SteamLogroOtorgadoRepository extends JpaRepository<SteamLogroOtorgado, Long> {
    boolean existsByUsuarioIdAndAppIdAndAchievementApiName(Long usuarioId, Long appId, String achievementApiName);
    List<SteamLogroOtorgado> findByUsuarioIdOrderByFechaOtorgadoDesc(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}
