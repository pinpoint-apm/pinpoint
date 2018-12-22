package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.transaction.RegistryMapping;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.transaction.IMappingRegistry;

public class MappingRegistryProvider implements Provider<IMappingRegistry> {

    private final ProfilerConfig profilerConfig;

    @Inject
    public MappingRegistryProvider(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
    }

    @Override
    public IMappingRegistry get() {
        return new RegistryMapping(profilerConfig);
    }
}
