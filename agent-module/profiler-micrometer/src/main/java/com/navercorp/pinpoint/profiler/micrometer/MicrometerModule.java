package com.navercorp.pinpoint.profiler.micrometer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.navercorp.pinpoint.common.config.util.ValueAnnotationProcessor;
import com.navercorp.pinpoint.profiler.micrometer.config.DefaultMicrometerConfig;
import com.navercorp.pinpoint.profiler.micrometer.config.MicrometerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Function;

public class MicrometerModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Function<String, String> properties;

    public MicrometerModule(Function<String, String> properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    protected void configure() {
        logger.info("load {}", this.getClass().getSimpleName());

        final ValueAnnotationProcessor process = new ValueAnnotationProcessor();

        MicrometerConfig micrometerConfig = new DefaultMicrometerConfig();
        process.process(micrometerConfig, properties);

        logger.info("{}", micrometerConfig);

        if (micrometerConfig.isEnable()) {
            bind(MicrometerConfig.class).toInstance(micrometerConfig);
            bind(MicrometerMonitor.class).to(DefaultMicrometerMonitor.class).in(Singleton.class);
        } else {
            bind(MicrometerMonitor.class).to(DisableMicrometerMonitor.class).in(Singleton.class);;
        }
    }
}
