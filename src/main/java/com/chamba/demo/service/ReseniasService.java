package com.chamba.demo.service;

import com.chamba.demo.model.Resenia;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.repository.ReseniasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReseniasService {

    @Autowired
    private ReseniasRepository reseniasRepository;

    public Resenia crearResenia(Usuario autor, Usuario destinatario, Trabajo trabajo, int puntuacion, String comentario) {
        Resenia resenia = new Resenia();
        resenia.setAutor(autor);
        resenia.setDestinatario(destinatario);
        resenia.setTrabajo(trabajo);
        resenia.setPuntuacion(puntuacion);
        resenia.setComentario(comentario);
        resenia.setFecha(LocalDateTime.now());
        return reseniasRepository.save(resenia);
    }

    public List<Resenia> listarPorDestinatario(Usuario destinatario) {
        return reseniasRepository.findByDestinatario(destinatario);
    }

    public double obtenerPromedioPuntuacion(Usuario usuario) {
        List<Resenia> resenias = listarPorDestinatario(usuario);
        if (resenias.isEmpty()) return 0.0;
        double suma = resenias.stream().mapToInt(Resenia::getPuntuacion).sum();
        return suma / resenias.size();
    }
}