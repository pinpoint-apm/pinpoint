package com.navercorp.pinpoint.plugin.spring.webflux6;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class SpringWebFluxPluginConfig {
    private final boolean enable;
    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;
    private final boolean clientEnable;

    private final boolean uriStatEnable;
    private final boolean uriStatUseUserInput;
    private final boolean uriStatCollectMethod;

    private final boolean versionForcedMatch;
    public SpringWebFluxPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        this.enable = config.readBoolean("profiler.spring.webflux6.enable", true);
        this.versionForcedMatch = config.readBoolean("profiler.spring.webflux6.version.forced.match", false);

        // Client
        this.clientEnable = config.readBoolean("profiler.spring.webflux.client.enable", false);
        this.param = config.readBoolean("profiler.spring.webflux.client.param", true);
        boolean cookie = config.readBoolean("profiler.spring.webflux.client.cookie", false);
        DumpType cookieDumpType = config.readDumpType("profiler.spring.webflux.client.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = config.readInt("profiler.spring.webflux.client.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.spring.webflux.client.cookie.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, false, cookieDumpType, 1, 1024);
        this.uriStatEnable = config.readBoolean("profiler.uri.stat.spring.webflux.enable", false);
        this.uriStatUseUserInput = config.readBoolean("profiler.uri.stat.spring.webflux.useuserinput", false);
        this.uriStatCollectMethod = config.readBoolean("profiler.uri.stat.collect.http.method", false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    public boolean isClientEnable() {
        return clientEnable;
    }

    public boolean isUriStatEnable() {
        return uriStatEnable;
    }

    public boolean isUriStatCollectMethod() {
        return uriStatCollectMethod;
    }

    public boolean isUriStatUseUserInput() {
        return uriStatUseUserInput;
    }

    public boolean isVersionForcedMatch() {
        return versionForcedMatch;
    }

    @Override
    public String toString() {
        return "SpringWebFluxPluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", httpDumpConfig=" + httpDumpConfig +
                ", clientEnable=" + clientEnable +
                ", uriStatEnable=" + uriStatEnable +
                ", uriStatUseUserInput=" + uriStatUseUserInput +
                ", uriStatCollectMethod=" + uriStatCollectMethod +
                ", versionForcedMatch=" + versionForcedMatch +
                '}';
    }
}
