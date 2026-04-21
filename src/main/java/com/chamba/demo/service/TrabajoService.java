package com.chamba.demo.service;

import com.chamba.demo.model.Contrato;
import com.chamba.demo.model.Postulacion;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Transaccion;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.Wallet;
import com.chamba.demo.model.enums.EstadoContrato;
import com.chamba.demo.model.enums.EstadoPostulacion;
import com.chamba.demo.model.enums.EstadoTrabajo;
import com.chamba.demo.repository.ContratoRepository;
import com.chamba.demo.repository.TrabajoRepository;
import com.chamba.demo.repository.TransaccionRepository;
import com.chamba.demo.repository.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransaccionRepository transaccionRepository;

    public Trabajo publicar(Usuario contratante, String titulo, String descripcion, String ubicacion, Double monto) {
        // 1. Verificar saldo suficiente
        if (!walletService.tieneSaldoSuficiente(contratante, monto)) {
            throw new RuntimeException("Saldo insuficiente. Necesitas Q" + monto + " para publicar este trabajo.");
        }
        
        // 2. Descontar del contratante
        Wallet wallet = contratante.getWallet();
        wallet.setSaldo(wallet.getSaldo() - monto);
        walletRepository.save(wallet);
        
        // 3. Registrar transacción de retención
        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(monto);
        trans.setTipo("RETENCION_PUBLICACION");
        trans.setDescripcion("Monto retenido al publicar trabajo: " + titulo);
        trans.setUsuarioOrigen(contratante);
        transaccionRepository.save(trans);
        
        // 4. Crear el trabajo
        Trabajo trabajo = new Trabajo();
        trabajo.setTitulo(titulo);
        trabajo.setDescripcion(descripcion);
        trabajo.setUbicacion(ubicacion);
        trabajo.setMonto(monto);
        trabajo.setFechaPublicacion(LocalDateTime.now());
        trabajo.setEstado(EstadoTrabajo.ABIERTO);
        trabajo.setContratante(contratante);
        trabajo = trabajoRepository.save(trabajo);
        
        // 5. Crear un contrato inicial sin trabajador (estado PENDIENTE_ASIGNACION)
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

public void cancelarTrabajo(Trabajo trabajo) {
    // Buscar el contrato asociado
    Contrato contrato = contratoRepository.findByTrabajo(trabajo)
        .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
    
    // Solo se puede cancelar si está pendiente de asignación (aún sin trabajador)
    if (contrato.getEstado() == EstadoContrato.PENDIENTE_ASIGNACION) {
        // Devolver el dinero al contratante
        Usuario contratante = trabajo.getContratante();
        Wallet wallet = contratante.getWallet();
        wallet.setSaldo(wallet.getSaldo() + contrato.getMontoRetenido());
        walletRepository.save(wallet);
        
        // Registrar transacción de devolución
        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(contrato.getMontoRetenido());
        trans.setTipo("DEVOLUCION_CANCELACION");
        trans.setDescripcion("Devolución por cancelación de trabajo: " + trabajo.getTitulo());
        trans.setUsuarioDestino(contratante);
        trans.setTrabajo(trabajo);
        transaccionRepository.save(trans);
        
        // Actualizar estados
        contrato.setEstado(EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);
        trabajo.setEstado(EstadoTrabajo.CANCELADO);
        trabajoRepository.save(trabajo);
    } else {
        throw new RuntimeException("No se puede cancelar un trabajo que ya tiene un trabajador asignado");
    }
}
}