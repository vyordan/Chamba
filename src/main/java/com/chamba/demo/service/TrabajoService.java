package com.chamba.demo.service;

import com.chamba.demo.model.Postulacion;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.enums.EstadoPostulacion;
import com.chamba.demo.model.enums.EstadoTrabajo;
import com.chamba.demo.repository.TrabajoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrabajoService {

    @Autowired
    private TrabajoRepository trabajoRepository;

    public Trabajo publicar(Usuario contratante, String titulo, String descripcion, String ubicacion, Double monto) {
        Trabajo trabajo = new Trabajo();
        trabajo.setTitulo(titulo);
        trabajo.setDescripcion(descripcion);
        trabajo.setUbicacion(ubicacion);
        trabajo.setMonto(monto);
        trabajo.setFechaPublicacion(LocalDateTime.now());
        trabajo.setEstado(EstadoTrabajo.ABIERTO);
        trabajo.setContratante(contratante);
        return trabajoRepository.save(trabajo);
    }

    public List<Trabajo> listarAbiertos() {
        return trabajoRepository.findByEstado(EstadoTrabajo.ABIERTO);
    }

    public List<Trabajo> listarPorContratante(Usuario contratante) {
        return trabajoRepository.findByContratante(contratante);
    }

    public Trabajo obtenerPorId(Long id) {
        return trabajoRepository.findById(id).orElse(null);
    }

    public void cerrarTrabajo(Trabajo trabajo) {
        trabajo.setEstado(EstadoTrabajo.CERRADO);
        trabajoRepository.save(trabajo);
    }

    public void completarTrabajo(Trabajo trabajo) {
        trabajo.setEstado(EstadoTrabajo.COMPLETADO);
        trabajoRepository.save(trabajo);
    }

    public void cancelarTrabajo(Trabajo trabajo) {
        trabajo.setEstado(EstadoTrabajo.CERRADO); // en lugar de CANCELADO
        trabajoRepository.save(trabajo);
    }
}