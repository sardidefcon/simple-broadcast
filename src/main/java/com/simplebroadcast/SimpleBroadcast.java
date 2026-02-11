package com.simplebroadcast;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SimpleBroadcast extends JavaPlugin {

    private ConfigManager configManager;
    private BukkitTask broadcastTask;

    @Override
    public void onEnable() {
        // Ensure config.yml exists in the plugin's data folder.
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        startBroadcastTask();

        SimpleBroadcastCommand cmd = new SimpleBroadcastCommand(this);
        getCommand("simplebroadcast").setExecutor(cmd);
        getCommand("simplebroadcast").setTabCompleter(cmd);

        getLogger().info("SimpleBroadcast has been enabled.");
    }

    @Override
    public void onDisable() {
        stopBroadcastTask();
        getLogger().info("SimpleBroadcast has been disabled.");
    }

    /**
     * Starts (or restarts) the broadcast task based on the current configuration.
     */
    private void startBroadcastTask() {
        stopBroadcastTask();

        long intervalSeconds = configManager.getIntervalSeconds();
        if (intervalSeconds <= 0L) {
            getLogger().warning("Broadcast interval is <= 0. Task will not be started.");
            return;
        }

        long periodTicks = intervalSeconds * 20L;

        BroadcastTask task = new BroadcastTask(configManager);
        this.broadcastTask = task.runTaskTimer(this, periodTicks, periodTicks);
    }

    /**
     * Stops the broadcast task if it is currently running.
     */
    private void stopBroadcastTask() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    /**
     * Returns whether the broadcast task is currently running.
     */
    public boolean isBroadcastRunning() {
        return broadcastTask != null;
    }

    /**
     * Toggles broadcast sending: stops if running, starts if stopped.
     * @return true if broadcasts are now running, false if now paused
     */
    public boolean toggleBroadcasts() {
        if (isBroadcastRunning()) {
            stopBroadcastTask();
            return false;
        } else {
            startBroadcastTask();
            return true;
        }
    }

    /**
     * Utility method in case the plugin is reloaded programmatically.
     * Not wired to any command, but safe to call from external code or plugins.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        configManager.reload();
        startBroadcastTask();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

