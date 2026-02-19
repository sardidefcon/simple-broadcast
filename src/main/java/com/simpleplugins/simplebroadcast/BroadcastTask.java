package com.simpleplugins.simplebroadcast;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class BroadcastTask extends BukkitRunnable {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final ConfigManager configManager;
    private final Random random = new Random();

    private int currentIndex = 0;
    private String lastMessageRaw = null;
    private long lastBroadcastTimeMillis = 0L;

    public BroadcastTask(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void run() {
        int onlineCount = Bukkit.getOnlinePlayers().size();

        if (onlineCount == 0 && configManager.shouldSkipWhenEmpty(0)) {
            return;
        }

        long effectiveIntervalSeconds = configManager.getEffectiveIntervalSeconds(onlineCount);
        long effectiveIntervalMillis = effectiveIntervalSeconds * 1000L;
        long now = System.currentTimeMillis();
        if (lastBroadcastTimeMillis > 0 && (now - lastBroadcastTimeMillis) < effectiveIntervalMillis) {
            return;
        }
        lastBroadcastTimeMillis = now;

        List<BroadcastMessage> messages = configManager.getMessages();

        if (messages == null || messages.isEmpty()) {
            return;
        }

        BroadcastMessage message;

        if (configManager.isRandomSend()) {
            if (messages.size() == 1) {
                message = messages.get(0);
            } else {
                int attempts = 0;
                BroadcastMessage candidate;
                do {
                    int index = random.nextInt(messages.size());
                    candidate = messages.get(index);
                    attempts++;
                } while (candidate.getText().equals(lastMessageRaw) && attempts < 10);
                message = candidate;
            }
        } else {
            if (currentIndex >= messages.size()) {
                currentIndex = 0;
            }
            message = messages.get(currentIndex);
            currentIndex++;
        }

        lastMessageRaw = message.getText();

        if (message.isActionBar()) {
            String colored = ChatColor.translateAlternateColorCodes('&', message.getText());
            Component component = LEGACY_SERIALIZER.deserialize(colored);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(component);
            }
        } else {
            String prefix = configManager.getPrefix();
            String full = (prefix == null || prefix.isEmpty())
                    ? message.getText()
                    : prefix + " " + message.getText();
            String colored = ChatColor.translateAlternateColorCodes('&', full);
            Bukkit.getServer().broadcastMessage(colored);
        }

        if (message.getSound() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), message.getSound(), 1.0f, 1.0f);
            }
        }
    }
}
