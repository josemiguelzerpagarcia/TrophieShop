package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/registro")
    public String showRegistro() {
        return "forward:/ui/index.html";
    }

    @PostMapping("/registro")
    @ResponseBody
    public String handleRegistro(@RequestParam String nombre, @RequestParam String email, @RequestParam String password) {
        try {
            authService.register(nombre, email, password);
            return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Exito</title></head><body><p>Registro exitoso.</p><script>setTimeout(() => window.location.href = '/acceso', 1200);</script></body></html>";
        } catch (ResponseStatusException e) {
            return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Error</title></head><body><p>Error: " + e.getReason() + "</p><a href=\"/acceso\">Volver</a></body></html>";
        }
    }

    @GetMapping("/login")
    public String showLogin() {
        return "redirect:/acceso";
    }

    @PostMapping("/login")
    @ResponseBody
    public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        Optional<Usuario> usuario = authService.login(email, password);

        if (usuario.isPresent()) {
            session.setAttribute("usuarioId", usuario.get().getId());
            session.setAttribute("usuarioNombre", usuario.get().getNombre());
            session.setAttribute("usuarioRol", usuario.get().getRol());
            session.setAttribute("steamId", usuario.get().getSteamId());

            String redirectPath = "ADMIN".equalsIgnoreCase(usuario.get().getRol())
                    ? "/admin/dashboard"
                    : "/usuario/dashboard";

            return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Exito</title></head><body><p>Bienvenido "
                    + usuario.get().getNombre()
                    + "!</p><script>setTimeout(() => window.location.href = '"
                    + redirectPath
                    + "', 1000);</script></body></html>";
        } else {
            return """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"><title>Error</title></head>
                    <body>
                        <p>Email o contraseña incorrectos</p>
                        <a href="/acceso">Volver</a>
                    </body>
                    </html>
                    """;
        }
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestBody RegisterRequest request) {
        Usuario created = authService.register(request.nombre(), request.email(), request.password());

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", created.getId());
        payload.put("nombre", created.getNombre());
        payload.put("email", created.getEmail());
        payload.put("rol", created.getRol());
        payload.put("steamId", created.getSteamId());
        payload.put("steamPersonaName", created.getSteamPersonaName());

        return ResponseEntity.status(HttpStatus.CREATED).body(payload);
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuario = authService.login(request.email(), request.password());

        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
        }

        Usuario u = usuario.get();
        session.setAttribute("usuarioId", u.getId());
        session.setAttribute("usuarioNombre", u.getNombre());
        session.setAttribute("usuarioRol", u.getRol());
        session.setAttribute("steamId", u.getSteamId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", u.getId());
        payload.put("nombre", u.getNombre());
        payload.put("email", u.getEmail());
        payload.put("rol", u.getRol());
        payload.put("monedasAcumuladas", u.getMonedasAcumuladas());
        payload.put("steamId", u.getSteamId());
        payload.put("steamPersonaName", u.getSteamPersonaName());

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<?> me(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        Long id = ((Number) usuarioId).longValue();
        Optional<Usuario> usuario = authService.findById(id);
        if (usuario.isEmpty()) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Sesion invalida"));
        }

        Usuario u = usuario.get();
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", u.getId());
        payload.put("nombre", u.getNombre());
        payload.put("email", u.getEmail());
        payload.put("rol", u.getRol());
        payload.put("monedasAcumuladas", u.getMonedasAcumuladas());
        payload.put("steamId", u.getSteamId());
        payload.put("steamPersonaName", u.getSteamPersonaName());

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> apiLogout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/acceso";
    }

    public record LoginRequest(String email, String password) {
    }

    public record RegisterRequest(String nombre, String email, String password) {
    }
}
