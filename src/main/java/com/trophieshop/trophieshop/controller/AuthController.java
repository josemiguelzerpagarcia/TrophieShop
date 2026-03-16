package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/registro")
    @ResponseBody
    public String showRegistro() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Registro - TrophieShop</title>
                    <style>
                        body { font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; background: #f6f8fb; margin: 0; color: #1f2937; }
                        .container { max-width: 400px; margin: 60px auto; background: #fff; border-radius: 10px; box-shadow: 0 8px 24px rgba(0,0,0,.08); padding: 30px; }
                        h1 { margin-top: 0; font-size: 1.5rem; text-align: center; }
                        .form-group { margin-bottom: 16px; }
                        label { display: block; margin-bottom: 6px; font-weight: 500; }
                        input { width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
                        input:focus { outline: none; border-color: #3b82f6; }
                        button { width: 100%; padding: 10px; background: #3b82f6; color: #fff; border: none; border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 20px; }
                        button:hover { background: #2563eb; }
                        .link { text-align: center; margin-top: 16px; }
                        .link a { color: #3b82f6; text-decoration: none; }
                        .link a:hover { text-decoration: underline; }
                        .error { color: #dc2626; margin-top: 16px; padding: 10px; background: #fee2e2; border-radius: 6px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Registro</h1>
                        <form method="POST" action="/registro">
                            <div class="form-group">
                                <label for="nombre">Nombre</label>
                                <input type="text" id="nombre" name="nombre" required>
                            </div>
                            <div class="form-group">
                                <label for="email">Email</label>
                                <input type="email" id="email" name="email" required>
                            </div>
                            <div class="form-group">
                                <label for="password">Contraseña</label>
                                <input type="password" id="password" name="password" required>
                            </div>
                            <button type="submit">Registrarse</button>
                        </form>
                        <div class="link">
                            ¿Ya tienes cuenta? <a href="/login">Inicia sesión aquí</a>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    @PostMapping("/registro")
    @ResponseBody
    public String handleRegistro(@RequestParam String nombre, @RequestParam String email, @RequestParam String password) {
        try {
            authService.register(nombre, email, password);
            return """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"><title>Éxito</title></head>
                    <body>
                        <p>¡Registro exitoso! Redirigiendo...</p>
                        <script>setTimeout(() => window.location.href = '/login', 2000);</script>
                    </body>
                    </html>
                    """;
        } catch (ResponseStatusException e) {
            return """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"><title>Error</title></head>
                    <body>
                        <p>Error: """ + e.getReason() + """
</p>
                        <a href="/registro">Volver</a>
                    </body>
                    </html>
                    """;
        }
    }

    @GetMapping("/login")
    @ResponseBody
    public String showLogin() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Login - TrophieShop</title>
                    <style>
                        body { font-family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif; background: #f6f8fb; margin: 0; color: #1f2937; }
                        .container { max-width: 400px; margin: 60px auto; background: #fff; border-radius: 10px; box-shadow: 0 8px 24px rgba(0,0,0,.08); padding: 30px; }
                        h1 { margin-top: 0; font-size: 1.5rem; text-align: center; }
                        .form-group { margin-bottom: 16px; }
                        label { display: block; margin-bottom: 6px; font-weight: 500; }
                        input { width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
                        input:focus { outline: none; border-color: #3b82f6; }
                        button { width: 100%; padding: 10px; background: #3b82f6; color: #fff; border: none; border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 20px; }
                        button:hover { background: #2563eb; }
                        .link { text-align: center; margin-top: 16px; }
                        .link a { color: #3b82f6; text-decoration: none; }
                        .link a:hover { text-decoration: underline; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Inicia Sesión</h1>
                        <form method="POST" action="/login">
                            <div class="form-group">
                                <label for="email">Email</label>
                                <input type="email" id="email" name="email" required>
                            </div>
                            <div class="form-group">
                                <label for="password">Contraseña</label>
                                <input type="password" id="password" name="password" required>
                            </div>
                            <button type="submit">Iniciar Sesión</button>
                        </form>
                        <div class="link">
                            ¿No tienes cuenta? <a href="/registro">Regístrate aquí</a>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    @PostMapping("/login")
    @ResponseBody
    public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        Optional<Usuario> usuario = authService.login(email, password);

        if (usuario.isPresent()) {
            session.setAttribute("usuarioId", usuario.get().getId());
            session.setAttribute("usuarioNombre", usuario.get().getNombre());
            return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Éxito</title></head><body><p>¡Bienvenido " + usuario.get().getNombre() + "!</p><script>setTimeout(() => window.location.href = '/videojuegos', 2000);</script></body></html>";
        } else {
            return """
                    <!DOCTYPE html>
                    <html>
                    <head><meta charset="UTF-8"><title>Error</title></head>
                    <body>
                        <p>Email o contraseña incorrectos</p>
                        <a href="/login">Volver</a>
                    </body>
                    </html>
                    """;
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
