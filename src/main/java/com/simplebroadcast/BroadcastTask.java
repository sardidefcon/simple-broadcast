package com.simplebroadcast;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class BroadcastTask extends BukkitRunnable {

    private final ConfigManager configManager;
    private final Random random = new Random();

    private int currentIndex = 0;
    private String lastMessageRaw = null;

    public BroadcastTask(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void run() {
        List<String> messages = configManager.getMessages();

        if (messages == null || messages.isEmpty()) {
            // Nothing to broadcast, just return silently.
            return;
        }

        String rawMessage;

        if (configManager.isRandomSend()) {
            if (messages.size() == 1) {
                rawMessage = messages.get(0);
            } else {
                // Pick a random message, avoiding repeating the last one if possible.
                int attempts = 0;
                String candidate;
                do {
                    int index = random.nextInt(messages.size());
                    candidate = messages.get(index);
                    attempts++;
                } while (candidate.equals(lastMessageRaw) && attempts < 10);

                rawMessage = candidate;
            }
        } else {
            if (currentIndex >= messages.size()) {
                currentIndex = 0;
            }
            rawMessage = messages.get(currentIndex);
            currentIndex++;
        }

        lastMessageRaw = rawMessage;

        String prefix = configManager.getPrefix();
        String full = (prefix == null || prefix.isEmpty())
                ? rawMessage
                : prefix + " " + rawMessage;

        String colored = ChatColor.translateAlternateColorCodes('&', full);
        Bukkit.getServer().broadcastMessage(colored);
    }
}

