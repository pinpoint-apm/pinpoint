package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * Created by Naver on 2015-11-17.
 */
public class JdkHttpPluginConfig {

    private boolean param = true;

    public JdkHttpPluginConfig(ProfilerConfig src) {
        this.param = src.readBoolean("profiler.jdk.http.param", true);
    }

    public boolean isParam() {
        return param;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JdkHttpPluginConfig{");
        sb.append("param=").append(param);
        sb.append('}');
        return sb.toString();
    }
}
