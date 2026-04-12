package com.trophieshop.trophieshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "monedas_acumuladas", nullable = false)
    private Integer monedasAcumuladas = 0;

    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'USER'")
    private String rol = "USER";

    @Column(name = "steam_id", unique = true, length = 30)
    private String steamId;

    @Column(name = "steam_persona_name", length = 120)
    private String steamPersonaName;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Videojuego> videojuegos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Canje> canjes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<LogroDesbloqueado> logrosDesbloqueados = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<SteamLogroOtorgado> steamLogrosOtorgados = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMonedasAcumuladas() {
        return monedasAcumuladas;
    }

    public void setMonedasAcumuladas(Integer monedasAcumuladas) {
        this.monedasAcumuladas = monedasAcumuladas;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public String getSteamPersonaName() {
        return steamPersonaName;
    }

    public void setSteamPersonaName(String steamPersonaName) {
        this.steamPersonaName = steamPersonaName;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Videojuego> getVideojuegos() {
        return videojuegos;
    }

    public void setVideojuegos(List<Videojuego> videojuegos) {
        this.videojuegos = videojuegos;
    }

    public List<Canje> getCanjes() {
        return canjes;
    }

    public void setCanjes(List<Canje> canjes) {
        this.canjes = canjes;
    }

    public List<LogroDesbloqueado> getLogrosDesbloqueados() {
        return logrosDesbloqueados;
    }

    public void setLogrosDesbloqueados(List<LogroDesbloqueado> logrosDesbloqueados) {
        this.logrosDesbloqueados = logrosDesbloqueados;
    }

    public List<SteamLogroOtorgado> getSteamLogrosOtorgados() {
        return steamLogrosOtorgados;
    }

    public void setSteamLogrosOtorgados(List<SteamLogroOtorgado> steamLogrosOtorgados) {
        this.steamLogrosOtorgados = steamLogrosOtorgados;
    }
}
