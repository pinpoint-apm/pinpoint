package com.nhn.pinpoint.web.view;

/**
 * @author emeroad
 */
public class ServerTime {

    private final long currentServerTime;

    public ServerTime() {
        this.currentServerTime = System.currentTimeMillis();
    }

    public long getCurrentServerTime() {
        return currentServerTime;
    }
}
