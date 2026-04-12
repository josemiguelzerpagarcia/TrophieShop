package com.trophieshop.trophieshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "steam_logros_otorgados",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_app_achievement", columnNames = {"usuario_id", "app_id", "achievement_api_name"})
        }
)
public class SteamLogroOtorgado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "game_name", nullable = false, length = 200)
    private String gameName;

    @Column(name = "achievement_api_name", nullable = false, length = 200)
    private String achievementApiName;

    @Column(name = "achievement_display_name", length = 200)
    private String achievementDisplayName;

    @Column(name = "rarity_percent", nullable = false)
    private Double rarityPercent;

    @Column(name = "rarity_type", nullable = false, length = 20)
    private String rarityType;

    @Column(name = "puntos_otorgados", nullable = false)
    private Integer puntosOtorgados;

    @Column(name = "fecha_otorgado", nullable = false)
    private LocalDateTime fechaOtorgado = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getAchievementApiName() {
        return achievementApiName;
    }

    public void setAchievementApiName(String achievementApiName) {
        this.achievementApiName = achievementApiName;
    }

    public String getAchievementDisplayName() {
        return achievementDisplayName;
    }

    public void setAchievementDisplayName(String achievementDisplayName) {
        this.achievementDisplayName = achievementDisplayName;
    }

    public Double getRarityPercent() {
        return rarityPercent;
    }

    public void setRarityPercent(Double rarityPercent) {
        this.rarityPercent = rarityPercent;
    }

    public String getRarityType() {
        return rarityType;
    }

    public void setRarityType(String rarityType) {
        this.rarityType = rarityType;
    }

    public Integer getPuntosOtorgados() {
        return puntosOtorgados;
    }

    public void setPuntosOtorgados(Integer puntosOtorgados) {
        this.puntosOtorgados = puntosOtorgados;
    }

    public LocalDateTime getFechaOtorgado() {
        return fechaOtorgado;
    }

    public void setFechaOtorgado(LocalDateTime fechaOtorgado) {
        this.fechaOtorgado = fechaOtorgado;
    }
}
