package com.chamba.demo.model;

import com.chamba.demo.model.enums.EstadoContrato;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratos")
public class Contrato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double montoRetenido;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    @Enumerated(EnumType.STRING)
    private EstadoContrato estado;

    @ManyToOne
    @JoinColumn(name = "trabajo_id")
    private Trabajo trabajo;

    @ManyToOne
    @JoinColumn(name = "contratante_id")
    private Usuario contratante;

    @ManyToOne
    @JoinColumn(name = "trabajador_id")
    private Usuario trabajador;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getMontoRetenido() { return montoRetenido; }
    public void setMontoRetenido(Double montoRetenido) { this.montoRetenido = montoRetenido; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public EstadoContrato getEstado() { return estado; }
    public void setEstado(EstadoContrato estado) { this.estado = estado; }
    public Trabajo getTrabajo() { return trabajo; }
    public void setTrabajo(Trabajo trabajo) { this.trabajo = trabajo; }
    public Usuario getContratante() { return contratante; }
    public void setContratante(Usuario contratante) { this.contratante = contratante; }
    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }
}