package com.navercorp.pinpoint.plugin.thread;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author echo
 */
public class ThreadConfig {
    private final String threadMatchPackage;

    public ThreadConfig(ProfilerConfig config) {
        this.threadMatchPackage = config.readString("profiler.thread.match.package", null);
    }

    public String getThreadMatchPackage() {
        return threadMatchPackage;
    }

    @Override
    public String toString() {
        return "ThreadConfig{" +
                "threadMatchPackage='" + threadMatchPackage + '\'' +
                '}';
    }
}
