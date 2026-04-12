package com.trophieshop.trophieshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trophieshop.trophieshop.entity.SteamLogroOtorgado;
import com.trophieshop.trophieshop.entity.Usuario;
import com.trophieshop.trophieshop.repository.SteamLogroOtorgadoRepository;
import com.trophieshop.trophieshop.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SteamService {

    private static final String OPENID_ENDPOINT = "https://steamcommunity.com/openid/login";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final UsuarioRepository usuarioRepository;
    private final SteamLogroOtorgadoRepository steamLogroOtorgadoRepository;

    @Value("${steam.api.key:}")
    private String steamApiKey;

    @Value("${steam.rare-threshold-percent:10}")
    private double rareThresholdPercent;

    @Value("${steam.points.common:200}")
    private int commonAchievementPoints;

    @Value("${steam.points.rare:500}")
    private int rareAchievementPoints;

    public SteamService(UsuarioRepository usuarioRepository,
                        SteamLogroOtorgadoRepository steamLogroOtorgadoRepository) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.usuarioRepository = usuarioRepository;
        this.steamLogroOtorgadoRepository = steamLogroOtorgadoRepository;
    }

    public URI buildLoginUri(String returnTo, String realm) {
        String query = "openid.ns=" + encode("http://specs.openid.net/auth/2.0")
                + "&openid.mode=checkid_setup"
                + "&openid.return_to=" + encode(returnTo)
                + "&openid.realm=" + encode(realm)
                + "&openid.identity=" + encode("http://specs.openid.net/auth/2.0/identifier_select")
                + "&openid.claimed_id=" + encode("http://specs.openid.net/auth/2.0/identifier_select");
        return URI.create(OPENID_ENDPOINT + "?" + query);
    }

    public String verifyAndExtractSteamId(Map<String, String> queryParams) {
        String claimedId = queryParams.get("openid.claimed_id");
        if (claimedId == null || !claimedId.startsWith("https://steamcommunity.com/openid/id/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Steam callback invalido");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith("openid.")) {
                form.add(entry.getKey(), entry.getValue());
            }
        }
        form.set("openid.mode", "check_authentication");

        String verification = restClient.post()
                .uri(OPENID_ENDPOINT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(form)
                .retrieve()
                .body(String.class);

        if (verification == null || !verification.contains("is_valid:true")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo validar la identidad de Steam");
        }

        return claimedId.substring(claimedId.lastIndexOf('/') + 1);
    }

    @Transactional
    public Usuario findOrCreateLocalUserBySteam(String steamId) {
        Optional<Usuario> existing = usuarioRepository.findBySteamId(steamId);
        if (existing.isPresent()) {
            Usuario user = existing.get();
            updatePersonaName(user);
            return usuarioRepository.save(user);
        }

        String personaName = fetchPersonaName(steamId);

        Usuario user = new Usuario();
        user.setNombre(personaName != null ? personaName : "Steam User " + steamId);
        user.setEmail("steam_" + steamId + "@steam.local");
        user.setPassword("steam-auth-" + UUID.randomUUID());
        user.setRol("USER");
        user.setMonedasAcumuladas(0);
        user.setSteamId(steamId);
        user.setSteamPersonaName(personaName);
        return usuarioRepository.save(user);
    }

    public Map<String, Object> getSteamProfile(String steamId) {
        ensureApiKey();
        JsonNode root = fetchJson("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key={key}&steamids={steamId}", steamApiKey, steamId);

        JsonNode player = root != null
                ? root.path("response").path("players").path(0)
                : null;

        if (player == null || player.isMissingNode()) {
            return Map.of("steamId", steamId, "personaName", "Unknown");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("steamId", steamId);
        profile.put("personaName", player.path("personaname").asText("Unknown"));
        profile.put("avatar", player.path("avatarfull").asText(""));
        profile.put("profileUrl", player.path("profileurl").asText(""));
        return profile;
    }

    public List<Map<String, Object>> getOwnedGames(String steamId) {
        ensureApiKey();
        JsonNode primary = null;
        try {
            primary = fetchJson("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key={key}&steamid={steamId}&include_appinfo=1&include_played_free_games=1", steamApiKey, steamId);
        } catch (Exception ignored) {
            // Try legacy endpoint below.
        }

        List<Map<String, Object>> games = extractOwnedGames(primary);
        if (!games.isEmpty()) {
            return games;
        }

        try {
            JsonNode fallback = fetchJson("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={key}&steamid={steamId}&include_appinfo=true&include_played_free_games=true&format=json", steamApiKey, steamId);
            return extractOwnedGames(fallback);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    public Map<String, Object> diagnoseOwnedGames(String steamId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("steamId", steamId);

        if (steamApiKey == null || steamApiKey.isBlank()) {
            result.put("error", "Falta configurar steam.api.key");
            result.put("v1Count", 0);
            result.put("v0001Count", 0);
            return result;
        }

        try {
            Map<String, Object> profile = getSteamProfile(steamId);
            result.put("profile", profile);
        } catch (Exception e) {
            result.put("profileError", safeErrorMessage(e));
        }

        try {
            JsonNode primary = fetchJson("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key={key}&steamid={steamId}&include_appinfo=1&include_played_free_games=1", steamApiKey, steamId);
            List<Map<String, Object>> primaryGames = extractOwnedGames(primary);
            result.put("v1Count", primaryGames.size());
            result.put("v1Sample", primaryGames.stream().limit(5).toList());
        } catch (Exception e) {
            result.put("v1Error", safeErrorMessage(e));
        }

        try {
            JsonNode fallback = fetchJson("https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key={key}&steamid={steamId}&include_appinfo=true&include_played_free_games=true&format=json", steamApiKey, steamId);
            List<Map<String, Object>> fallbackGames = extractOwnedGames(fallback);
            result.put("v0001Count", fallbackGames.size());
            result.put("v0001Sample", fallbackGames.stream().limit(5).toList());
        } catch (Exception e) {
            result.put("v0001Error", safeErrorMessage(e));
        }

        return result;
    }

    public Map<String, Object> getPlayerAchievementsSummary(String steamId, long appId) {
        ensureApiKey();
        JsonNode root = fetchJson("https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v1/?key={key}&steamid={steamId}&appid={appId}", steamApiKey, steamId, appId);

        JsonNode achievements = root != null
                ? root.path("playerstats").path("achievements")
                : null;

        int total = 0;
        int completed = 0;
        if (achievements != null && achievements.isArray()) {
            total = achievements.size();
            for (JsonNode a : achievements) {
                if (a.path("achieved").asInt(0) == 1) {
                    completed++;
                }
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("appId", appId);
        summary.put("total", total);
        summary.put("completed", completed);
        summary.put("completionRate", total == 0 ? 0 : Math.round((completed * 10000.0) / total) / 100.0);
        return summary;
    }

    public List<Map<String, Object>> getOwnedGamesWithUnlockedAchievements(String steamId) {
        List<Map<String, Object>> ownedGames = getOwnedGames(steamId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> game : ownedGames) {
            long appId = ((Number) game.get("appId")).longValue();
            List<Map<String, Object>> unlocked = getUnlockedAchievementsForGame(steamId, appId);

            int totalPoints = unlocked.stream()
                    .mapToInt(a -> ((Number) a.get("points")).intValue())
                    .sum();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("appId", appId);
            row.put("name", game.get("name"));
            row.put("playtimeMinutes", game.get("playtimeMinutes"));
            row.put("unlockedCount", unlocked.size());
            row.put("totalPoints", totalPoints);
            row.put("achievements", unlocked);
            result.add(row);
        }

        return result;
    }

    @Transactional
    public Map<String, Object> syncUserAchievements(Long usuarioId, String steamId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<Map<String, Object>> gamesWithAchievements = getOwnedGamesWithUnlockedAchievements(steamId);

        int grantedPoints = 0;
        int newAchievements = 0;
        int commonCount = 0;
        int rareCount = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        for (Map<String, Object> game : gamesWithAchievements) {
            Long appId = ((Number) game.get("appId")).longValue();
            String gameName = String.valueOf(game.get("name"));

            Object achievementsObj = game.get("achievements");
            if (!(achievementsObj instanceof List<?> achievements)) {
                continue;
            }

            int pointsForGame = 0;
            int unlockedForGame = 0;

            for (Object item : achievements) {
                if (!(item instanceof Map<?, ?> achievementRaw)) {
                    continue;
                }

                String apiName = String.valueOf(achievementRaw.get("apiName"));
                String displayName = String.valueOf(achievementRaw.get("displayName"));
                double percent = ((Number) achievementRaw.get("rarityPercent")).doubleValue();
                String rarityType = String.valueOf(achievementRaw.get("rarityType"));
                int points = ((Number) achievementRaw.get("points")).intValue();

                if (steamLogroOtorgadoRepository.existsByUsuarioIdAndAppIdAndAchievementApiName(usuario.getId(), appId, apiName)) {
                    continue;
                }

                SteamLogroOtorgado nuevo = new SteamLogroOtorgado();
                nuevo.setUsuario(usuario);
                nuevo.setAppId(appId);
                nuevo.setGameName(gameName);
                nuevo.setAchievementApiName(apiName);
                nuevo.setAchievementDisplayName(displayName);
                nuevo.setRarityPercent(percent);
                nuevo.setRarityType(rarityType);
                nuevo.setPuntosOtorgados(points);
                steamLogroOtorgadoRepository.save(nuevo);

                grantedPoints += points;
                pointsForGame += points;
                unlockedForGame++;
                newAchievements++;
                if ("RARE".equalsIgnoreCase(rarityType)) {
                    rareCount++;
                } else {
                    commonCount++;
                }
            }

            if (unlockedForGame > 0) {
                Map<String, Object> gameSummary = new LinkedHashMap<>();
                gameSummary.put("appId", appId);
                gameSummary.put("name", gameName);
                gameSummary.put("newAchievements", unlockedForGame);
                gameSummary.put("pointsGranted", pointsForGame);
                details.add(gameSummary);
            }
        }

        if (grantedPoints > 0) {
            usuario.setMonedasAcumuladas(usuario.getMonedasAcumuladas() + grantedPoints);
            usuarioRepository.save(usuario);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("grantedPoints", grantedPoints);
        result.put("newAchievements", newAchievements);
        result.put("commonAchievements", commonCount);
        result.put("rareAchievements", rareCount);
        result.put("details", details);
        return result;
    }

    private List<Map<String, Object>> getUnlockedAchievementsForGame(String steamId, long appId) {
        try {
            ensureApiKey();

                JsonNode root = fetchJson("https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v1/?key={key}&steamid={steamId}&appid={appId}", steamApiKey, steamId, appId);

            JsonNode achievements = root != null ? root.path("playerstats").path("achievements") : null;
            if (achievements == null || !achievements.isArray()) {
                return List.of();
            }

            Map<String, Double> globalPercentages = getGlobalAchievementPercentages(appId);
            List<Map<String, Object>> unlocked = new ArrayList<>();

            for (JsonNode achievement : achievements) {
                if (achievement.path("achieved").asInt(0) != 1) {
                    continue;
                }

                String apiName = achievement.path("apiname").asText("");
                if (apiName.isBlank()) {
                    continue;
                }

                double percent = globalPercentages.getOrDefault(apiName, 100.0);
                boolean isRare = percent <= rareThresholdPercent;
                int points = isRare ? rareAchievementPoints : commonAchievementPoints;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("apiName", apiName);
                item.put("displayName", achievement.path("name").asText(apiName));
                item.put("description", achievement.path("description").asText(""));
                item.put("unlockTime", achievement.path("unlocktime").asLong(0));
                item.put("rarityPercent", Math.round(percent * 100.0) / 100.0);
                item.put("rarityType", isRare ? "RARE" : "COMMON");
                item.put("points", points);
                unlocked.add(item);
            }

            return unlocked;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private Map<String, Double> getGlobalAchievementPercentages(long appId) {
        try {
            JsonNode root = fetchJson("https://api.steampowered.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v2/?gameid={appId}", appId);

            JsonNode achievements = root != null
                    ? root.path("achievementpercentages").path("achievements")
                    : null;

            if (achievements == null || !achievements.isArray()) {
                return Map.of();
            }

            Map<String, Double> percentages = new HashMap<>();
            for (JsonNode achievement : achievements) {
                String name = achievement.path("name").asText("");
                if (name.isBlank()) {
                    continue;
                }
                percentages.put(name, achievement.path("percent").asDouble(100.0));
            }

            return percentages;
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> extractOwnedGames(JsonNode root) {
        JsonNode games = root != null ? root.path("response").path("games") : null;

        if (games == null || !games.isArray()) {
            return List.of();
        }

        List<Map<String, Object>> payload = new ArrayList<>();
        for (JsonNode game : games) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("appId", game.path("appid").asLong());
            row.put("name", game.path("name").asText(""));
            row.put("playtimeMinutes", game.path("playtime_forever").asInt(0));
            payload.add(row);
        }
        return payload;
    }

    private String fetchPersonaName(String steamId) {
        try {
            Map<String, Object> profile = getSteamProfile(steamId);
            Object persona = profile.get("personaName");
            return persona != null ? String.valueOf(persona) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void updatePersonaName(Usuario user) {
        String personaName = fetchPersonaName(user.getSteamId());
        if (personaName != null && !personaName.isBlank()) {
            user.setSteamPersonaName(personaName);
            if (user.getNombre() == null || user.getNombre().isBlank() || user.getNombre().startsWith("Steam User ")) {
                user.setNombre(personaName);
            }
        }
    }

    private void ensureApiKey() {
        if (steamApiKey == null || steamApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falta configurar steam.api.key");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String safeErrorMessage(Exception e) {
        String message = e.getMessage();
        return (message == null || message.isBlank()) ? e.getClass().getSimpleName() : message;
    }

    private JsonNode fetchJson(String uriTemplate, Object... uriVariables) {
        String payload = restClient.get()
                .uri(uriTemplate, uriVariables)
                .retrieve()
                .body(String.class);
        try {
            return payload == null || payload.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta JSON invalida de Steam");
        }
    }
}
