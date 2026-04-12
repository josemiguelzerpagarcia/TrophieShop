package com.trophieshop.trophieshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos_merchandising")
public class ProductoMerchandising {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "costo_monedas", nullable = false)
    private Integer costoMonedas;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Canje> canjes = new ArrayList<>();

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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getCostoMonedas() {
        return costoMonedas;
    }

    public void setCostoMonedas(Integer costoMonedas) {
        this.costoMonedas = costoMonedas;
    }

    public List<Canje> getCanjes() {
        return canjes;
    }

    public void setCanjes(List<Canje> canjes) {
        this.canjes = canjes;
    }
}
