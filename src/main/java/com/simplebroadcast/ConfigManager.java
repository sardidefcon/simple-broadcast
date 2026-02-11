package com.simplebroadcast;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private static final String PLUGIN_MESSAGES_PATH = "plugin-messages";

    private final JavaPlugin plugin;

    private String prefix;
    private long intervalSeconds;
    private boolean randomSend;
    private List<String> messages;

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

        String rawPrefix = config.getString("prefix", "[!]");
        this.prefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);

        this.intervalSeconds = config.getLong("interval", 60L);
        if (this.intervalSeconds < 1L) {
            this.intervalSeconds = 60L;
        }

        this.randomSend = config.getBoolean("random-send", false);

        List<String> list = config.getStringList("messages");
        if (list == null || list.isEmpty()) {
            this.messages = Collections.emptyList();
        } else {
            this.messages = Collections.unmodifiableList(list);
        }

        loadPluginMessages(config);
    }

    private void loadPluginMessages(FileConfiguration config) {
        String base = PLUGIN_MESSAGES_PATH + ".";
        this.pluginPrefix = translate(config.getString(base + "prefix", "&7[&aSimpleBroadcast&7]"));
        this.reloadSuccess = translate(config.getString(base + "reload-success", "&aConfiguration reloaded successfully."));
        this.reloadNoPermission = translate(config.getString(base + "reload-no-permission", "&cYou do not have permission to run this command. (sp.reload)"));
        this.toggleResumed = translate(config.getString(base + "toggle-resumed", "&aMessage broadcasting resumed."));
        this.togglePaused = translate(config.getString(base + "toggle-paused", "&eMessage broadcasting paused."));
        this.toggleNoPermission = translate(config.getString(base + "toggle-no-permission", "&cYou do not have permission to run this command. (sp.toggle)"));
        this.usage = translate(config.getString(base + "usage", "&7Usage: &f/<command> <reload|toggle>"));
    }

    private static String translate(String raw) {
        return raw == null ? "" : ChatColor.translateAlternateColorCodes('&', raw);
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

    public List<String> getMessages() {
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

    /** Returns the usage message; replace &lt;command&gt; with the actual command label. */
    public String getUsage(String commandLabel) {
        return usage.replace("<command>", commandLabel);
    }
}
