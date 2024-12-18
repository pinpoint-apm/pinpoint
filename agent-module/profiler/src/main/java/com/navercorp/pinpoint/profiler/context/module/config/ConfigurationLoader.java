package com.navercorp.pinpoint.profiler.context.module.config;

import com.navercorp.pinpoint.common.config.util.ValueAnnotationProcessor;

import java.util.Objects;
import java.util.function.Function;

public class ConfigurationLoader {
    private final ValueAnnotationProcessor process = new ValueAnnotationProcessor();
    private final Function<String, String> properties;

    public ConfigurationLoader(Function<String, String> properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public <T> void load(T config) {
        process.process(config, properties);
    }
}
