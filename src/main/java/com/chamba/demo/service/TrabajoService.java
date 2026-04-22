package com.chamba.demo.service;

import com.chamba.demo.model.Contrato;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.enums.EstadoContrato;
import com.chamba.demo.model.enums.EstadoTrabajo;
import com.chamba.demo.repository.ContratoRepository;
import com.chamba.demo.repository.TrabajoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrabajoService {

    @Autowired
    private TrabajoRepository trabajoRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private ContratoRepository contratoRepository;

    @Transactional
    public Trabajo publicar(Usuario contratante, String titulo, String descripcion, String ubicacion, Double monto) {
        if (!walletService.tieneSaldoSuficiente(contratante, monto)) {
            throw new RuntimeException("Saldo insuficiente. Necesitas Q" + monto + " para publicar este trabajo.");
        }

        // Descontar saldo y registrar transacción via WalletService
        walletService.retener(contratante, monto, "Monto retenido al publicar trabajo: " + titulo);

        // Crear el trabajo
        Trabajo trabajo = new Trabajo();
        trabajo.setTitulo(titulo);
        trabajo.setDescripcion(descripcion);
        trabajo.setUbicacion(ubicacion);
        trabajo.setMonto(monto);
        trabajo.setFechaPublicacion(LocalDateTime.now());
        trabajo.setEstado(EstadoTrabajo.ABIERTO);
        trabajo.setContratante(contratante);
        trabajo = trabajoRepository.save(trabajo);

        // Crear contrato inicial sin trabajador
        Contrato contrato = new Contrato();
        contrato.setTrabajo(trabajo);
        contrato.setContratante(contratante);
        contrato.setMontoRetenido(monto);
        contrato.setFechaInicio(LocalDateTime.now());
        contrato.setEstado(EstadoContrato.PENDIENTE_ASIGNACION);
        contratoRepository.save(contrato);

        return trabajo;
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

    @Transactional
    public void cancelarTrabajo(Trabajo trabajo) {
        Contrato contrato = contratoRepository.findByTrabajo(trabajo)
            .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        if (contrato.getEstado() != EstadoContrato.PENDIENTE_ASIGNACION) {
            throw new RuntimeException("No se puede cancelar un trabajo que ya tiene un trabajador asignado");
        }

        // Devolver el dinero via WalletService
        walletService.devolver(trabajo.getContratante(), contrato.getMontoRetenido(),
            "Devolución por cancelación de trabajo: " + trabajo.getTitulo(), trabajo);

        contrato.setEstado(EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);
        trabajo.setEstado(EstadoTrabajo.CANCELADO);
        trabajoRepository.save(trabajo);
    }
}