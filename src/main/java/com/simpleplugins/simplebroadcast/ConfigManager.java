package com.simpleplugins.simplebroadcast;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private static final String PLUGIN_MESSAGES_PATH = "plugin-messages";

    private final JavaPlugin plugin;

    private boolean checkUpdates;
    private String prefix;
    private long intervalSeconds;
    private boolean skipIfEmpty;
    private boolean customIntervalEnabled;
    private List<CustomIntervalRule> customIntervalRules;
    private boolean randomSend;
    private List<BroadcastMessage> messages;

    private String pluginPrefix;
    private String reloadSuccess;
    private String reloadNoPermission;
    private String toggleResumed;
    private String togglePaused;
    private String toggleNoPermission;
    private String usage;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        this.checkUpdates = config.getBoolean("check-updates", true);

        String rawPrefix = config.getString("prefix", "[!]");
        this.prefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);

        this.intervalSeconds = config.getLong("interval", 60L);
        if (this.intervalSeconds < 1L) {
            this.intervalSeconds = 60L;
        }

        this.skipIfEmpty = config.getBoolean("skip-if-empty", true);

        this.customIntervalEnabled = config.getBoolean("custom-interval.enabled", false);
        List<?> rawRules = config.getList("custom-interval.rules");
        this.customIntervalRules = loadAndSortCustomIntervalRules(rawRules);

        this.randomSend = config.getBoolean("random-send", false);

        this.messages = loadMessages(config.getList("messages"));

        loadPluginMessages(config);
    }

    private void loadPluginMessages(FileConfiguration config) {
        String base = PLUGIN_MESSAGES_PATH + ".";
        this.pluginPrefix = translate(config.getString(base + "prefix", "&7[&aSimpleBroadcast&7]"));
        this.reloadSuccess = translate(config.getString(base + "reload-success", "&aConfiguration reloaded successfully."));
        this.reloadNoPermission = translate(config.getString(base + "reload-no-permission", "&cYou do not have permission to run this command. (sb.reload)"));
        this.toggleResumed = translate(config.getString(base + "toggle-resumed", "&aMessage broadcasting resumed."));
        this.togglePaused = translate(config.getString(base + "toggle-paused", "&eMessage broadcasting paused."));
        this.toggleNoPermission = translate(config.getString(base + "toggle-no-permission", "&cYou do not have permission to run this command. (sb.toggle)"));
        this.usage = translate(config.getString(base + "usage", "&7Usage: &f/<command> <reload|toggle>"));
    }

    private static String translate(String raw) {
        return raw == null ? "" : ChatColor.translateAlternateColorCodes('&', raw);
    }

    @SuppressWarnings("unchecked")
    private static List<BroadcastMessage> loadMessages(List<?> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<BroadcastMessage> out = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof String) {
                out.add(new BroadcastMessage((String) item, BroadcastMessage.Display.CHAT, null));
            } else if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                String text = map.get("text") != null ? map.get("text").toString() : "";
                Object displayObj = map.get("display");
                BroadcastMessage.Display display = BroadcastMessage.Display.CHAT;
                if (displayObj != null && "action-bar".equalsIgnoreCase(displayObj.toString())) {
                    display = BroadcastMessage.Display.ACTION_BAR;
                }
                Object soundObj = map.get("sound");
                String sound = (soundObj == null || soundObj.toString().isBlank())
                        ? null
                        : soundObj.toString();
                out.add(new BroadcastMessage(text, display, sound));
            }
        }
        return Collections.unmodifiableList(out);
    }

    @SuppressWarnings("unchecked")
    private static List<CustomIntervalRule> loadAndSortCustomIntervalRules(List<?> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<CustomIntervalRule> out = new ArrayList<>(list.size());
        for (Object item : list) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) item;
            Object minObj = map.get("min-players");
            Object intervalObj = map.get("interval");
            if (minObj == null || intervalObj == null) {
                continue;
            }
            int minPlayers = toInt(minObj, -1);
            long interval = toLong(intervalObj, 0L);
            if (minPlayers < 0 || interval < 1L) {
                continue;
            }
            out.add(new CustomIntervalRule(minPlayers, interval));
        }
        out.sort(Comparator.comparingInt(CustomIntervalRule::getMinPlayers));
        return Collections.unmodifiableList(out);
    }

    private static int toInt(Object o, int def) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if (o != null) {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static long toLong(Object o, long def) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        if (o != null) {
            try {
                return Long.parseLong(o.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    public long getEffectiveIntervalSeconds(int onlinePlayerCount) {
        if (!customIntervalEnabled || customIntervalRules.isEmpty()) {
            return intervalSeconds;
        }
        CustomIntervalRule best = null;
        for (CustomIntervalRule rule : customIntervalRules) {
            if (rule.getMinPlayers() <= onlinePlayerCount) {
                best = rule;
            }
        }
        return best != null ? best.getIntervalSeconds() : intervalSeconds;
    }

    public boolean shouldSkipWhenEmpty(int onlinePlayerCount) {
        if (onlinePlayerCount > 0) {
            return false;
        }
        if (customIntervalEnabled) {
            boolean hasZeroRule = customIntervalRules.stream()
                    .anyMatch(r -> r.getMinPlayers() == 0);
            if (hasZeroRule) {
                return false;
            }
            return true;
        }
        return skipIfEmpty;
    }

    public boolean isCheckUpdates() {
        return checkUpdates;
    }

    public String getPrefix() {
        return prefix;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isRandomSend() {
        return randomSend;
    }

    public List<BroadcastMessage> getMessages() {
        return messages;
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public String getReloadSuccess() {
        return reloadSuccess;
    }

    public String getReloadNoPermission() {
        return reloadNoPermission;
    }

    public String getToggleResumed() {
        return toggleResumed;
    }

    public String getTogglePaused() {
        return togglePaused;
    }

    public String getToggleNoPermission() {
        return toggleNoPermission;
    }

    public String getUsage(String commandLabel) {
        return usage.replace("<command>", commandLabel);
    }
}
