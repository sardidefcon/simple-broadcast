package com.simpleplugins.simplebroadcast;

public final class CustomIntervalRule {

    private final int minPlayers;
    private final long intervalSeconds;

    public CustomIntervalRule(int minPlayers, long intervalSeconds) {
        this.minPlayers = minPlayers;
        this.intervalSeconds = intervalSeconds;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }
}
