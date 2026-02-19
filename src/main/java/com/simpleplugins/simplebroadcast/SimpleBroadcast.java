package com.simpleplugins.simplebroadcast;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SimpleBroadcast extends JavaPlugin {

    private ConfigManager configManager;
    private BukkitTask broadcastTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int pluginId = 29532;
        Metrics metrics = new Metrics(this, pluginId);

        this.configManager = new ConfigManager(this);
        startBroadcastTask();

        SimpleBroadcastCommand cmd = new SimpleBroadcastCommand(this);
        getCommand("simplebroadcast").setExecutor(cmd);
        getCommand("simplebroadcast").setTabCompleter(cmd);

        UpdateChecker.check(this, getDescription().getVersion(), configManager.isCheckUpdates());

        getLogger().info("SimpleBroadcast has been enabled.");
    }

    @Override
    public void onDisable() {
        stopBroadcastTask();
        getLogger().info("SimpleBroadcast has been disabled.");
    }

    private void startBroadcastTask() {
        stopBroadcastTask();

        long defaultInterval = configManager.getIntervalSeconds();
        if (defaultInterval <= 0L) {
            getLogger().warning("Broadcast interval is <= 0. Task will not be started.");
            return;
        }

        final long checkPeriodTicks = 20L;
        BroadcastTask task = new BroadcastTask(configManager);
        this.broadcastTask = task.runTaskTimer(this, checkPeriodTicks, checkPeriodTicks);
    }

    private void stopBroadcastTask() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    public boolean isBroadcastRunning() {
        return broadcastTask != null;
    }

    public boolean toggleBroadcasts() {
        if (isBroadcastRunning()) {
            stopBroadcastTask();
            return false;
        } else {
            startBroadcastTask();
            return true;
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        configManager.reload();
        startBroadcastTask();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
