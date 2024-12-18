package com.navercorp.pinpoint.profiler.context.module.config;

import com.navercorp.pinpoint.common.config.util.ValueAnnotationProcessor;

import java.util.Objects;
import java.util.Properties;

public class ConfigurationLoader {
    private final ValueAnnotationProcessor process = new ValueAnnotationProcessor();
    private final Properties properties;

    public ConfigurationLoader(Properties properties) {
        // Function<String, String> not recommended. Prevent recursive call.
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public <T> void load(T config) {
        process.process(config, properties::getProperty);
    }
}
