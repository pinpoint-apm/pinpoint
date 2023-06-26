package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingService;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingServiceProvider;
import com.navercorp.pinpoint.profiler.context.exception.model.SpanEventExceptionFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.SpanEventExceptionFactoryProvider;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSamplerProvider;
import com.navercorp.pinpoint.profiler.context.module.config.ConfigurationLoader;
import com.navercorp.pinpoint.profiler.context.monitor.config.DefaultExceptionTraceConfig;
import com.navercorp.pinpoint.profiler.context.monitor.config.ExceptionTraceConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Properties;

public class ExceptionTraceModule extends PrivateModule {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Properties properties;

    public ExceptionTraceModule(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        ConfigurationLoader configurationLoader = new ConfigurationLoader(properties);

        ExceptionTraceConfig exceptionTraceConfig = new DefaultExceptionTraceConfig();
        configurationLoader.load(exceptionTraceConfig);
        logger.info("{}", exceptionTraceConfig);
        bind(ExceptionTraceConfig.class).toInstance(exceptionTraceConfig);

        bind(ExceptionTraceSampler.class).toProvider(ExceptionTraceSamplerProvider.class).in(Scopes.SINGLETON);
        bind(SpanEventExceptionFactory.class).toProvider(SpanEventExceptionFactoryProvider.class).in(Scopes.SINGLETON);

        bind(ExceptionRecordingService.class).toProvider(ExceptionRecordingServiceProvider.class).in(Scopes.SINGLETON);
        expose(ExceptionRecordingService.class);
    }
}
