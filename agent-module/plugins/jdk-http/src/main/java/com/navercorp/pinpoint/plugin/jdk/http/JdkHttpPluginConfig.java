package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class JdkHttpPluginConfig {

    private final boolean param;
    private final boolean enable;
    private final boolean markError;

    public static boolean isParam(ProfilerConfig config) {
        return config.readBoolean("profiler.jdk.http.param", true);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.jdk.http.mark.error", true);
    }

    public JdkHttpPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.jdk.http", true);

        this.param = isParam(src);
        this.markError = isMarkError(src);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "JdkHttpPluginConfig{" +
                "param=" + param +
                ", enable=" + enable +
                ", markError=" + markError +
                '}';
    }
}
