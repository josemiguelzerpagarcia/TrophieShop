package com.trophieshop.trophieshop.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {

    @GetMapping({"/", "/catalogo", "/catalago", "/detalle/{tipo}/{id}", "/acceso", "/olvide-password"})
    public String guestPages() {
        return "forward:/ui/guest.html";
    }

    @GetMapping({"/usuario", "/usuario/dashboard", "/usuario/perfil", "/usuario/carrito", "/usuario/canjes", "/usuario/logros", "/usuario/configuracion"})
    public String userPages(HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/acceso";
        }
        return "forward:/ui/user.html";
    }

    @GetMapping({"/admin", "/admin/dashboard", "/admin/usuarios", "/admin/productos", "/admin/videojuegos", "/admin/logros", "/admin/canjes", "/admin/plataformas", "/admin/configuracion"})
    public String adminPages(HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/acceso";
        }

        Object rol = session.getAttribute("usuarioRol");
        if (rol == null || !"ADMIN".equalsIgnoreCase(String.valueOf(rol))) {
            return "redirect:/usuario/dashboard";
        }

        return "forward:/ui/admin.html";
    }
}
