package com.chamba.demo.controller;

import com.chamba.demo.model.Usuario;
import com.chamba.demo.service.UsuariosService;
import com.chamba.demo.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UsuariosService usuariosService;

    @GetMapping
    public String verWallet(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        if (userId == null) return "redirect:/login";
        Usuario usuario = usuariosService.obtenerPorId(userId);
        model.addAttribute("saldo", usuario.getWallet().getSaldo());
        return "wallet";
    }

    @PostMapping("/topup")
    public String recargar(@RequestParam Double monto, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        if (userId == null) return "redirect:/login";
        Usuario usuario = usuariosService.obtenerPorId(userId);
        walletService.recargar(usuario, monto);
        return "redirect:/wallet";
    }

    @PostMapping("/withdraw")
    public String retirar(@RequestParam Double monto, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("usuarioId");
        if (userId == null) return "redirect:/login";
        Usuario usuario = usuariosService.obtenerPorId(userId);
        try {
            walletService.retirar(usuario, monto);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("saldo", usuario.getWallet().getSaldo());
            return "wallet";
        }
        return "redirect:/wallet";
    }
}