package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author huangpengjie@fang.com
 *
 */
public class ResinConfig {

    private final boolean resinEnable;

    private final String resinBootstrapMains;

    private final boolean resinHidePinpointHeader;

    private final boolean resinTraceRequestParam;

    private final Filter<String> resinExcludeUrlFilter;

    private final String resinRealIpHeader;

    private final String resinRealIpEmptyValue;

    private final Filter<String> resinExcludeProfileMethodFilter;

    private final boolean isTraceCookies;

    private final int cookieSamplingRate;

    private final DumpType cookieDumpType;

    public ResinConfig(ProfilerConfig config) {

        if (config == null) {
            throw new NullPointerException("config must not be null");
        }

        // plugin
        this.resinEnable = config.readBoolean("profiler.resin.enable", true);
        this.resinBootstrapMains = config.readString("profiler.resin.bootstrap.main", "");

        // runtime
        this.resinTraceRequestParam = config.readBoolean("profiler.resin.tracerequestparam", true);
        final String resinExcludeURL = config.readString("profiler.resin.excludeurl", "");
        if (!resinExcludeURL.isEmpty()) {
            this.resinExcludeUrlFilter = new ExcludePathFilter(resinExcludeURL);
        } else {
            this.resinExcludeUrlFilter = new ExcludePathFilter("");
        }
        this.resinRealIpHeader = config.readString("profiler.resin.realipheader", null);
        this.resinRealIpEmptyValue = config.readString("profiler.resin.realipemptyvalue", null);

        final String resinExcludeProfileMethod = config.readString("profiler.resin.excludemethod", "");
        if (!resinExcludeProfileMethod.isEmpty()) {
            this.resinExcludeProfileMethodFilter = new ExcludeMethodFilter(resinExcludeProfileMethod);
        } else {
            this.resinExcludeProfileMethodFilter = new ExcludeMethodFilter("");
        }
        this.resinHidePinpointHeader = config.readBoolean("profiler.resin.hidepinpointheader", true);
        this.isTraceCookies = config.readBoolean("profiler.resin.tracecookies", true);
        this.cookieSamplingRate = config.readInt("profiler.resin.cookie.sampling.rate", 10);
        this.cookieDumpType = config.readDumpType("profiler.resin.cookie.dumptype", DumpType.ALWAYS);
    }

    public boolean isResinEnable() {
        return resinEnable;
    }

    public String getResinBootstrapMains() {
        return resinBootstrapMains;
    }

    public boolean isResinTraceRequestParam() {
        return resinTraceRequestParam;
    }

    public Filter<String> getResinExcludeUrlFilter() {
        return resinExcludeUrlFilter;
    }

    public String getResinRealIpHeader() {
        return resinRealIpHeader;
    }

    public String getResinRealIpEmptyValue() {
        return resinRealIpEmptyValue;
    }

    public Filter<String> getResinExcludeProfileMethodFilter() {
        return resinExcludeProfileMethodFilter;
    }

    public boolean isResinHidePinpointHeader() {
        return resinHidePinpointHeader;
    }

    public boolean isTraceCookies() {
        return isTraceCookies;
    }

    public int getCookieSamplingRate() {
        return cookieSamplingRate;
    }

    public DumpType getCookieDumpType() {
        return cookieDumpType;
    }

}
