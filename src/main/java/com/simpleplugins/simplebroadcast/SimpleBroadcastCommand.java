package com.simpleplugins.simplebroadcast;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleBroadcastCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_RELOAD = "sp.reload";
    private static final String PERMISSION_TOGGLE = "sp.toggle";

    private final SimpleBroadcast plugin;

    public SimpleBroadcastCommand(SimpleBroadcast plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].equalsIgnoreCase("reload") ? "reload" : args[0].equalsIgnoreCase("toggle") ? "toggle" : null;

        if (sub == null) {
            sendUsage(sender, label);
            return true;
        }

        if ("reload".equals(sub)) {
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                sender.sendMessage(withPrefix(plugin.getConfigManager().getReloadNoPermission()));
                return true;
            }
            plugin.reloadPluginConfig();
            sender.sendMessage(withPrefix(plugin.getConfigManager().getReloadSuccess()));
            plugin.getLogger().info("Configuration reloaded by " + sender.getName() + ".");
            return true;
        }

        if ("toggle".equals(sub)) {
            if (!sender.hasPermission(PERMISSION_TOGGLE)) {
                sender.sendMessage(withPrefix(plugin.getConfigManager().getToggleNoPermission()));
                return true;
            }
            boolean nowRunning = plugin.toggleBroadcasts();
            sender.sendMessage(withPrefix(nowRunning
                    ? plugin.getConfigManager().getToggleResumed()
                    : plugin.getConfigManager().getTogglePaused()));
            return true;
        }

        sendUsage(sender, label);
        return true;
    }

    private String withPrefix(String message) {
        String prefix = plugin.getConfigManager().getPluginPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + " " + message;
        }
        return message;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(withPrefix(plugin.getConfigManager().getUsage(label)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        String input = args[0].toLowerCase();
        List<String> suggestions = new ArrayList<>();
        if (sender.hasPermission(PERMISSION_RELOAD) && "reload".startsWith(input)) {
            suggestions.add("reload");
        }
        if (sender.hasPermission(PERMISSION_TOGGLE) && "toggle".startsWith(input)) {
            suggestions.add("toggle");
        }
        return suggestions;
    }
}
