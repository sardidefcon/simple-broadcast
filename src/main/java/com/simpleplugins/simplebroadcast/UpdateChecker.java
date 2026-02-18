package com.simpleplugins.simplebroadcast;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Checks Modrinth for new plugin versions and notifies console and operators.
 */
public class UpdateChecker {

    private static final String MODRINTH_PROJECT = "simple-broadcast";
    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT + "/version";
    private static final String MODRINTH_PAGE_URL = "https://modrinth.com/plugin/" + MODRINTH_PROJECT;

    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

    /**
     * Schedules an async check for updates. If a newer version is found, a yellow message
     * is sent to the console and to all online operators (with clickable link).
     *
     * @param plugin        the plugin instance
     * @param currentVersion current version string (e.g. from plugin.yml)
     * @param enabled       whether update checking is enabled in config
     */
    public static void check(JavaPlugin plugin, String currentVersion, boolean enabled) {
        if (!enabled || currentVersion == null || currentVersion.isEmpty()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String latestVersion = fetchLatestVersion();
            if (latestVersion == null) {
                return;
            }
            if (!isNewerVersion(latestVersion, currentVersion)) {
                return;
            }

            final String latest = latestVersion;
            Bukkit.getScheduler().runTask(plugin, () -> notifyUpdateAvailable(plugin, latest));
        });
    }

    private static String fetchLatestVersion() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MODRINTH_API_URL))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }

            String body = response.body();
            java.util.regex.Matcher matcher = VERSION_NUMBER_PATTERN.matcher(body);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Compares two version strings (e.g. "1.0.0" vs "1.1.0"). Returns true if latest is newer than current.
     */
    private static boolean isNewerVersion(String latest, String current) {
        int[] latestParts = parseVersion(latest);
        int[] currentParts = parseVersion(current);
        if (latestParts == null || currentParts == null) {
            return false;
        }
        int maxLen = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < maxLen; i++) {
            int l = i < latestParts.length ? latestParts[i] : 0;
            int c = i < currentParts.length ? currentParts[i] : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private static int[] parseVersion(String version) {
        if (version == null) return null;
        String[] parts = version.split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Integer.parseInt(parts[i].replaceAll("[^0-9].*", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return out;
    }

    private static void notifyUpdateAvailable(JavaPlugin plugin, String latestVersion) {
        Component line1 = Component.text("> A new version of Simple Broadcast is available")
                .color(NamedTextColor.YELLOW);
        Component line2 = Component.text("> ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text("Click here to download it")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(MODRINTH_PAGE_URL)));

        // Console
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(line1);
        console.sendMessage(line2);
        plugin.getLogger().info("A new version of Simple Broadcast is available: " + latestVersion + " - " + MODRINTH_PAGE_URL);

        // Operators online
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(line1);
                player.sendMessage(line2);
            }
        }
    }
}
