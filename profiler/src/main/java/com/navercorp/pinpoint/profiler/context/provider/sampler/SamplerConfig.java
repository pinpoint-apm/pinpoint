package com.navercorp.pinpoint.profiler.context.provider.sampler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.sampler.SamplerType;

public class SamplerConfig {
    public static final String SAMPLER_ENABLE_NAME = "profiler.sampling.enable";
    public static final String URL_SAMPLER_ENABLE_NAME = "profiler.sampling.url.enable";

    private final boolean samplingEnable;
    private final SamplerType samplerType;
    private final boolean urlSamplingEnable;

    public SamplerConfig(ProfilerConfig profilerConfig) {
        this.samplingEnable = profilerConfig.readBoolean(SAMPLER_ENABLE_NAME, true);
        this.urlSamplingEnable = profilerConfig.readBoolean(URL_SAMPLER_ENABLE_NAME, true);
        String rateSamplerType = profilerConfig.readString("profiler.sampling.type", SamplerType.COUNTING.name());
        this.samplerType = SamplerType.of(rateSamplerType);
    }

    public boolean isSamplingEnable() {
        return samplingEnable;
    }

    public SamplerType getSamplerType() {
        return samplerType;
    }

    public boolean isUrlSamplingEnable() {
        return urlSamplingEnable;
    }

    @Override
    public String toString() {
        return "SamplerConfig{" +
                "samplingEnable=" + samplingEnable +
                ", samplerType=" + samplerType +
                '}';
    }
}
