package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorderFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;
import com.navercorp.pinpoint.profiler.context.exception.storage.ExceptionStorageFactory;
import com.navercorp.pinpoint.profiler.context.monitor.config.ExceptionTraceConfig;
import com.navercorp.pinpoint.profiler.context.provider.exception.ExceptionContextFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.exception.ExceptionRecorderFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.exception.ExceptionStorageFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.exception.ExceptionTraceSamplerProvider;
import com.navercorp.pinpoint.profiler.context.provider.exception.ExceptionWrapperFactoryProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class ExceptionTraceModule extends PrivateModule {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ExceptionTraceConfig exceptionTraceConfig;

    public ExceptionTraceModule(ExceptionTraceConfig exceptionTraceConfig) {
        this.exceptionTraceConfig = Objects.requireNonNull(exceptionTraceConfig, "exceptionTraceConfig");
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());
        bind(ExceptionTraceConfig.class).toInstance(exceptionTraceConfig);

        bind(ExceptionChainSampler.class).toProvider(ExceptionTraceSamplerProvider.class).in(Scopes.SINGLETON);
        bind(ExceptionWrapperFactory.class).toProvider(ExceptionWrapperFactoryProvider.class).in(Scopes.SINGLETON);
        bind(ExceptionStorageFactory.class).toProvider(ExceptionStorageFactoryProvider.class).in(Scopes.SINGLETON);

        bind(ExceptionContextFactory.class).toProvider(ExceptionContextFactoryProvider.class).in(Scopes.SINGLETON);
        expose(ExceptionContextFactory.class);

        bind(ExceptionRecorderFactory.class).toProvider(ExceptionRecorderFactoryProvider.class).in(Scopes.SINGLETON);
        expose(ExceptionRecorderFactory.class);
    }
}
