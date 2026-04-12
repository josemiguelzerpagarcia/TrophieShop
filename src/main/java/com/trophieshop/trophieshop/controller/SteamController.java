package com.trophieshop.trophieshop.controller;

import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.service.SteamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/steam")
public class SteamController {

    private final SteamService steamService;

    public SteamController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());

        URI loginUri = steamService.buildLoginUri(baseUrl + "/api/steam/callback", baseUrl);
        return "redirect:" + loginUri;
    }

    @GetMapping("/callback")
    public String callback(HttpServletRequest request, HttpSession session) {
        Map<String, String[]> rawParams = request.getParameterMap();
        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : rawParams.entrySet()) {
            if (entry.getValue().length > 0) {
                params.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        String steamId = steamService.verifyAndExtractSteamId(params);
        Usuario user = steamService.findOrCreateLocalUserBySteam(steamId);

        session.setAttribute("usuarioId", user.getId());
        session.setAttribute("usuarioNombre", user.getNombre());
        session.setAttribute("usuarioRol", user.getRol());
        session.setAttribute("steamId", steamId);

        return "redirect:/usuario/dashboard";
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> me(HttpSession session) {
        Object steamId = session.getAttribute("steamId");
        if (steamId == null) {
            Object userSteam = session.getAttribute("usuarioId");
            if (userSteam == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
            }
        }

        Object sessionSteam = session.getAttribute("steamId");
        if (sessionSteam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario sin cuenta Steam vinculada en sesion"));
        }

        return ResponseEntity.ok(steamService.getSteamProfile(String.valueOf(sessionSteam)));
    }

    @GetMapping("/owned-games")
    @ResponseBody
    public ResponseEntity<?> ownedGames(HttpSession session) {
        String steamId = requireSteamSession(session);
        return ResponseEntity.ok(steamService.getOwnedGames(steamId));
    }

    @GetMapping("/library")
    @ResponseBody
    public ResponseEntity<?> library(HttpSession session) {
        String steamId = requireSteamSession(session);
        return ResponseEntity.ok(steamService.getOwnedGamesWithUnlockedAchievements(steamId));
    }

    @GetMapping("/achievements/{appId}")
    @ResponseBody
    public ResponseEntity<?> achievements(HttpSession session, @org.springframework.web.bind.annotation.PathVariable long appId) {
        String steamId = requireSteamSession(session);
        return ResponseEntity.ok(steamService.getPlayerAchievementsSummary(steamId, appId));
    }

    @PostMapping("/sync")
    @ResponseBody
    public ResponseEntity<?> sync(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String steamId = requireSteamSession(session);
        Long localUserId = ((Number) usuarioId).longValue();

        return ResponseEntity.ok(steamService.syncUserAchievements(localUserId, steamId));
    }

    @GetMapping("/debug/current")
    @ResponseBody
    public ResponseEntity<?> debugCurrent(HttpSession session) {
        try {
            String steamId = requireSteamSession(session);
            return ResponseEntity.ok(steamService.diagnoseOwnedGames(steamId));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", String.valueOf(e.getReason())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error inesperado al diagnosticar Steam", "detail", String.valueOf(e.getMessage())));
        }
    }

    @GetMapping("/debug/by-id")
    @ResponseBody
    public ResponseEntity<?> debugById(@RequestParam String steamId) {
        try {
            return ResponseEntity.ok(steamService.diagnoseOwnedGames(steamId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error inesperado al diagnosticar Steam", "detail", String.valueOf(e.getMessage())));
        }
    }

    private String requireSteamSession(HttpSession session) {
        Object steamId = session.getAttribute("steamId");
        if (steamId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay SteamID en la sesion. Inicia sesion con Steam.");
        }
        return String.valueOf(steamId);
    }
}
