package com.navercorp.pinpoint.collector.sampler;

public enum SamplerType {
    MOD,
    PERCENT;

    public static final SamplerType DEFAULT_SAMPLER_TYPE = MOD;

    public static SamplerType of(String name) {
        for (SamplerType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return DEFAULT_SAMPLER_TYPE;
    }
}
