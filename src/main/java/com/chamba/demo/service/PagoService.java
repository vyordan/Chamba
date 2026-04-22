package com.chamba.demo.service;

import com.chamba.demo.model.*;
import com.chamba.demo.model.enums.EstadoContrato;
import com.chamba.demo.model.enums.EstadoTrabajo;
import com.chamba.demo.repository.ContratoRepository;
import com.chamba.demo.repository.TrabajoRepository;
import com.chamba.demo.repository.TransaccionRepository;
import com.chamba.demo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PagoService {

    private static final double COMISION = 0.05;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private TrabajoRepository trabajoRepository;

    @Transactional
    public void liberarPago(Contrato contrato) {
        if (!contrato.getEstado().equals(EstadoContrato.EN_PROGRESO)) {
            throw new RuntimeException("El contrato no está en progreso");
        }
        Double monto = contrato.getMontoRetenido();
        Double comision = monto * COMISION;
        Double neto = monto - comision;

        Usuario trabajador = contrato.getTrabajador();
        Wallet walletTrabajador = trabajador.getWallet();
        walletTrabajador.setSaldo(walletTrabajador.getSaldo() + neto);
        walletRepository.save(walletTrabajador);

        Transaccion transComision = new Transaccion();
        transComision.setFecha(LocalDateTime.now());
        transComision.setMonto(comision);
        transComision.setTipo("COMISION");
        transComision.setDescripcion("Comisión de Chamba por trabajo completado");
        transComision.setUsuarioOrigen(contrato.getContratante());
        transComision.setTrabajo(contrato.getTrabajo());
        transaccionRepository.save(transComision);

        Transaccion transLiberacion = new Transaccion();
        transLiberacion.setFecha(LocalDateTime.now());
        transLiberacion.setMonto(neto);
        transLiberacion.setTipo("LIBERACION");
        transLiberacion.setDescripcion("Pago liberado al trabajador");
        transLiberacion.setUsuarioDestino(trabajador);
        transLiberacion.setTrabajo(contrato.getTrabajo());
        transaccionRepository.save(transLiberacion);

        contrato.setEstado(EstadoContrato.COMPLETADO);
        contrato.setFechaFin(LocalDateTime.now());
        contratoRepository.save(contrato);

        Trabajo trabajo = contrato.getTrabajo();
        trabajo.setEstado(EstadoTrabajo.COMPLETADO);
        trabajoRepository.save(trabajo);
    }

    @Transactional
    public void cancelarPago(Contrato contrato) {
        if (!contrato.getEstado().equals(EstadoContrato.EN_PROGRESO)) {
            return;
        }
        Usuario contratante = contrato.getContratante();
        Wallet walletContratante = contratante.getWallet();
        walletContratante.setSaldo(walletContratante.getSaldo() + contrato.getMontoRetenido());
        walletRepository.save(walletContratante);

        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(contrato.getMontoRetenido());
        trans.setTipo("CANCELACION");
        trans.setDescripcion("Pago cancelado, devolución al contratante");
        trans.setUsuarioDestino(contratante);
        trans.setTrabajo(contrato.getTrabajo());
        transaccionRepository.save(trans);

        contrato.setEstado(EstadoContrato.CANCELADO);
        contratoRepository.save(contrato);

        Trabajo trabajo = contrato.getTrabajo();
        trabajo.setEstado(EstadoTrabajo.CANCELADO);
        trabajoRepository.save(trabajo);
    }
}