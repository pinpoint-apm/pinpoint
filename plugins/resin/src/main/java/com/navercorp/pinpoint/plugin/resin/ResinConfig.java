package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

/**
 * @author huangpengjie@fang.com
 */
public class ResinConfig {

    private final boolean enable;
    private final String bootstrapMains;
    private final boolean hidePinpointHeader;
    private final boolean traceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final Filter<String> excludeProfileMethodFilter;

    public ResinConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config");
        }

        // plugin
        this.enable = config.readBoolean("profiler.resin.enable", true);
        this.bootstrapMains = config.readString("profiler.resin.bootstrap.main", "");
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.traceRequestParam = serverConfig.isTraceRequestParam("profiler.resin.tracerequestparam");
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter("profiler.resin.excludeurl");
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
        final StringBuilder sb = new StringBuilder("ResinConfig{");
        sb.append("enable=").append(enable);
        sb.append(", bootstrapMains='").append(bootstrapMains).append('\'');
        sb.append(", hidePinpointHeader=").append(hidePinpointHeader);
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append('}');
        return sb.toString();
    }
}
