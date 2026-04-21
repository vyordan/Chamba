package com.chamba.demo.controller;

import com.chamba.demo.model.Contrato;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.repository.ContratoRepository;
import com.chamba.demo.service.PagoService;
import com.chamba.demo.service.UsuariosService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/contratos")
public class ContratoContoller {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private PagoService pagoService;

    @GetMapping("/mis-contratos")
    public String misContratos(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Usuario usuario = usuariosService.obtenerPorId(userId);
        List<Contrato> comoContratante = contratoRepository.findByContratante(usuario);
        List<Contrato> comoTrabajador = contratoRepository.findByTrabajador(usuario);
        model.addAttribute("comoContratante", comoContratante);
        model.addAttribute("comoTrabajador", comoTrabajador);
        return "contratos/mis-contratos";
    }

    @PostMapping("/{id}/completar")
    public String completarTrabajo(@PathVariable Long id, HttpSession session) {
        Contrato contrato = contratoRepository.findById(id).orElse(null);
        if (contrato != null) {
            Long userId = (Long) session.getAttribute("usuarioId");
            if (contrato.getContratante().getId().equals(userId)) {
                pagoService.liberarPago(contrato);
            }
        }
        return "redirect:/contratos/mis-contratos";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarContrato(@PathVariable Long id, HttpSession session) {
        Contrato contrato = contratoRepository.findById(id).orElse(null);
        if (contrato != null) {
            Long userId = (Long) session.getAttribute("usuarioId");
            if (contrato.getContratante().getId().equals(userId)) {
                pagoService.cancelarPago(contrato);
            }
        }
        return "redirect:/contratos/mis-contratos";
    }
}