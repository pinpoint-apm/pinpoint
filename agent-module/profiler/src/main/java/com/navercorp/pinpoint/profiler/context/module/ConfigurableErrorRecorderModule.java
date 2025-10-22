package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.error.ConfigurableErrorRecorderFactory;
import com.navercorp.pinpoint.profiler.context.config.ErrorRecorderConfig;
import com.navercorp.pinpoint.profiler.context.error.ErrorRecorderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class ConfigurableErrorRecorderModule extends PrivateModule {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ErrorRecorderConfig errorRecorderConfig;

    public ConfigurableErrorRecorderModule(ErrorRecorderConfig errorRecorderConfig) {
        this.errorRecorderConfig = Objects.requireNonNull(errorRecorderConfig, "errorRecorderConfig");
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        bind(ErrorRecorderConfig.class).toInstance(errorRecorderConfig);
        bind(ErrorRecorderFactory.class).to(ConfigurableErrorRecorderFactory.class).in(Scopes.SINGLETON);

        expose(ErrorRecorderFactory.class);
    }
}
