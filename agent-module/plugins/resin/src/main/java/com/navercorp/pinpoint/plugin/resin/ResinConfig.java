package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

import java.util.Objects;

/**
 * @author huangpengjie@fang.com
 */
public class ResinConfig {

    private final boolean enable;
    private final String bootstrapMains;
    private final boolean hidePinpointHeader;
    private final boolean traceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> traceExcludeMethodFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final Filter<String> excludeProfileMethodFilter;

    public ResinConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.resin.enable", true);
        this.bootstrapMains = config.readString("profiler.resin.bootstrap.main", "");
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.traceRequestParam = serverConfig.isTraceRequestParam("profiler.resin.tracerequestparam");
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter("profiler.resin.excludeurl");
        this.traceExcludeMethodFilter = serverConfig.getTraceExcludeMethodFilter("profiler.resin.trace.excludemethod");
        this.realIpHeader = serverConfig.getRealIpHeader("profiler.resin.realipheader");
        this.realIpEmptyValue = serverConfig.getRealIpEmptyValue("profiler.resin.realipemptyvalue");
        this.excludeProfileMethodFilter = serverConfig.getExcludeMethodFilter("profiler.resin.excludemethod");
        this.hidePinpointHeader = serverConfig.isHidePinpointHeader("profiler.resin.hidepinpointheader");
    }

    public boolean isEnable() {
        return enable;
    }

    public String getBootstrapMains() {
        return bootstrapMains;
    }

    public boolean isTraceRequestParam() {
        return traceRequestParam;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public Filter<String> getTraceExcludeMethodFilter() {
        return traceExcludeMethodFilter;
    }

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public String getRealIpEmptyValue() {
        return realIpEmptyValue;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    @Override
    public String toString() {
        return "ResinConfig{" +
                "enable=" + enable +
                ", bootstrapMains='" + bootstrapMains + '\'' +
                ", hidePinpointHeader=" + hidePinpointHeader +
                ", traceRequestParam=" + traceRequestParam +
                ", excludeUrlFilter=" + excludeUrlFilter +
                ", traceExcludeMethodFilter=" + traceExcludeMethodFilter +
                ", realIpHeader='" + realIpHeader + '\'' +
                ", realIpEmptyValue='" + realIpEmptyValue + '\'' +
                ", excludeProfileMethodFilter=" + excludeProfileMethodFilter +
                '}';
    }
}
