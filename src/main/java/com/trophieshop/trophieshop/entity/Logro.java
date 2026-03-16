package com.trophieshop.trophieshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trophieshop.trophieshop.entity.enums.TipoLogro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logros")
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 400)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLogro tipo;

    @Column(name = "valor_monedas", nullable = false)
    private Integer valorMonedas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    @OneToMany(mappedBy = "logro")
    @JsonIgnore
    private List<LogroDesbloqueado> desbloqueos = new ArrayList<>();

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoLogro getTipo() {
        return tipo;
    }

    public void setTipo(TipoLogro tipo) {
        this.tipo = tipo;
    }

    public Integer getValorMonedas() {
        return valorMonedas;
    }

    public void setValorMonedas(Integer valorMonedas) {
        this.valorMonedas = valorMonedas;
    }

    public Videojuego getVideojuego() {
        return videojuego;
    }

    public void setVideojuego(Videojuego videojuego) {
        this.videojuego = videojuego;
    }

    public List<LogroDesbloqueado> getDesbloqueos() {
        return desbloqueos;
    }

    public void setDesbloqueos(List<LogroDesbloqueado> desbloqueos) {
        this.desbloqueos = desbloqueos;
    }
}
