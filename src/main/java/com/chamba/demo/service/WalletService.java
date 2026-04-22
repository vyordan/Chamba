package com.chamba.demo.service;

import com.chamba.demo.model.Transaccion;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.Wallet;
import com.chamba.demo.repository.TransaccionRepository;
import com.chamba.demo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Transactional
    public void recargar(Usuario usuario, Double monto) {
        Wallet wallet = usuario.getWallet();
        wallet.setSaldo(wallet.getSaldo() + monto);
        walletRepository.save(wallet);

        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(monto);
        trans.setTipo("TOPUP");
        trans.setDescripcion("Recarga de saldo");
        trans.setUsuarioDestino(usuario);
        transaccionRepository.save(trans);
    }

    @Transactional
    public void retirar(Usuario usuario, Double monto) {
        Wallet wallet = usuario.getWallet();
        if (wallet.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente");
        }
        wallet.setSaldo(wallet.getSaldo() - monto);
        walletRepository.save(wallet);

        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(monto);
        trans.setTipo("WITHDRAW");
        trans.setDescripcion("Retiro simulado");
        trans.setUsuarioOrigen(usuario);
        transaccionRepository.save(trans);
    }

    public boolean tieneSaldoSuficiente(Usuario usuario, Double monto) {
        return usuario.getWallet().getSaldo() >= monto;
    }

    @Transactional
    public void retener(Usuario usuario, Double monto, String descripcion) {
        Wallet wallet = usuario.getWallet();
        wallet.setSaldo(wallet.getSaldo() - monto);
        walletRepository.save(wallet);

        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(monto);
        trans.setTipo("RETENCION");
        trans.setDescripcion(descripcion);
        trans.setUsuarioOrigen(usuario);
        transaccionRepository.save(trans);
    }

    @Transactional
    public void devolver(Usuario usuario, Double monto, String descripcion, Trabajo trabajo) {
        Wallet wallet = usuario.getWallet();
        wallet.setSaldo(wallet.getSaldo() + monto);
        walletRepository.save(wallet);

        Transaccion trans = new Transaccion();
        trans.setFecha(LocalDateTime.now());
        trans.setMonto(monto);
        trans.setTipo("DEVOLUCION");
        trans.setDescripcion(descripcion);
        trans.setUsuarioDestino(usuario);
        trans.setTrabajo(trabajo);
        transaccionRepository.save(trans);
    }
}