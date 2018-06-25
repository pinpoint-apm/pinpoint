package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class JdkHttpPluginConfig {

    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;

    public JdkHttpPluginConfig(ProfilerConfig src) {
        this.param = src.readBoolean("profiler.jdk.http.param", true);
        this.httpDumpConfig = HttpDumpConfig.getDefault();
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JdkHttpPluginConfig{");
        sb.append("param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append('}');
        return sb.toString();
    }
}
