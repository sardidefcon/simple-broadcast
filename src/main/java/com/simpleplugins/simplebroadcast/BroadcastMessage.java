package com.simpleplugins.simplebroadcast;

/**
 * Represents a single broadcast message with its display mode (chat or action bar) and optional sound.
 */
public class BroadcastMessage {

    public enum Display {
        CHAT,
        ACTION_BAR
    }

    private final String text;
    private final Display display;
    /** Minecraft sound key (e.g. "entity.experience_orb.pickup"), or null if no sound. */
    private final String sound;

    public BroadcastMessage(String text, Display display) {
        this(text, display, null);
    }

    public BroadcastMessage(String text, Display display, String sound) {
        this.text = text != null ? text : "";
        this.display = display != null ? display : Display.CHAT;
        String s = (sound != null && !sound.isBlank()) && !"none".equalsIgnoreCase(sound.trim())
                ? sound.trim()
                : null;
        this.sound = s;
    }

    public String getText() {
        return text;
    }

    public Display getDisplay() {
        return display;
    }

    /** Returns the Minecraft sound key to play, or null if no sound. */
    public String getSound() {
        return sound;
    }

    public boolean isActionBar() {
        return display == Display.ACTION_BAR;
    }
}
