package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolderIdGenerator;

import java.util.Objects;

public class InterceptorHolderIdGeneratorProvider implements Provider<InterceptorHolderIdGenerator> {

    private final InterceptorHolderIdGenerator interceptorHolderIdGenerator;

    @Inject
    public InterceptorHolderIdGeneratorProvider(InstrumentConfig instrumentConfig) {
        this(getInterceptorRegistrySize(instrumentConfig), getInterceptorRegistryBootstrapSize(instrumentConfig));
    }

    private static int getInterceptorRegistrySize(InstrumentConfig instrumentConfig) {
        Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        return instrumentConfig.getInterceptorRegistrySize();
    }

    private static int getInterceptorRegistryBootstrapSize(InstrumentConfig instrumentConfig) {
        Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        return instrumentConfig.getInterceptorRegistryBootstrapSize();
    }

    public InterceptorHolderIdGeneratorProvider(int interceptorSize, int interceptorBootstrapSize) {
        this.interceptorHolderIdGenerator = new InterceptorHolderIdGenerator(interceptorSize, interceptorBootstrapSize);
    }

    @Override
    public InterceptorHolderIdGenerator get() {
        return interceptorHolderIdGenerator;
    }
}
