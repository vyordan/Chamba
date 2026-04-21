package com.chamba.demo.service;

import com.chamba.demo.model.Postulacion;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.enums.EstadoPostulacion;
import com.chamba.demo.repository.PostulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostulacionService {

    @Autowired
    private PostulacionRepository postulacionRepository;

    public Postulacion postular(Usuario trabajador, Trabajo trabajo, String mensaje) {
        if (postulacionRepository.findByTrabajoAndTrabajador(trabajo, trabajador).isPresent()) {
            throw new RuntimeException("Ya te has postulado a este trabajo");
        }
        Postulacion post = new Postulacion();
        post.setTrabajador(trabajador);
        post.setTrabajo(trabajo);
        post.setMensaje(mensaje);
        post.setFechaPostulacion(LocalDateTime.now());
        post.setEstado(EstadoPostulacion.PENDIENTE);
        return postulacionRepository.save(post);
    }

    public List<Postulacion> listarPorTrabajo(Trabajo trabajo) {
        return postulacionRepository.findByTrabajo(trabajo);
    }

    public List<Postulacion> listarPorTrabajador(Usuario trabajador) {
        return postulacionRepository.findByTrabajador(trabajador);
    }

    public Postulacion obtenerPorId(Long id) {
        return postulacionRepository.findById(id).orElse(null);
    }

    public void aceptarPostulacion(Postulacion postulacion) {
        postulacion.setEstado(EstadoPostulacion.ACEPTADO);
        postulacionRepository.save(postulacion);
        List<Postulacion> otras = postulacionRepository.findByTrabajoAndEstado(postulacion.getTrabajo(), EstadoPostulacion.PENDIENTE);
        for (Postulacion p : otras) {
            if (!p.getId().equals(postulacion.getId())) {
                p.setEstado(EstadoPostulacion.RECHAZADO);
                postulacionRepository.save(p);
            }
        }
    }

    public void rechazarPostulacion(Postulacion postulacion) {
        postulacion.setEstado(EstadoPostulacion.RECHAZADO);
        postulacionRepository.save(postulacion);
    }
}